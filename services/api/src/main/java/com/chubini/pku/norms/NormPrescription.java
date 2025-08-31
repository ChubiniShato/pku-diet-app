package com.chubini.pku.norms;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "norm_prescription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormPrescription {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private PatientProfile patient;

  @Column(name = "phe_limit_mg_per_day", nullable = false, precision = 8, scale = 2)
  private BigDecimal pheLimitMgPerDay;

  @Column(name = "protein_limit_g_per_day", precision = 8, scale = 2)
  private BigDecimal proteinLimitGPerDay;

  @Column(name = "kcal_min_per_day", precision = 8, scale = 2)
  private BigDecimal kcalMinPerDay;

  @Column(name = "fat_limit_g_per_day", precision = 8, scale = 2)
  private BigDecimal fatLimitGPerDay;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "prescribed_date", nullable = false)
  @Builder.Default
  private LocalDate prescribedDate = LocalDate.now();

  @Column(name = "notes")
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
