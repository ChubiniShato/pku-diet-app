package com.chubini.pku.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.chubini.pku.menus.MenuDay;
import com.chubini.pku.norms.NormPrescription;
import com.chubini.pku.validation.dto.ValidationResult;
import com.chubini.pku.validation.events.LimitBreachEvent;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CriticalFactService {

  private final CriticalFactRepository criticalFactRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final NutritionCalculator nutritionCalculator;

  /** Process validation result and create critical facts for breaches */
  @Transactional
  public List<CriticalFact> processBreach(
      ValidationResult result, NormPrescription norm, MenuDay menuDay) {
    if (result == null || !result.hasBreaches() || menuDay == null || norm == null) {
      return List.of();
    }

    log.debug(
        "Processing breaches for menu day {} with {} deltas",
        menuDay.getId(),
        result.deltas().size());

    List<CriticalFact> criticalFacts = new ArrayList<>();

    // Calculate current totals for context
    NutritionCalculator.DayTotals planned = nutritionCalculator.calculatePlannedTotals(menuDay);
    NutritionCalculator.DayTotals consumed = nutritionCalculator.calculateConsumedTotals(menuDay);

    // Process each delta that represents a breach
    for (Map.Entry<String, BigDecimal> entry : result.deltas().entrySet()) {
      String nutrientType = entry.getKey();
      BigDecimal delta = entry.getValue();

      // Only process positive deltas (breaches) or negative kcal deltas (deficits)
      if (shouldCreateCriticalFact(nutrientType, delta)) {
        CriticalFact criticalFact =
            createCriticalFact(nutrientType, delta, norm, menuDay, planned, consumed);

        if (criticalFact != null) {
          criticalFacts.add(criticalFactRepository.save(criticalFact));

          // Publish breach event
          publishBreachEvent(criticalFact);
        }
      }
    }

    log.info("Created {} critical facts for menu day {}", criticalFacts.size(), menuDay.getId());
    return criticalFacts;
  }

  /** Determine if a critical fact should be created based on nutrient type and delta */
  private boolean shouldCreateCriticalFact(String nutrientType, BigDecimal delta) {
    return switch (nutrientType) {
      case "phe", "protein", "fat" -> delta.compareTo(BigDecimal.ZERO) > 0; // Positive breach
      case "kcal" -> delta.compareTo(BigDecimal.ZERO) < 0; // Negative deficit
      default -> false;
    };
  }

  /** Create a critical fact based on nutrient type and values */
  private CriticalFact createCriticalFact(
      String nutrientType,
      BigDecimal delta,
      NormPrescription norm,
      MenuDay menuDay,
      NutritionCalculator.DayTotals planned,
      NutritionCalculator.DayTotals consumed) {

    return switch (nutrientType) {
      case "phe" -> createPheCriticalFact(delta, norm, menuDay, planned, consumed);
      case "protein" -> createProteinCriticalFact(delta, norm, menuDay, planned, consumed);
      case "kcal" -> createKcalCriticalFact(delta, norm, menuDay, planned, consumed);
      case "fat" -> createFatCriticalFact(delta, norm, menuDay, planned, consumed);
      default -> null;
    };
  }

  private CriticalFact createPheCriticalFact(
      BigDecimal delta,
      NormPrescription norm,
      MenuDay menuDay,
      NutritionCalculator.DayTotals planned,
      NutritionCalculator.DayTotals consumed) {
    // Determine which context (planned vs consumed) has the breach
    String contextType = "planned";
    BigDecimal actualValue = planned.pheMg();

    if (consumed.pheMg().compareTo(planned.pheMg()) > 0) {
      contextType = "consumed";
      actualValue = consumed.pheMg();
    }

    return CriticalFact.createPheBreach(
        menuDay.getPatient(), menuDay, delta, norm.getPheLimitMgPerDay(), actualValue, contextType);
  }

  private CriticalFact createProteinCriticalFact(
      BigDecimal delta,
      NormPrescription norm,
      MenuDay menuDay,
      NutritionCalculator.DayTotals planned,
      NutritionCalculator.DayTotals consumed) {
    String contextType = "planned";
    BigDecimal actualValue = planned.proteinG();

    if (consumed.proteinG().compareTo(planned.proteinG()) > 0) {
      contextType = "consumed";
      actualValue = consumed.proteinG();
    }

    return CriticalFact.createProteinBreach(
        menuDay.getPatient(),
        menuDay,
        delta,
        norm.getProteinLimitGPerDay(),
        actualValue,
        contextType);
  }

  private CriticalFact createKcalCriticalFact(
      BigDecimal delta,
      NormPrescription norm,
      MenuDay menuDay,
      NutritionCalculator.DayTotals planned,
      NutritionCalculator.DayTotals consumed) {
    String contextType = "planned";
    BigDecimal actualValue = BigDecimal.valueOf(planned.kcal());

    if (consumed.kcal() < planned.kcal()) {
      contextType = "consumed";
      actualValue = BigDecimal.valueOf(consumed.kcal());
    }

    return CriticalFact.createKcalDeficit(
        menuDay.getPatient(), menuDay, delta, norm.getKcalMinPerDay(), actualValue, contextType);
  }

  private CriticalFact createFatCriticalFact(
      BigDecimal delta,
      NormPrescription norm,
      MenuDay menuDay,
      NutritionCalculator.DayTotals planned,
      NutritionCalculator.DayTotals consumed) {
    String contextType = "planned";
    BigDecimal actualValue = planned.fatG();

    if (consumed.fatG().compareTo(planned.fatG()) > 0) {
      contextType = "consumed";
      actualValue = consumed.fatG();
    }

    return CriticalFact.createFatBreach(
        menuDay.getPatient(), menuDay, delta, norm.getFatLimitGPerDay(), actualValue, contextType);
  }

  /** Publish a breach event for external handling */
  private void publishBreachEvent(CriticalFact criticalFact) {
    LimitBreachEvent event =
        new LimitBreachEvent(
            criticalFact.getMenuDay().getId(),
            criticalFact.getBreachType().name(),
            criticalFact.getDeltaValue(),
            criticalFact.getSeverity(),
            criticalFact.getContextType(),
            criticalFact.getDescription());

    log.debug("Publishing breach event: {}", event);
    eventPublisher.publishEvent(event);
  }

  /** Get all unresolved critical facts for a patient */
  public List<CriticalFact> getUnresolvedFacts(UUID patientId) {
    // This would need PatientProfileRepository to fetch the patient
    // For now, return empty list - to be implemented when needed
    return List.of();
  }

  /** Get critical facts for a specific menu day */
  public List<CriticalFact> getFactsForDay(MenuDay menuDay) {
    return criticalFactRepository.findByMenuDayOrderByCreatedAtDesc(menuDay);
  }

  /** Resolve a critical fact */
  @Transactional
  public void resolveFact(UUID factId) {
    criticalFactRepository
        .findById(factId)
        .ifPresent(
            fact -> {
              fact.resolve();
              criticalFactRepository.save(fact);
              log.info("Resolved critical fact: {}", factId);
            });
  }

  /** Get critical facts count by severity for a patient */
  public long getFactCountBySeverity(UUID patientId, CriticalFact.Severity severity) {
    // This would need PatientProfileRepository to fetch the patient
    // For now, return 0 - to be implemented when needed
    return 0;
  }
}
