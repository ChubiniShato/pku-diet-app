package com.chubini.pku.validation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chubini.pku.menus.MenuDay;
import com.chubini.pku.patients.PatientProfile;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "critical_fact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriticalFact {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private PatientProfile patient;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "menu_day_id", nullable = false)
  private MenuDay menuDay;

  @Enumerated(EnumType.STRING)
  @Column(name = "breach_type", nullable = false)
  private BreachType breachType;

  @Column(name = "delta_value", nullable = false, precision = 10, scale = 2)
  private BigDecimal deltaValue;

  @Column(name = "limit_value", precision = 10, scale = 2)
  private BigDecimal limitValue;

  @Column(name = "actual_value", precision = 10, scale = 2)
  private BigDecimal actualValue;

  @Column(name = "context_type", nullable = false)
  private String contextType; // "planned" or "consumed"

  @Column(name = "description", nullable = false)
  private String description;

  @Column(name = "severity", nullable = false)
  @Enumerated(EnumType.STRING)
  private Severity severity;

  @Column(name = "resolved", nullable = false)
  @Builder.Default
  private Boolean resolved = false;

  @Column(name = "resolved_at")
  private LocalDateTime resolvedAt;

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

  public enum BreachType {
    PHE_EXCEEDED("Phenylalanine limit exceeded"),
    PROTEIN_EXCEEDED("Natural protein limit exceeded"),
    KCAL_DEFICIT("Calorie requirement not met"),
    FAT_EXCEEDED("Fat limit exceeded");

    private final String description;

    BreachType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
  }

  /** Create a critical fact for PHE breach */
  public static CriticalFact createPheBreach(
      PatientProfile patient,
      MenuDay menuDay,
      BigDecimal delta,
      BigDecimal limit,
      BigDecimal actual,
      String contextType) {
    return CriticalFact.builder()
        .patient(patient)
        .menuDay(menuDay)
        .breachType(BreachType.PHE_EXCEEDED)
        .deltaValue(delta)
        .limitValue(limit)
        .actualValue(actual)
        .contextType(contextType)
        .description(
            String.format(
                "PHE %s exceeded limit by %.2f mg (%.2f/%.2f mg)",
                contextType, delta, actual, limit))
        .severity(determineSeverity(delta, limit))
        .build();
  }

  /** Create a critical fact for protein breach */
  public static CriticalFact createProteinBreach(
      PatientProfile patient,
      MenuDay menuDay,
      BigDecimal delta,
      BigDecimal limit,
      BigDecimal actual,
      String contextType) {
    return CriticalFact.builder()
        .patient(patient)
        .menuDay(menuDay)
        .breachType(BreachType.PROTEIN_EXCEEDED)
        .deltaValue(delta)
        .limitValue(limit)
        .actualValue(actual)
        .contextType(contextType)
        .description(
            String.format(
                "Natural protein %s exceeded limit by %.2f g (%.2f/%.2f g)",
                contextType, delta, actual, limit))
        .severity(determineSeverity(delta, limit))
        .build();
  }

  /** Create a critical fact for calorie deficit */
  public static CriticalFact createKcalDeficit(
      PatientProfile patient,
      MenuDay menuDay,
      BigDecimal deficit,
      BigDecimal minimum,
      BigDecimal actual,
      String contextType) {
    return CriticalFact.builder()
        .patient(patient)
        .menuDay(menuDay)
        .breachType(BreachType.KCAL_DEFICIT)
        .deltaValue(deficit.abs()) // Store as positive value
        .limitValue(minimum)
        .actualValue(actual)
        .contextType(contextType)
        .description(
            String.format(
                "Calorie %s below minimum by %.0f kcal (%.0f/%.0f kcal)",
                contextType, deficit.abs(), actual, minimum))
        .severity(determineSeverity(deficit.abs(), minimum))
        .build();
  }

  /** Create a critical fact for fat excess (if configured as hard limit) */
  public static CriticalFact createFatBreach(
      PatientProfile patient,
      MenuDay menuDay,
      BigDecimal delta,
      BigDecimal limit,
      BigDecimal actual,
      String contextType) {
    return CriticalFact.builder()
        .patient(patient)
        .menuDay(menuDay)
        .breachType(BreachType.FAT_EXCEEDED)
        .deltaValue(delta)
        .limitValue(limit)
        .actualValue(actual)
        .contextType(contextType)
        .description(
            String.format(
                "Fat %s exceeded limit by %.2f g (%.2f/%.2f g)", contextType, delta, actual, limit))
        .severity(determineSeverity(delta, limit))
        .build();
  }

  /** Determine severity based on the percentage of breach */
  private static Severity determineSeverity(BigDecimal delta, BigDecimal limit) {
    if (limit == null || limit.compareTo(BigDecimal.ZERO) == 0) {
      return Severity.MEDIUM;
    }

    BigDecimal percentage =
        delta.divide(limit, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

    if (percentage.compareTo(BigDecimal.valueOf(50)) > 0) {
      return Severity.CRITICAL;
    } else if (percentage.compareTo(BigDecimal.valueOf(25)) > 0) {
      return Severity.HIGH;
    } else if (percentage.compareTo(BigDecimal.valueOf(10)) > 0) {
      return Severity.MEDIUM;
    } else {
      return Severity.LOW;
    }
  }

  /** Mark this critical fact as resolved */
  public void resolve() {
    this.resolved = true;
    this.resolvedAt = LocalDateTime.now();
  }
}
