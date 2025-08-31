package com.chubini.pku.consents;

import java.time.LocalDateTime;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

/** Patient consent entity for tracking explicit permissions for data sharing and submissions */
@Entity
@Table(
    name = "patient_consent",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"patient_id", "consent_type", "version"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientConsent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private PatientProfile patient;

  @Enumerated(EnumType.STRING)
  @Column(name = "consent_type", nullable = false)
  private ConsentType consentType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ConsentStatus status;

  @Column(name = "version", nullable = false)
  private Integer version;

  @Column(name = "granted_reason", columnDefinition = "TEXT")
  private String grantedReason;

  @Column(name = "revoked_reason", columnDefinition = "TEXT")
  private String revokedReason;

  @Column(name = "granted_at", nullable = false)
  private LocalDateTime grantedAt;

  @Column(name = "revoked_at")
  private LocalDateTime revokedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Column(name = "granted_by", columnDefinition = "TEXT")
  private String grantedBy; // User/Device identifier

  @Column(name = "revoked_by", columnDefinition = "TEXT")
  private String revokedBy; // User/Device identifier

  /** Consent types supported by the system */
  public enum ConsentType {
    GLOBAL_SUBMISSION_OPTIN, // Required for any data submissions
    SHARE_WITH_DOCTOR, // Required for creating share links
    EMERGENCY_ACCESS, // Emergency medical access
    RESEARCH_OPTIN // Research data usage
  }

  /** Consent status states */
  public enum ConsentStatus {
    GRANTED,
    REVOKED,
    EXPIRED
  }

  /** Check if consent is currently active */
  public boolean isActive() {
    return status == ConsentStatus.GRANTED
        && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
  }

  /** Check if consent has expired */
  public boolean isExpired() {
    return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
  }

  /** Get the latest version for this consent type and patient */
  public static Integer getNextVersion(Integer currentVersion) {
    return currentVersion == null ? 1 : currentVersion + 1;
  }

  /** Create a new consent record */
  public static PatientConsent createNew(
      PatientProfile patient,
      ConsentType type,
      String reason,
      LocalDateTime expiresAt,
      String grantedBy) {
    return PatientConsent.builder()
        .patient(patient)
        .consentType(type)
        .status(ConsentStatus.GRANTED)
        .version(1)
        .grantedReason(reason)
        .grantedAt(LocalDateTime.now())
        .expiresAt(expiresAt)
        .grantedBy(grantedBy)
        .build();
  }
}
