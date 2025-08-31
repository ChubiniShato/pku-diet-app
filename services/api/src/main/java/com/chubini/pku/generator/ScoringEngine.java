package com.chubini.pku.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.chubini.pku.generator.dto.FoodCandidate;
import com.chubini.pku.menus.MealSlot;
import com.chubini.pku.norms.dto.NormPrescriptionDto;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Scoring engine for menu generation candidates Score = w1*PheOver + w2*ProteinOver +
 * w3*KcalDeficit + w4*Cost + w5*RepeatPenalty
 */
@Component
@Slf4j
public class ScoringEngine {

  // Scoring weights (can be made configurable)
  private static final BigDecimal W1_PHE_OVER =
      new BigDecimal("100.0"); // High penalty for PHE violations
  private static final BigDecimal W2_PROTEIN_OVER =
      new BigDecimal("80.0"); // High penalty for protein violations
  private static final BigDecimal W3_KCAL_DEFICIT =
      new BigDecimal("0.5"); // Moderate penalty for calorie deficit
  private static final BigDecimal W4_COST = new BigDecimal("10.0"); // Moderate penalty for cost
  private static final BigDecimal W5_REPEAT = new BigDecimal("50.0"); // High penalty for repeats

  // Scaling factors
  private static final BigDecimal HUNDRED = new BigDecimal("100.0");
  private static final int SCALE = 4;

  /** Calculate comprehensive score for a food candidate */
  public BigDecimal calculateScore(
      FoodCandidate candidate,
      MealSlot mealSlot,
      NormPrescriptionDto norm,
      BigDecimal dailyBudgetLimit,
      int repeatDays) {

    BigDecimal pheOverPenalty = calculatePheOverPenalty(candidate, norm);
    BigDecimal proteinOverPenalty = calculateProteinOverPenalty(candidate, norm);
    BigDecimal kcalDeficitPenalty = calculateKcalDeficitPenalty(candidate, mealSlot);
    BigDecimal costPenalty = calculateCostPenalty(candidate, dailyBudgetLimit);
    BigDecimal repeatPenalty = calculateRepeatPenalty(repeatDays);

    // Store components in candidate for analysis
    candidate.setPheOverPenalty(pheOverPenalty);
    candidate.setProteinOverPenalty(proteinOverPenalty);
    candidate.setKcalDeficitPenalty(kcalDeficitPenalty);
    candidate.setCostPenalty(costPenalty);
    candidate.setRepeatPenalty(repeatPenalty);

    BigDecimal totalScore =
        pheOverPenalty
            .add(proteinOverPenalty)
            .add(kcalDeficitPenalty)
            .add(costPenalty)
            .add(repeatPenalty);

    // Apply pantry bonus (reduce score by 10% if available in pantry)
    if (candidate.isAvailableInPantry() && candidate.hasSufficientPantryQuantity()) {
      totalScore = totalScore.multiply(new BigDecimal("0.9"));
    }

    candidate.setScore(totalScore.setScale(SCALE, RoundingMode.HALF_UP));

    log.debug(
        "Scored candidate {}: PHE={}, Protein={}, Kcal={}, Cost={}, Repeat={}, Total={}",
        candidate.getItemName(),
        pheOverPenalty,
        proteinOverPenalty,
        kcalDeficitPenalty,
        costPenalty,
        repeatPenalty,
        candidate.getScore());

    return candidate.getScore();
  }

  /** Calculate penalty for PHE constraint violation */
  private BigDecimal calculatePheOverPenalty(FoodCandidate candidate, NormPrescriptionDto norm) {
    if (norm.dailyPheMgLimit() == null || candidate.getCalculatedPheMg() == null) {
      return BigDecimal.ZERO;
    }

    // Calculate how much this serving contributes to daily PHE as percentage
    BigDecimal pheContribution =
        candidate
            .getCalculatedPheMg()
            .divide(norm.dailyPheMgLimit(), SCALE, RoundingMode.HALF_UP)
            .multiply(HUNDRED);

    // Penalty grows exponentially if this single serving exceeds reasonable meal portion (>25% of
    // daily)
    BigDecimal reasonableMealPortion = new BigDecimal("25.0");
    if (pheContribution.compareTo(reasonableMealPortion) > 0) {
      BigDecimal excess = pheContribution.subtract(reasonableMealPortion);
      return W1_PHE_OVER
          .multiply(excess)
          .multiply(excess)
          .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
    }

    return BigDecimal.ZERO;
  }

