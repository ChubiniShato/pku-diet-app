package com.chubini.pku.moderation;

import java.time.LocalDateTime;
import java.util.*;

import com.chubini.pku.consents.PatientConsent;
import com.chubini.pku.consents.PatientConsentService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Service for managing moderation submissions to global catalog */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationSubmissionService {

  private final ModerationSubmissionRepository submissionRepository;
  private final PatientConsentService consentService;

  // TODO: Inject repositories for products, custom products, etc.

  /** Create a new moderation submission */
  @Transactional
  public ModerationSubmission createSubmission(
      UUID patientId,
      ModerationSubmission.TargetType targetType,
      String payloadRef,
      String title,
      String description,
      String createdBy) {

    log.info(
        "Creating moderation submission for patient {}: {} {}", patientId, targetType, payloadRef);

    // Check if patient has GLOBAL_SUBMISSION_OPTIN consent
    if (!consentService.hasActiveConsent(
        null, PatientConsent.ConsentType.GLOBAL_SUBMISSION_OPTIN)) {
      throw new InsufficientConsentException(
          "Patient must grant GLOBAL_SUBMISSION_OPTIN consent for catalog submissions");
    }

    // Validate payload reference
    validatePayloadReference(targetType, payloadRef);

    // Check for conflicts
    List<String> conflicts = detectConflicts(targetType, payloadRef);

    ModerationSubmission submission =
        ModerationSubmission.builder()
            .patient(null) // TODO: Get patient from repository
            .targetType(targetType)
            .payloadRef(payloadRef)
            .submissionTitle(title)
            .submissionDescription(description)
            .status(
                conflicts.isEmpty()
                    ? ModerationSubmission.ModerationStatus.PENDING
                    : ModerationSubmission.ModerationStatus.DRAFT)
            .conflicts(conflicts)
            .createdBy(createdBy)
            .expiresAt(LocalDateTime.now().plusDays(30)) // Submissions expire in 30 days
            .build();

    ModerationSubmission saved = submissionRepository.save(submission);

    log.info("Created moderation submission {} for patient {}", saved.getId(), patientId);

    // TODO: Publish event for notifications
    // publishSubmissionCreatedEvent(saved);

    return saved;
  }

  /** Get submissions for a patient */
  public List<ModerationSubmission> getPatientSubmissions(UUID patientId) {
    // TODO: Get patient and filter by patient
    return submissionRepository.findAll(); // Temporary implementation
  }

  /** Get moderation queue for reviewers */
  public List<ModerationSubmission> getModerationQueue(int page, int size) {
    // TODO: Implement proper pagination and filtering
    return submissionRepository.findAll(); // Temporary implementation
  }

  /** Review and approve a submission */
  @Transactional
  public ModerationSubmission approveSubmission(
      UUID submissionId, String reviewerId, String reviewNotes, Map<String, String> mergeData) {

    ModerationSubmission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(
                () ->
                    new ModerationSubmissionNotFoundException(
                        "Submission not found: " + submissionId));

    if (submission.getStatus() != ModerationSubmission.ModerationStatus.PENDING
        && submission.getStatus() != ModerationSubmission.ModerationStatus.UNDER_REVIEW) {
      throw new IllegalStateException("Submission is not in a reviewable state");
    }

    // Process the approval based on target type
    UUID approvedProductId = processApproval(submission, mergeData);

    submission.markApproved(reviewerId, reviewNotes, approvedProductId);

    ModerationSubmission saved = submissionRepository.save(submission);

    log.info("Approved moderation submission {} by reviewer {}", submissionId, reviewerId);

    // TODO: Publish approval event
    // publishSubmissionStatusChangedEvent(saved, "APPROVED");

    return saved;
  }

  /** Review and reject a submission */
  @Transactional
  public ModerationSubmission rejectSubmission(
      UUID submissionId, String reviewerId, String reviewNotes) {

    ModerationSubmission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(
                () ->
                    new ModerationSubmissionNotFoundException(
                        "Submission not found: " + submissionId));

    if (submission.getStatus() != ModerationSubmission.ModerationStatus.PENDING
        && submission.getStatus() != ModerationSubmission.ModerationStatus.UNDER_REVIEW) {
      throw new IllegalStateException("Submission is not in a reviewable state");
    }

    submission.markRejected(reviewerId, reviewNotes);

    ModerationSubmission saved = submissionRepository.save(submission);

    log.info("Rejected moderation submission {} by reviewer {}", submissionId, reviewerId);

    // TODO: Publish rejection event
    // publishSubmissionStatusChangedEvent(saved, "REJECTED");

    return saved;
  }

  /** Mark submission as under review */
  @Transactional
  public ModerationSubmission markUnderReview(UUID submissionId, String reviewerId) {
    ModerationSubmission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(
                () ->
                    new ModerationSubmissionNotFoundException(
                        "Submission not found: " + submissionId));

    if (submission.getStatus() != ModerationSubmission.ModerationStatus.PENDING) {
      throw new IllegalStateException("Only PENDING submissions can be marked under review");
    }

    submission.markUnderReview(reviewerId);

    ModerationSubmission saved = submissionRepository.save(submission);

    log.info("Marked submission {} under review by {}", submissionId, reviewerId);

    return saved;
  }

  /** Process expired submissions */
  @Transactional
  public void processExpiredSubmissions() {
    List<ModerationSubmission> expiredSubmissions =
        submissionRepository.findAll().stream()
            .filter(ModerationSubmission::isExpired)
            .filter(
                sub ->
                    sub.getStatus() == ModerationSubmission.ModerationStatus.PENDING
                        || sub.getStatus() == ModerationSubmission.ModerationStatus.DRAFT)
            .toList();

    for (ModerationSubmission submission : expiredSubmissions) {
      submission.markExpired();
      submissionRepository.save(submission);
      log.info("Marked submission {} as expired", submission.getId());
    }

    if (!expiredSubmissions.isEmpty()) {
      log.info("Processed {} expired submissions", expiredSubmissions.size());
    }
  }

  /** Get submission statistics */
  public ModerationStatistics getStatistics() {
    // TODO: Implement proper statistics calculation
    return new ModerationStatistics(
        submissionRepository.findAll().size(),
        (int)
            submissionRepository.findAll().stream()
                .filter(s -> s.getStatus() == ModerationSubmission.ModerationStatus.PENDING)
                .count(),
        (int)
            submissionRepository.findAll().stream()
                .filter(s -> s.getStatus() == ModerationSubmission.ModerationStatus.APPROVED)
                .count(),
        (int)
            submissionRepository.findAll().stream()
                .filter(s -> s.getStatus() == ModerationSubmission.ModerationStatus.REJECTED)
                .count());
  }

  /** Validate payload reference format */
  private void validatePayloadReference(
      ModerationSubmission.TargetType targetType, String payloadRef) {
    if (payloadRef == null || payloadRef.trim().isEmpty()) {
      throw new IllegalArgumentException("Payload reference cannot be empty");
    }

    String[] parts = payloadRef.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid payload reference format. Expected: 'type:id'");
    }

    String type = parts[0];
    String id = parts[1];

    // Validate type matches target type
    String expectedType =
        switch (targetType) {
          case CUSTOM_PRODUCT -> "custom_product";
          case CUSTOM_DISH -> "custom_dish";
          case LABEL_SCAN -> "label_scan";
        };

    if (!type.equals(expectedType)) {
      throw new IllegalArgumentException(
          String.format("Payload type '%s' does not match target type '%s'", type, expectedType));
    }

    // Validate ID is a valid UUID
    try {
      UUID.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID in payload reference: " + id);
    }

    // TODO: Validate that the entity actually exists
  }

  /** Detect conflicts with existing catalog items */
  private List<String> detectConflicts(
      ModerationSubmission.TargetType targetType, String payloadRef) {
    List<String> conflicts = new ArrayList<>();

    // TODO: Implement conflict detection based on:
    // - Barcode duplicates
    // - Name + brand + weight similarity
    // - Ingredient composition similarity

    // Placeholder conflict detection
    if (targetType == ModerationSubmission.TargetType.CUSTOM_PRODUCT) {
      // Check for existing products with similar names
      conflicts.add("Potential duplicate: Similar product exists in catalog");
    }

    return conflicts;
  }

  /** Process approval based on target type */
  private UUID processApproval(ModerationSubmission submission, Map<String, String> mergeData) {
    // TODO: Implement approval processing based on target type

    switch (submission.getTargetType()) {
      case CUSTOM_PRODUCT:
        return processCustomProductApproval(submission, mergeData);
      case CUSTOM_DISH:
        return processCustomDishApproval(submission, mergeData);
      case LABEL_SCAN:
        return processLabelScanApproval(submission, mergeData);
      default:
        throw new IllegalArgumentException(
            "Unsupported target type: " + submission.getTargetType());
    }
  }

  private UUID processCustomProductApproval(
      ModerationSubmission submission, Map<String, String> mergeData) {
    // TODO: Create or update global product from custom product
    // Set isVerified = true, visibility = PUBLIC
    log.info("Processing custom product approval for submission {}", submission.getId());
    return UUID.randomUUID(); // Placeholder
  }

  private UUID processCustomDishApproval(
      ModerationSubmission submission, Map<String, String> mergeData) {
    // TODO: Create or update global dish from custom dish
    log.info("Processing custom dish approval for submission {}", submission.getId());
    return UUID.randomUUID(); // Placeholder
  }

  private UUID processLabelScanApproval(
      ModerationSubmission submission, Map<String, String> mergeData) {
    // TODO: Create product from label scan data
    log.info("Processing label scan approval for submission {}", submission.getId());
    return UUID.randomUUID(); // Placeholder
  }

  /** Statistics for moderation dashboard */
  public record ModerationStatistics(
      int totalSubmissions,
      int pendingSubmissions,
      int approvedSubmissions,
      int rejectedSubmissions) {}

  /** Exception classes */
  public static class ModerationSubmissionNotFoundException extends RuntimeException {
    public ModerationSubmissionNotFoundException(String message) {
      super(message);
    }
  }

  public static class InsufficientConsentException extends RuntimeException {
    public InsufficientConsentException(String message) {
      super(message);
    }
  }
}
