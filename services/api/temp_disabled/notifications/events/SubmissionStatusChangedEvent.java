package com.chubini.pku.notifications.events;

import java.time.LocalDateTime;

/** Event published when a moderation submission status changes */
public class SubmissionStatusChangedEvent {

  private final String submissionId;
  private final String oldStatus;
  private final String newStatus;
  private final String targetType;
  private final String submissionTitle;
  private final String reviewerNotes;
  private final String reviewerId;
  private final LocalDateTime timestamp;

  public SubmissionStatusChangedEvent(
      String submissionId,
      String oldStatus,
      String newStatus,
      String targetType,
      String submissionTitle,
      String reviewerNotes,
      String reviewerId) {
    this.submissionId = submissionId;
    this.oldStatus = oldStatus;
    this.newStatus = newStatus;
    this.targetType = targetType;
    this.submissionTitle = submissionTitle;
    this.reviewerNotes = reviewerNotes;
    this.reviewerId = reviewerId;
    this.timestamp = LocalDateTime.now();
  }

  public String getSubmissionId() {
    return submissionId;
  }

  public String getOldStatus() {
    return oldStatus;
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

  public String getReviewerId() {
    return reviewerId;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return String.format(
        "SubmissionStatusChangedEvent{submissionId='%s', oldStatus='%s', newStatus='%s', targetType='%s'}",
        submissionId, oldStatus, newStatus, targetType);
  }
}