  /** Calculate penalty for protein constraint violation */
  private BigDecimal calculateProteinOverPenalty(
      FoodCandidate candidate, NormPrescriptionDto norm) {
    if (norm.dailyProteinGLimit() == null || candidate.getCalculatedProteinG() == null) {
      return BigDecimal.ZERO;
    }

    // Calculate how much this serving contributes to daily protein as percentage
    BigDecimal proteinContribution =
        candidate
            .getCalculatedProteinG()
            .divide(norm.dailyProteinGLimit(), SCALE, RoundingMode.HALF_UP)
            .multiply(HUNDRED);

    // Penalty grows exponentially if this single serving exceeds reasonable meal portion (>25% of
    // daily)
    BigDecimal reasonableMealPortion = new BigDecimal("25.0");
    if (proteinContribution.compareTo(reasonableMealPortion) > 0) {
      BigDecimal excess = proteinContribution.subtract(reasonableMealPortion);
      return W2_PROTEIN_OVER
          .multiply(excess)
          .multiply(excess)
          .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
    }

    return BigDecimal.ZERO;
  }

  /** Calculate penalty for calorie deficit (not meeting target) */
  private BigDecimal calculateKcalDeficitPenalty(FoodCandidate candidate, MealSlot mealSlot) {
    if (mealSlot.getTargetKcal() == null || candidate.getCalculatedKcal() == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal targetKcal = mealSlot.getTargetKcal();
    BigDecimal actualKcal = BigDecimal.valueOf(candidate.getCalculatedKcal());

    // Penalty if this food provides significantly less calories than the meal target
    if (actualKcal.compareTo(targetKcal) < 0) {
      BigDecimal deficit = targetKcal.subtract(actualKcal);
      BigDecimal deficitPercentage =
          deficit.divide(targetKcal, SCALE, RoundingMode.HALF_UP).multiply(HUNDRED);
      return W3_KCAL_DEFICIT.multiply(deficitPercentage);
    }

    return BigDecimal.ZERO;
  }

  /** Calculate penalty for cost (higher cost = higher penalty) */
  private BigDecimal calculateCostPenalty(FoodCandidate candidate, BigDecimal dailyBudgetLimit) {
    if (candidate.getCostPerServing() == null
        || dailyBudgetLimit == null
        || dailyBudgetLimit.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    // Penalty based on what percentage of daily budget this serving costs
    BigDecimal costPercentage =
        candidate
            .getCostPerServing()
            .divide(dailyBudgetLimit, SCALE, RoundingMode.HALF_UP)
            .multiply(HUNDRED);

    return W4_COST.multiply(costPercentage);
  }

  /** Calculate penalty for recent repeats */
  private BigDecimal calculateRepeatPenalty(int repeatDays) {
    if (repeatDays <= 0) {
      return BigDecimal.ZERO; // No recent repeats
    }

    // Penalty decreases with time: 2 days ago = full penalty, 1 day ago = half penalty
    BigDecimal daysFactor = BigDecimal.valueOf(Math.max(0, 3 - repeatDays));
    return W5_REPEAT.multiply(daysFactor);
  }

  /** Calculate efficiency score (calories per mg PHE) - higher is better */
  public BigDecimal calculateEfficiency(FoodCandidate candidate) {
    if (candidate.getCalculatedPheMg() == null
        || candidate.getCalculatedKcal() == null
        || candidate.getCalculatedPheMg().compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return BigDecimal.valueOf(candidate.getCalculatedKcal())
        .divide(candidate.getCalculatedPheMg(), SCALE, RoundingMode.HALF_UP);
  }

  /** Calculate cost efficiency (calories per unit cost) - higher is better */
  public BigDecimal calculateCostEfficiency(FoodCandidate candidate) {
    if (candidate.getCostPerServing() == null
        || candidate.getCalculatedKcal() == null
        || candidate.getCostPerServing().compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    return BigDecimal.valueOf(candidate.getCalculatedKcal())
        .divide(candidate.getCostPerServing(), SCALE, RoundingMode.HALF_UP);
  }

  /** Generate reason for alternative suggestion */
  public String generateAlternativeReason(FoodCandidate alternative, FoodCandidate current) {
    if (current == null) {
      return "Primary suggestion";
    }

    // Cost improvement
    if (alternative.getCostPerServing() != null && current.getCostPerServing() != null) {
      BigDecimal costSaving = current.getCostPerServing().subtract(alternative.getCostPerServing());
      if (costSaving.compareTo(new BigDecimal("0.10")) > 0) {
        return String.format("Cheaper by %.2f %s", costSaving, "USD");
      }
    }

    // Calorie improvement
    if (alternative.getCalculatedKcal() != null && current.getCalculatedKcal() != null) {
      int kcalDiff = alternative.getCalculatedKcal() - current.getCalculatedKcal();
      if (Math.abs(kcalDiff) > 10) {
        return String.format("%+d kcal difference", kcalDiff);
      }
    }

    // Pantry availability
    if (alternative.isAvailableInPantry() && !current.isAvailableInPantry()) {
      return "Available in pantry";
    }

    // Variety
    if (alternative.getRepeatPenalty().compareTo(current.getRepeatPenalty()) < 0) {
      return "Avoids recent repeat";
    }

    return "Alternative option";
  }
}
