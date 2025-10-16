package com.chubini.pku.products;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "custom_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private PatientProfile patient;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "category")
  private String category;

  @Column(name = "standard_serving_grams", precision = 8, scale = 2)
  private BigDecimal standardServingGrams;

  @Column(name = "manual_serving_override", nullable = false)
  @Builder.Default
  private Boolean manualServingOverride = false;

  @Column(name = "phenylalanine", precision = 8, scale = 2)
  private BigDecimal phenylalanine;

  @Column(name = "leucine", precision = 8, scale = 2)
  private BigDecimal leucine;

  @Column(name = "tyrosine", precision = 8, scale = 2)
  private BigDecimal tyrosine;

  @Column(name = "methionine", precision = 8, scale = 2)
  private BigDecimal methionine;

  @Column(name = "kilojoules", precision = 8, scale = 2)
  private BigDecimal kilojoules;

  @Column(name = "kilocalories", precision = 8, scale = 2)
  private BigDecimal kilocalories;

  @Column(name = "protein", precision = 8, scale = 2)
  private BigDecimal protein;

  @Column(name = "carbohydrates", precision = 8, scale = 2)
  private BigDecimal carbohydrates;

  @Column(name = "fats", precision = 8, scale = 2)
  private BigDecimal fats;

  @Column(name = "is_visible", nullable = false)
  @Builder.Default
  private Boolean isVisible = true;

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
