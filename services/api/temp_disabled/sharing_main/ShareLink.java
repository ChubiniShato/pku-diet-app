package com.chubini.pku.sharing;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

/** ShareLink entity for secure doctor-patient data sharing */
@Entity
@Table(
    name = "share_link",
    indexes = {
      @Index(name = "idx_share_link_token", columnList = "token"),
      @Index(name = "idx_share_link_patient", columnList = "patient_id"),
      @Index(name = "idx_share_link_doctor_email", columnList = "doctor_email")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareLink {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private PatientProfile patient;

  @Column(name = "token", nullable = false, unique = true, length = 128)
  private String token;

  @Column(name = "doctor_email")
  private String doctorEmail;

  @Column(name = "doctor_name")
  private String doctorName;

  @ElementCollection
  @CollectionTable(name = "share_link_scopes", joinColumns = @JoinColumn(name = "share_link_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "scope")
  private Set<ShareScope> scopes;

  @Column(name = "one_time_use", nullable = false)
  @Builder.Default
  private Boolean oneTimeUse = true;

  @Column(name = "device_bound")
  private String deviceBound; // Device fingerprint for additional security

  @Column(name = "ttl_hours", nullable = false)
  @Builder.Default
  private Integer ttlHours = 48;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private ShareLinkStatus status = ShareLinkStatus.ACTIVE;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "revoked_at")
  private LocalDateTime revokedAt;

  @Column(name = "first_used_at")
  private LocalDateTime firstUsedAt;

  @Column(name = "last_used_at")
  private LocalDateTime lastUsedAt;

  @Column(name = "usage_count", nullable = false)
  @Builder.Default
  private Integer usageCount = 0;

  @Column(name = "created_by", columnDefinition = "TEXT")
  private String createdBy; // Patient device/user identifier

  @Column(name = "revoked_by", columnDefinition = "TEXT")
  private String revokedBy;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /** Share scope definitions */
  public enum ShareScope {
    CRITICAL_FACTS, // Only critical breaches and alerts
    DAY, // Single day menu data
    WEEK, // Weekly menu data
    RANGE, // Date range of menu data
    NUTRITION_SUMMARY // Overall nutrition summaries
  }

  /** Share link status */
  public enum ShareLinkStatus {
    ACTIVE,
    USED, // For one-time links that have been used
    REVOKED,
    EXPIRED
  }

  /** Check if link is currently usable */
  public boolean isUsable() {
    return status == ShareLinkStatus.ACTIVE && !isExpired() && (!oneTimeUse || usageCount == 0);
  }

  /** Check if link has expired */
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }

  /** Check if scope is allowed for this link */
  public boolean allowsScope(ShareScope scope) {
    return scopes != null && scopes.contains(scope);
  }

  /** Record usage of the link */
  public void recordUsage() {
    this.usageCount++;
    this.lastUsedAt = LocalDateTime.now();

    if (this.firstUsedAt == null) {
      this.firstUsedAt = LocalDateTime.now();
    }

    if (oneTimeUse && usageCount >= 1) {
      this.status = ShareLinkStatus.USED;
    }
  }

  /** Revoke the link */
  public void revoke(String revokedBy) {
    this.status = ShareLinkStatus.REVOKED;
    this.revokedAt = LocalDateTime.now();
    this.revokedBy = revokedBy;
  }

  /** Generate a secure random token */
  public static String generateSecureToken() {
    return UUID.randomUUID().toString() + UUID.randomUUID().toString().replace("-", "");
  }

  /** Create a new share link */
  public static ShareLink createNew(
      PatientProfile patient,
      Set<ShareScope> scopes,
      String doctorEmail,
      String doctorName,
      Integer ttlHours,
      Boolean oneTimeUse,
      String deviceBound,
      String createdBy) {

    LocalDateTime expiresAt = LocalDateTime.now().plusHours(ttlHours != null ? ttlHours : 48);

    return ShareLink.builder()
        .patient(patient)
        .token(generateSecureToken())
        .doctorEmail(doctorEmail)
        .doctorName(doctorName)
        .scopes(scopes)
        .oneTimeUse(oneTimeUse != null ? oneTimeUse : true)
        .deviceBound(deviceBound)
        .ttlHours(ttlHours != null ? ttlHours : 48)
        .expiresAt(expiresAt)
        .createdBy(createdBy)
        .createdAt(LocalDateTime.now())
        .build();
  }

  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    if (expiresAt == null && ttlHours != null) {
      expiresAt = createdAt.plusHours(ttlHours);
    }
  }
}
