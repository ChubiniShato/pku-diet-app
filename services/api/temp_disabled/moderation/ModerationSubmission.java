package com.chubini.pku.moderation;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

/** ModerationSubmission entity for global catalog submissions */
@Entity
@Table(
    name = "moderation_submission",
    indexes = {
      @Index(name = "idx_moderation_patient", columnList = "patient_id"),
      @Index(name = "idx_moderation_status", columnList = "status"),
      @Index(name = "idx_moderation_type", columnList = "target_type"),
      @Index(name = "idx_moderation_created", columnList = "created_at")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationSubmission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private PatientProfile patient;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false)
  private TargetType targetType;

  @Column(name = "payload_ref", nullable = false)
  private String payloadRef; // Reference to the actual data (e.g., custom_product:uuid)

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private ModerationStatus status = ModerationStatus.DRAFT;

  @Column(name = "submission_title")
  private String submissionTitle; // User-provided title for the submission

  @Column(name = "submission_description", columnDefinition = "TEXT")
  private String submissionDescription; // User-provided description

  @ElementCollection
  @CollectionTable(name = "moderation_conflicts", joinColumns = @JoinColumn(name = "submission_id"))
  @Column(name = "conflict_description")
  private java.util.List<String> conflicts; // Detected conflicts (duplicates, etc.)

  @ElementCollection
  @CollectionTable(
      name = "moderation_merge_data",
      joinColumns = @JoinColumn(name = "submission_id"))
  @MapKeyColumn(name = "field_path")
  @Column(name = "field_value")
  private Map<String, String> mergeData; // Proposed field changes for merge

  @Column(name = "review_notes", columnDefinition = "TEXT")
  private String reviewNotes; // Reviewer's notes

  @Column(name = "reviewer_id")
  private String reviewerId; // ID of the reviewer who processed this

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @Column(name = "approved_product_id")
  private UUID approvedProductId; // ID of the product created/modified in global catalog

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt; // Submissions expire if not reviewed

  /** Target type enumeration */
  public enum TargetType {
    CUSTOM_PRODUCT,
    CUSTOM_DISH,
    LABEL_SCAN
  }

  /** Moderation status enumeration */
  public enum ModerationStatus {
    DRAFT, // Patient is editing, not yet submitted
    PENDING, // Submitted and waiting for review
    UNDER_REVIEW, // Currently being reviewed
    APPROVED, // Approved and merged to global catalog
    REJECTED, // Rejected by reviewer
    EXPIRED // Expired without review
  }

  /** Check if submission is in a final state */
  public boolean isFinal() {
    return status == ModerationStatus.APPROVED
        || status == ModerationStatus.REJECTED
        || status == ModerationStatus.EXPIRED;
  }

  /** Check if submission is expired */
  public boolean isExpired() {
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
  }

  /** Check if submission has conflicts */
  public boolean hasConflicts() {
    return conflicts != null && !conflicts.isEmpty();
  }

  /** Mark as under review */
  public void markUnderReview(String reviewerId) {
    this.status = ModerationStatus.UNDER_REVIEW;
    this.reviewerId = reviewerId;
  }

  /** Mark as approved */
  public void markApproved(String reviewerId, String notes, UUID approvedProductId) {
    this.status = ModerationStatus.APPROVED;
    this.reviewerId = reviewerId;
    this.reviewNotes = notes;
    this.approvedProductId = approvedProductId;
    this.reviewedAt = LocalDateTime.now();
  }

  /** Mark as rejected */
  public void markRejected(String reviewerId, String notes) {
    this.status = ModerationStatus.REJECTED;
    this.reviewerId = reviewerId;
    this.reviewNotes = notes;
    this.reviewedAt = LocalDateTime.now();
  }

  /** Mark as expired */
  public void markExpired() {
    this.status = ModerationStatus.EXPIRED;
  }

  /** Add a conflict */
  public void addConflict(String conflict) {
    if (this.conflicts == null) {
      this.conflicts = new java.util.ArrayList<>();
    }
    if (!this.conflicts.contains(conflict)) {
      this.conflicts.add(conflict);
    }
  }

  /** Set merge data for a field */
  public void setMergeField(String fieldPath, String value) {
    if (this.mergeData == null) {
      this.mergeData = new java.util.HashMap<>();
    }
    this.mergeData.put(fieldPath, value);
  }

  /** Get the entity ID from payload reference */
  public UUID getEntityId() {
    if (payloadRef == null) return null;
    String[] parts = payloadRef.split(":");
    if (parts.length == 2) {
      try {
        return UUID.fromString(parts[1]);
      } catch (IllegalArgumentException e) {
        return null;
      }
    }
    return null;
  }

  /** Get the entity type from payload reference */
  public String getEntityType() {
    if (payloadRef == null) return null;
    String[] parts = payloadRef.split(":");
    if (parts.length >= 1) {
      return parts[0];
    }
    return null;
  }
}
