package com.chubini.pku.patients;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patient_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "weight_kg", precision = 5, scale = 2)
  private BigDecimal weightKg;

  @Column(name = "height_cm", precision = 5, scale = 2)
  private BigDecimal heightCm;

  @Enumerated(EnumType.STRING)
  @Column(name = "activity_level")
  private ActivityLevel activityLevel;

  @Column(name = "region")
  private String region;

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

  public enum ActivityLevel {
    LOW,
    MODERATE,
    HIGH
  }
}
