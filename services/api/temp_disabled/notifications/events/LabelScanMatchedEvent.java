package com.chubini.pku.notifications.events;

import java.util.List;
import java.util.UUID;

/** Event published when a label scan is completed with safety analysis results */
public class LabelScanMatchedEvent {

  private final UUID submissionId;
  private final UUID patientId;
  private final String status;
  private final String matchedProductName;
  private final Double matchConfidence;
  private final List<String> allergenHits;
  private final List<String> forbiddenHits;
  private final List<String> warnings;
  private final boolean hasSafetyConcerns;
  private final java.time.LocalDateTime timestamp;

  public LabelScanMatchedEvent(
      UUID submissionId,
      UUID patientId,
      String status,
      String matchedProductName,
      Double matchConfidence,
      List<String> allergenHits,
      List<String> forbiddenHits,
      List<String> warnings,
      boolean hasSafetyConcerns) {
    this.submissionId = submissionId;
    this.patientId = patientId;
    this.status = status;
    this.matchedProductName = matchedProductName;
    this.matchConfidence = matchConfidence;
    this.allergenHits = allergenHits != null ? List.copyOf(allergenHits) : List.of();
    this.forbiddenHits = forbiddenHits != null ? List.copyOf(forbiddenHits) : List.of();
    this.warnings = warnings != null ? List.copyOf(warnings) : List.of();
    this.hasSafetyConcerns = hasSafetyConcerns;
    this.timestamp = java.time.LocalDateTime.now();
  }

  public UUID getSubmissionId() {
    return submissionId;
  }

  public UUID getPatientId() {
    return patientId;
  }

  public String getStatus() {
    return status;
  }

  public String getMatchedProductName() {
    return matchedProductName;
  }

  public Double getMatchConfidence() {
    return matchConfidence;
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

  public java.time.LocalDateTime getTimestamp() {
    return timestamp;
  }

  public int getTotalSafetyConcerns() {
    return allergenHits.size() + forbiddenHits.size() + warnings.size();
  }

  @Override
  public String toString() {
    return String.format(
        "LabelScanMatchedEvent{submissionId='%s', patientId='%s', status='%s', hasSafetyConcerns=%s, totalConcerns=%d}",
        submissionId, patientId, status, hasSafetyConcerns, getTotalSafetyConcerns());
  }
}
