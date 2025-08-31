package com.chubini.pku.consents;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientConsentService {

  private final PatientConsentRepository consentRepository;

  /** Grant a new consent or update existing one */
  @Transactional
  public PatientConsent grantConsent(
      PatientProfile patient,
      PatientConsent.ConsentType consentType,
      String reason,
      LocalDateTime expiresAt,
      String grantedBy) {

    // Check if there's already an active consent of this type
    Optional<PatientConsent> existingActive =
        consentRepository.findActiveConsentByPatientAndType(
            patient, consentType, LocalDateTime.now());

    if (existingActive.isPresent()) {
      // Revoke existing consent before creating new one
      PatientConsent existing = existingActive.get();
      existing.setStatus(PatientConsent.ConsentStatus.REVOKED);
      existing.setRevokedAt(LocalDateTime.now());
      existing.setRevokedBy(grantedBy);
      existing.setRevokedReason("Replaced by new consent");
      consentRepository.save(existing);

      log.info(
          "Revoked existing consent {} for patient {} and type {}",
          existing.getId(),
          patient.getId(),
          consentType);
    }

    // Get next version number
    Integer nextVersion = getNextVersionNumber(patient, consentType);

    // Create new consent
    PatientConsent newConsent =
        PatientConsent.builder()
            .patient(patient)
            .consentType(consentType)
            .status(PatientConsent.ConsentStatus.GRANTED)
            .version(nextVersion)
            .grantedReason(reason)
            .grantedAt(LocalDateTime.now())
            .expiresAt(expiresAt)
            .grantedBy(grantedBy)
            .build();

    PatientConsent saved = consentRepository.save(newConsent);
    log.info(
        "Granted new consent {} for patient {} and type {} (version {})",
        saved.getId(),
        patient.getId(),
        consentType,
        nextVersion);

    return saved;
  }

  /** Revoke an existing consent */
  @Transactional
  public PatientConsent revokeConsent(UUID consentId, String reason, String revokedBy) {
    PatientConsent consent =
        consentRepository
            .findById(consentId)
            .orElseThrow(
                () -> new ConsentNotFoundException("Consent not found with ID: " + consentId));

    if (consent.getStatus() != PatientConsent.ConsentStatus.GRANTED) {
      throw new IllegalStateException("Consent is not currently granted");
    }

    consent.setStatus(PatientConsent.ConsentStatus.REVOKED);
    consent.setRevokedAt(LocalDateTime.now());
    consent.setRevokedBy(revokedBy);
    consent.setRevokedReason(reason);

    PatientConsent saved = consentRepository.save(consent);
    log.info(
        "Revoked consent {} for patient {} and type {}",
        consentId,
        consent.getPatient().getId(),
        consent.getConsentType());

    return saved;
  }

  /** Check if a patient has active consent for a specific type */
  public boolean hasActiveConsent(PatientProfile patient, PatientConsent.ConsentType consentType) {
    Optional<PatientConsent> activeConsent =
        consentRepository.findActiveConsentByPatientAndType(
            patient, consentType, LocalDateTime.now());
    return activeConsent.isPresent();
  }

  /** Get active consent for a patient and type */
  public Optional<PatientConsent> getActiveConsent(
      PatientProfile patient, PatientConsent.ConsentType consentType) {
    return consentRepository.findActiveConsentByPatientAndType(
        patient, consentType, LocalDateTime.now());
  }

  /** Get all consents for a patient */
  public List<PatientConsent> getPatientConsents(PatientProfile patient) {
    return consentRepository.findByPatientOrderByCreatedAtDesc(patient);
  }

  /** Get all active consents for a patient */
  public List<PatientConsent> getActivePatientConsents(PatientProfile patient) {
    return consentRepository.findActiveConsentsByPatient(patient, LocalDateTime.now());
  }

  /** Get latest consent version number for a patient and type */
  private Integer getNextVersionNumber(
      PatientProfile patient, PatientConsent.ConsentType consentType) {
    Optional<PatientConsent> latest =
        consentRepository.findLatestConsentByPatientAndType(patient, consentType);
    return latest.map(consent -> consent.getVersion() + 1).orElse(1);
  }

  /** Process expired consents (should be called by scheduled job) */
  @Transactional
  public void processExpiredConsents() {
    List<PatientConsent> expiredConsents =
        consentRepository.findExpiredConsents(LocalDateTime.now());

    for (PatientConsent consent : expiredConsents) {
      consent.setStatus(PatientConsent.ConsentStatus.EXPIRED);
      consentRepository.save(consent);

      log.info(
          "Marked consent {} as expired for patient {} and type {}",
          consent.getId(),
          consent.getPatient().getId(),
          consent.getConsentType());
    }

    if (!expiredConsents.isEmpty()) {
      log.info("Processed {} expired consents", expiredConsents.size());
    }
  }

  /** Get consents expiring soon (for notification purposes) */
  public List<PatientConsent> getConsentsExpiringSoon(int hoursAhead) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime futureTime = now.plusHours(hoursAhead);
    return consentRepository.findConsentsExpiringSoon(now, futureTime);
  }

  /** Count active consents by type (for analytics) */
  public long countActiveConsentsByType(PatientConsent.ConsentType consentType) {
    return consentRepository.countActiveConsentsByType(consentType, LocalDateTime.now());
  }

  /** Exception for consent-related errors */
  public static class ConsentNotFoundException extends RuntimeException {
    public ConsentNotFoundException(String message) {
      super(message);
    }
  }
}
