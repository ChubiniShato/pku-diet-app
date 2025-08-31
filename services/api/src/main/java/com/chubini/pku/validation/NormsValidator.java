package com.chubini.pku.validation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chubini.pku.menus.MenuDay;
import com.chubini.pku.norms.NormPrescription;
import com.chubini.pku.validation.dto.ValidationResult;
import com.chubini.pku.validation.dto.ValidationResult.ValidationLevel;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NormsValidator {

  private final NutritionCalculator nutritionCalculator;

  /** Validate a menu day against norm prescription */
  public ValidationResult validate(NormPrescription norm, MenuDay menuDay) {
    if (norm == null || menuDay == null) {
      log.debug(
          "Validation skipped - null inputs: norm={}, menuDay={}", norm != null, menuDay != null);
      return ValidationResult.ok();
    }

    log.debug("Validating menu day {} against norm prescription {}", menuDay.getId(), norm.getId());

    // Calculate planned and consumed totals
    NutritionCalculator.DayTotals planned = nutritionCalculator.calculatePlannedTotals(menuDay);
    NutritionCalculator.DayTotals consumed = nutritionCalculator.calculateConsumedTotals(menuDay);

    // Validate both planned and consumed values
    ValidationResult plannedResult = validateTotals(planned, norm, "planned");
    ValidationResult consumedResult = validateTotals(consumed, norm, "consumed");

    // Combine results - highest severity wins
    ValidationLevel level = getHighestSeverity(plannedResult.level(), consumedResult.level());

    Map<String, BigDecimal> combinedDeltas = new HashMap<>();
    combinedDeltas.putAll(plannedResult.deltas());
    combinedDeltas.putAll(consumedResult.deltas());

    List<String> combinedMessages = new ArrayList<>();
    combinedMessages.addAll(plannedResult.messages());
    combinedMessages.addAll(consumedResult.messages());

    List<String> combinedSuggestions = new ArrayList<>();
    combinedSuggestions.addAll(plannedResult.suggestions());
    combinedSuggestions.addAll(consumedResult.suggestions());

    return new ValidationResult(level, combinedDeltas, combinedMessages, combinedSuggestions);
  }

  /** Validate nutrition totals against norms */
  private ValidationResult validateTotals(
      NutritionCalculator.DayTotals totals, NormPrescription norm, String type) {
    Map<String, BigDecimal> deltas = new HashMap<>();
    List<String> messages = new ArrayList<>();
    List<String> suggestions = new ArrayList<>();
    ValidationLevel level = ValidationLevel.OK;

    // PHE validation (upper limit - BREACH if exceeded)
    if (norm.getPheLimitMgPerDay() != null && totals.pheMg() != null) {
      BigDecimal delta = totals.pheMg().subtract(norm.getPheLimitMgPerDay());
      deltas.put("phe", delta);

      if (delta.compareTo(BigDecimal.ZERO) > 0) {
        level = ValidationLevel.BREACH;
        messages.add(
            String.format(
                "PHE %s exceeds daily limit by %.2f mg (%.2f/%.2f mg)",
                type, delta, totals.pheMg(), norm.getPheLimitMgPerDay()));
        suggestions.add("Consider reducing high-PHE foods or replacing with low-PHE alternatives");
      }
    }

    // Protein validation (upper limit - BREACH if exceeded)
    if (norm.getProteinLimitGPerDay() != null && totals.proteinG() != null) {
      BigDecimal delta = totals.proteinG().subtract(norm.getProteinLimitGPerDay());
      deltas.put("protein", delta);

      if (delta.compareTo(BigDecimal.ZERO) > 0) {
        level = ValidationLevel.BREACH;
        messages.add(
            String.format(
                "Natural protein %s exceeds daily limit by %.2f g (%.2f/%.2f g)",
                type, delta, totals.proteinG(), norm.getProteinLimitGPerDay()));
        suggestions.add("Consider reducing natural protein sources");
      }
    }

    // Calorie validation (lower limit - BREACH if below, but ±5% tolerance for display)
    if (norm.getKcalMinPerDay() != null && totals.kcal() != null) {
      BigDecimal kcalBD = BigDecimal.valueOf(totals.kcal());
      BigDecimal delta = kcalBD.subtract(norm.getKcalMinPerDay()); // negative if below minimum
      deltas.put("kcal", delta);

      if (delta.compareTo(BigDecimal.ZERO) < 0) {
        level = ValidationLevel.BREACH;
        messages.add(
            String.format(
                "Calories %s below minimum requirement by %.0f kcal (%d/%.0f kcal)",
                type, delta.abs(), totals.kcal(), norm.getKcalMinPerDay()));
        suggestions.add("Add calorie-dense, low-PHE foods to meet energy requirements");
      }
    }

    // Fat validation (warning threshold + optional hard limit)
    if (norm.getFatLimitGPerDay() != null && totals.fatG() != null) {
      BigDecimal delta = totals.fatG().subtract(norm.getFatLimitGPerDay());
      deltas.put("fat", delta);

      if (delta.compareTo(BigDecimal.ZERO) > 0) {
        // For now, treat fat limit as warning threshold
        // In future, could add separate fatMaxG for hard limit
        if (level == ValidationLevel.OK) {
          level = ValidationLevel.WARN;
        }
        messages.add(
            String.format(
                "Fat %s exceeds recommended limit by %.2f g (%.2f/%.2f g)",
                type, delta, totals.fatG(), norm.getFatLimitGPerDay()));
        suggestions.add("Consider reducing high-fat foods or using lower-fat alternatives");
      }
    }

    return new ValidationResult(level, deltas, messages, suggestions);
  }

  /** Get the highest severity level between two validation levels */
  private ValidationLevel getHighestSeverity(ValidationLevel level1, ValidationLevel level2) {
    if (level1 == ValidationLevel.BREACH || level2 == ValidationLevel.BREACH) {
      return ValidationLevel.BREACH;
    }
    if (level1 == ValidationLevel.WARN || level2 == ValidationLevel.WARN) {
      return ValidationLevel.WARN;
    }
    return ValidationLevel.OK;
  }

  /** Calculate percentage of daily limits used for progress tracking */
  public ValidationResult calculateProgress(NormPrescription norm, MenuDay menuDay) {
    log.debug(
        "Calculating progress for menu day {} against norm {}", menuDay.getId(), norm.getId());

    NutritionCalculator.DayTotals totals = nutritionCalculator.calculatePlannedTotals(menuDay);

    Map<String, BigDecimal> deltas = new HashMap<>();
    List<String> messages = new ArrayList<>();

    // PHE progress
    if (norm.getPheLimitMgPerDay() != null && totals.pheMg() != null) {
      BigDecimal percentage =
          totals
              .pheMg()
              .divide(norm.getPheLimitMgPerDay(), 4, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100));

      deltas.put("phe_percentage", percentage);
      messages.add(
          String.format(
              "PHE: %.1f%% of daily limit used (%.2f/%.2f mg)",
              percentage, totals.pheMg(), norm.getPheLimitMgPerDay()));
    }

    // Protein progress
    if (norm.getProteinLimitGPerDay() != null && totals.proteinG() != null) {
      BigDecimal percentage =
          totals
              .proteinG()
              .divide(norm.getProteinLimitGPerDay(), 4, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100));

      deltas.put("protein_percentage", percentage);
      messages.add(
          String.format(
              "Protein: %.1f%% of daily limit used (%.2f/%.2f g)",
              percentage, totals.proteinG(), norm.getProteinLimitGPerDay()));
    }

    // Calorie progress
    if (norm.getKcalMinPerDay() != null && totals.kcal() != null) {
      BigDecimal kcalBD = BigDecimal.valueOf(totals.kcal());
      BigDecimal percentage =
          kcalBD
              .divide(norm.getKcalMinPerDay(), 4, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100));

      deltas.put("kcal_percentage", percentage);
      messages.add(
          String.format(
              "Calories: %.1f%% of minimum requirement met (%d/%.0f kcal)",
              percentage, totals.kcal(), norm.getKcalMinPerDay()));
    }

    return new ValidationResult(ValidationLevel.OK, deltas, messages, List.of());
  }
}
