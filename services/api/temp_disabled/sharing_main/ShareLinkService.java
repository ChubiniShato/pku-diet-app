package com.chubini.pku.sharing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.chubini.pku.consents.PatientConsent;
import com.chubini.pku.consents.PatientConsentService;
import com.chubini.pku.patients.PatientProfile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShareLinkService {

  private final ShareLinkRepository shareLinkRepository;
  private final ShareLinkAccessLogRepository accessLogRepository;
  private final PatientConsentService consentService;

  /** Create a new share link for a patient */
  @Transactional
  public ShareLink createShareLink(
      PatientProfile patient,
      Set<ShareLink.ShareScope> scopes,
      String doctorEmail,
      String doctorName,
      Integer ttlHours,
      Boolean oneTimeUse,
      String deviceBound,
      String createdBy) {

    // Check if patient has SHARE_WITH_DOCTOR consent
    if (!consentService.hasActiveConsent(patient, PatientConsent.ConsentType.SHARE_WITH_DOCTOR)) {
      throw new InsufficientConsentException(
          "Patient must grant SHARE_WITH_DOCTOR consent before creating share links");
    }

    // Validate scopes
    if (scopes == null || scopes.isEmpty()) {
      throw new IllegalArgumentException("At least one scope must be specified");
    }

    // Create the share link
    ShareLink shareLink =
        ShareLink.createNew(
            patient, scopes, doctorEmail, doctorName, ttlHours, oneTimeUse, deviceBound, createdBy);

    ShareLink saved = shareLinkRepository.save(shareLink);

    // Send email invitation if doctor email provided
    if (doctorEmail != null && !doctorEmail.trim().isEmpty()) {
      // TODO: Integrate with email service
      log.info(
          "Share link created for patient {} with doctor email: {}", patient.getId(), doctorEmail);
    }

    log.info(
        "Created share link {} for patient {} with scopes: {}",
        saved.getId(),
        patient.getId(),
        scopes);

    return saved;
  }

  /** Validate and access a share link */
  @Transactional
  public ShareLinkAccessResult validateAndAccessShareLink(
      String token,
      String accessType,
      String resourceAccessed,
      String clientIp,
      String userAgent,
      Long responseTimeMs) {

    Optional<ShareLink> shareLinkOpt = shareLinkRepository.findByToken(token);
    if (shareLinkOpt.isEmpty()) {
      logAccessFailure(null, accessType, "Share link not found", clientIp, userAgent);
      return ShareLinkAccessResult.failure("Share link not found");
    }

    ShareLink shareLink = shareLinkOpt.get();

    // Check if link is usable
    if (!shareLink.isUsable()) {
      String reason =
          switch (shareLink.getStatus()) {
            case REVOKED -> "Share link has been revoked";
            case USED -> "One-time share link has already been used";
            case EXPIRED -> "Share link has expired";
            default -> "Share link is not available";
          };

      logAccessFailure(shareLink, accessType, reason, clientIp, userAgent);
      return ShareLinkAccessResult.failure(reason);
    }

    // Check if requested scope is allowed
    if (resourceAccessed != null) {
      ShareLink.ShareScope requestedScope = parseResourceScope(resourceAccessed);
      if (requestedScope != null && !shareLink.allowsScope(requestedScope)) {
        logAccessFailure(shareLink, accessType, "Requested scope not allowed", clientIp, userAgent);
        return ShareLinkAccessResult.failure("Requested scope not allowed for this share link");
      }
    }

    // Record successful access
    shareLink.recordUsage();
    shareLinkRepository.save(shareLink);

    // Log the access
    ShareLinkAccessLog accessLog =
        ShareLinkAccessLog.success(
            shareLink, accessType, resourceAccessed, clientIp, userAgent, responseTimeMs);
    accessLogRepository.save(accessLog);

    log.info(
        "Share link {} accessed successfully by IP: {} for resource: {}",
        shareLink.getId(),
        clientIp,
        resourceAccessed);

    return ShareLinkAccessResult.success(shareLink);
  }

  /** Revoke a share link */
  @Transactional
  public ShareLink revokeShareLink(UUID shareLinkId, String revokedBy) {
    ShareLink shareLink =
        shareLinkRepository
            .findById(shareLinkId)
            .orElseThrow(
                () -> new ShareLinkNotFoundException("Share link not found: " + shareLinkId));

    if (shareLink.getStatus() == ShareLink.ShareLinkStatus.REVOKED) {
      throw new IllegalStateException("Share link is already revoked");
    }

    shareLink.revoke(revokedBy);
    ShareLink saved = shareLinkRepository.save(shareLink);

    log.info("Share link {} revoked by {}", shareLinkId, revokedBy);
    return saved;
  }

  /** Get all share links for a patient */
  public List<ShareLink> getPatientShareLinks(PatientProfile patient) {
    return shareLinkRepository.findByPatientOrderByCreatedAtDesc(patient);
  }

  /** Get active share links for a patient */
  public List<ShareLink> getActivePatientShareLinks(PatientProfile patient) {
    return shareLinkRepository.findActiveShareLinksByPatient(patient, LocalDateTime.now());
  }

  /** Get share link by token (for internal use) */
  public Optional<ShareLink> getShareLinkByToken(String token) {
    return shareLinkRepository.findByToken(token);
  }

  /** Process expired share links (should be called by scheduled job) */
  @Transactional
  public void processExpiredShareLinks() {
    List<ShareLink> expiredLinks = shareLinkRepository.findExpiredShareLinks(LocalDateTime.now());

    for (ShareLink link : expiredLinks) {
      link.setStatus(ShareLink.ShareLinkStatus.EXPIRED);
      shareLinkRepository.save(link);
    }

    if (!expiredLinks.isEmpty()) {
      log.info("Processed {} expired share links", expiredLinks.size());
    }
  }

  /** Get share links expiring soon */
  public List<ShareLink> getShareLinksExpiringSoon(int hoursAhead) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime futureTime = now.plusHours(hoursAhead);
    return shareLinkRepository.findShareLinksExpiringSoon(now, futureTime);
  }

  /** Get access logs for a share link */
  public List<ShareLinkAccessLog> getShareLinkAccessLogs(UUID shareLinkId) {
    ShareLink shareLink =
        shareLinkRepository
            .findById(shareLinkId)
            .orElseThrow(
                () -> new ShareLinkNotFoundException("Share link not found: " + shareLinkId));

    return accessLogRepository.findByShareLinkOrderByAccessedAtDesc(shareLink);
  }

  /** Parse resource scope from resource string */
  private ShareLink.ShareScope parseResourceScope(String resourceAccessed) {
    if (resourceAccessed == null) return null;

    if (resourceAccessed.startsWith("CRITICAL_FACTS")) {
      return ShareLink.ShareScope.CRITICAL_FACTS;
    } else if (resourceAccessed.startsWith("DAY:")) {
      return ShareLink.ShareScope.DAY;
    } else if (resourceAccessed.startsWith("WEEK:")) {
      return ShareLink.ShareScope.WEEK;
    } else if (resourceAccessed.startsWith("RANGE:")) {
      return ShareLink.ShareScope.RANGE;
    } else if (resourceAccessed.startsWith("NUTRITION_SUMMARY")) {
      return ShareLink.ShareScope.NUTRITION_SUMMARY;
    }

    return null;
  }

  /** Log access failure */
  private void logAccessFailure(
      ShareLink shareLink,
      String accessType,
      String errorMessage,
      String clientIp,
      String userAgent) {
    if (shareLink != null) {
      ShareLinkAccessLog failureLog =
          ShareLinkAccessLog.failure(shareLink, accessType, errorMessage, clientIp, userAgent);
      accessLogRepository.save(failureLog);
    }

    log.warn("Share link access failed: {} (IP: {})", errorMessage, clientIp);
  }

  /** Result class for share link access operations */
  public static class ShareLinkAccessResult {
    private final boolean success;
    private final ShareLink shareLink;
    private final String errorMessage;

    private ShareLinkAccessResult(boolean success, ShareLink shareLink, String errorMessage) {
      this.success = success;
      this.shareLink = shareLink;
      this.errorMessage = errorMessage;
    }

    public static ShareLinkAccessResult success(ShareLink shareLink) {
      return new ShareLinkAccessResult(true, shareLink, null);
    }

    public static ShareLinkAccessResult failure(String errorMessage) {
      return new ShareLinkAccessResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
      return success;
    }

    public ShareLink getShareLink() {
      return shareLink;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }

  /** Exception classes */
  public static class ShareLinkNotFoundException extends RuntimeException {
    public ShareLinkNotFoundException(String message) {
      super(message);
    }
  }

  public static class InsufficientConsentException extends RuntimeException {
    public InsufficientConsentException(String message) {
      super(message);
    }
  }
}
