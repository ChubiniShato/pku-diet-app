package com.chubini.pku.notifications;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Base notification message structure */
public abstract class NotificationMessage {

  private final UUID patientId;
  private final String patientName;
  private final NotificationType type;

  private final String title;
  private final String body;
  private final Map<String, Object> data;
  private final LocalDateTime timestamp;
  private final String priority;

  protected NotificationMessage(
      UUID patientId,
      String patientName,
      NotificationType type,
      String title,
      String body,
      Map<String, Object> data,
      String priority) {
    this.patientId = patientId;
    this.patientName = patientName;
    this.type = type;
    this.title = title;
    this.body = body;
    this.data = data;
    this.timestamp = LocalDateTime.now();
    this.priority = priority != null ? priority : "normal";
  }

  // Getters
  public UUID getPatientId() {
    return patientId;
  }

  public String getPatientName() {
    return patientName;
  }

  public NotificationType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getBody() {
    return body;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public String getPriority() {
    return priority;
  }

  /** Breach notification for limit violations */
  public static class BreachNotification extends NotificationMessage {
    private final String breachType; // PHE, PROTEIN, KCAL, FAT
    private final double deltaValue;
    private final String severity; // LOW, MEDIUM, HIGH, CRITICAL

    public BreachNotification(
        UUID patientId, String patientName, String breachType, double deltaValue, String severity) {
      super(
          patientId,
          patientName,
          NotificationType.BREACH,
          getBreachTitle(breachType, severity),
          getBreachBody(breachType, deltaValue, severity),
          Map.of("breachType", breachType, "delta", deltaValue, "severity", severity),
          getPriorityFromSeverity(severity));
      this.breachType = breachType;
      this.deltaValue = deltaValue;
      this.severity = severity;
    }

    private static String getBreachTitle(String breachType, String severity) {
      return String.format("Nutrition %s Alert", breachType.toUpperCase());
    }

    private static String getBreachBody(String breachType, double deltaValue, String severity) {
      String direction = deltaValue > 0 ? "above" : "below";
      String amount = Math.abs(deltaValue) + (breachType.equalsIgnoreCase("KCAL") ? " kcal" : "g");
      return String.format(
          "Your %s intake is %s recommended limit by %s. Priority: %s",
          breachType.toLowerCase(), direction, amount, severity);
    }

    private static String getPriorityFromSeverity(String severity) {
      return switch (severity.toLowerCase()) {
        case "critical" -> "high";
        case "high" -> "high";
        case "medium" -> "normal";
        case "low" -> "low";
        default -> "normal";
      };
    }

    public String getBreachType() {
      return breachType;
    }

    public double getDeltaValue() {
      return deltaValue;
    }

    public String getSeverity() {
      return severity;
    }
  }

  /** Suggestions notification for meal planning */
  public static class SuggestionsNotification extends NotificationMessage {
    private final int suggestionCount;
    private final String suggestionType; // SNACK, MEAL, ADJUSTMENT

    public SuggestionsNotification(
        UUID patientId, String patientName, int suggestionCount, String suggestionType) {
      super(
          patientId,
          patientName,
          NotificationType.SUGGESTIONS,
          "Dietary Suggestions Available",
          String.format(
              "You have %d new %s suggestions available",
              suggestionCount, suggestionType.toLowerCase()),
          Map.of("count", suggestionCount, "type", suggestionType),
          "normal");
      this.suggestionCount = suggestionCount;
      this.suggestionType = suggestionType;
    }

    public int getSuggestionCount() {
      return suggestionCount;
    }

    public String getSuggestionType() {
      return suggestionType;
    }
  }

  /** Consent expiration notification */
  public static class ConsentExpiryNotification extends NotificationMessage {
    private final String consentType;
    private final LocalDateTime expiryDate;

    public ConsentExpiryNotification(
        UUID patientId, String patientName, String consentType, LocalDateTime expiryDate) {
      super(
          patientId,
          patientName,
          NotificationType.CONSENT_EXPIRY,
          "Consent Expiring Soon",
          String.format("Your %s consent will expire on %s", consentType, expiryDate.toLocalDate()),
          Map.of("consentType", consentType, "expiryDate", expiryDate),
          "normal");
      this.consentType = consentType;
      this.expiryDate = expiryDate;
    }

    public String getConsentType() {
      return consentType;
    }

    public LocalDateTime getExpiryDate() {
      return expiryDate;
    }
  }

  /** Share link created notification */
  public static class ShareLinkCreatedNotification extends NotificationMessage {
    private final String doctorName;
    private final LocalDateTime expiryDate;

    public ShareLinkCreatedNotification(
        UUID patientId, String patientName, String doctorName, LocalDateTime expiryDate) {
      super(
          patientId,
          patientName,
          NotificationType.SHARE_LINK_CREATED,
          "Medical Data Shared",
          String.format(
              "Your data has been shared with Dr. %s (expires: %s)",
              doctorName, expiryDate.toLocalDate()),
          Map.of("doctorName", doctorName, "expiryDate", expiryDate),
          "normal");
      this.doctorName = doctorName;
      this.expiryDate = expiryDate;
    }

    public String getDoctorName() {
      return doctorName;
    }

    public LocalDateTime getExpiryDate() {
      return expiryDate;
    }
  }

  /** Submission status notification for moderation workflow */
  public static class SubmissionStatusNotification extends NotificationMessage {
    private final String newStatus;
    private final String targetType;
    private final String submissionTitle;
    private final String reviewerNotes;

    public SubmissionStatusNotification(
        UUID patientId,
        String patientName,
        String newStatus,
        String targetType,
        String submissionTitle,
        String reviewerNotes) {
      super(
          patientId,
          patientName,
          NotificationType.SUBMISSION_STATUS,
          "Submission Status Update",
          String.format(
              "Your %s submission status changed to: %s", targetType.toLowerCase(), newStatus),
          Map.of(
              "newStatus", newStatus, "targetType", targetType, "submissionTitle", submissionTitle),
          "normal");
      this.newStatus = newStatus;
      this.targetType = targetType;
      this.submissionTitle = submissionTitle;
      this.reviewerNotes = reviewerNotes;
    }

    public String getNewStatus() {
      return newStatus;
    }

    public String getTargetType() {
      return targetType;
    }

    public String getSubmissionTitle() {
      return submissionTitle;
    }

    public String getReviewerNotes() {
      return reviewerNotes;
    }
  }

  /** Label scan notification for scan completion */
  public static class LabelScanNotification extends NotificationMessage {
    private final String scanStatus;
    private final String matchedProductName;
    private final List<String> allergenHits;
    private final List<String> forbiddenHits;
    private final List<String> warnings;
    private final boolean hasSafetyConcerns;

    public LabelScanNotification(
        UUID patientId,
        String patientName,
        String scanStatus,
        String matchedProductName,
        List<String> allergenHits,
        List<String> forbiddenHits,
        List<String> warnings,
        boolean hasSafetyConcerns) {
      super(
          patientId,
          patientName,
          NotificationType.LABEL_SCAN,
          "Label Scan Completed",
          String.format(
              "Label scan completed for %s with %d safety concerns",
              matchedProductName != null ? matchedProductName : "product",
              (allergenHits != null ? allergenHits.size() : 0)
                  + (forbiddenHits != null ? forbiddenHits.size() : 0)
                  + (warnings != null ? warnings.size() : 0)),
          Map.of(
              "scanStatus",
              scanStatus,
              "matchedProductName",
              matchedProductName,
              "allergenHits",
              allergenHits,
              "forbiddenHits",
              forbiddenHits,
              "warnings",
              warnings,
              "hasSafetyConcerns",
              hasSafetyConcerns),
          hasSafetyConcerns ? "high" : "normal");
      this.scanStatus = scanStatus;
      this.matchedProductName = matchedProductName;
      this.allergenHits = allergenHits != null ? List.copyOf(allergenHits) : List.of();
      this.forbiddenHits = forbiddenHits != null ? List.copyOf(forbiddenHits) : List.of();
      this.warnings = warnings != null ? List.copyOf(warnings) : List.of();
      this.hasSafetyConcerns = hasSafetyConcerns;
    }

    public String getScanStatus() {
      return scanStatus;
    }

    public String getMatchedProductName() {
      return matchedProductName;
    }

    public List<String> getAllergenHits() {
      return allergenHits;
    }

    public List<String> getForbiddenHits() {
      return forbiddenHits;
    }

    public List<String> getWarnings() {
      return warnings;
    }

    public boolean isHasSafetyConcerns() {
      return hasSafetyConcerns;
    }

    public int getTotalSafetyConcerns() {
      return allergenHits.size() + forbiddenHits.size() + warnings.size();
    }
  }
}
