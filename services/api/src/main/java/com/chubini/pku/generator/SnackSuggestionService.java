package com.chubini.pku.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import com.chubini.pku.generator.dto.MealAlternative;
import com.chubini.pku.generator.dto.SnackSuggestion;
import com.chubini.pku.menus.MenuDay;
import com.chubini.pku.menus.MenuDayRepository;
import com.chubini.pku.norms.NormService;
import com.chubini.pku.norms.dto.NormPrescriptionDto;
import com.chubini.pku.products.Product;
import com.chubini.pku.products.ProductRepository;
import com.chubini.pku.validation.NutritionCalculator;
import com.chubini.pku.validation.NutritionScaler;
import com.chubini.pku.validation.dto.NutritionBreakdown;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Service for generating snack suggestions when core meals can't be safely increased */
@Service
@RequiredArgsConstructor
@Slf4j
public class SnackSuggestionService {

  private final MenuDayRepository menuDayRepository;
  private final NormService normService;
  private final NutritionCalculator nutritionCalculator;
  private final ProductRepository productRepository;
  private final PantryAwareService pantryAwareService;
  private final NutritionScaler nutritionScaler;

  // Snack categories with low PHE/protein ratios
  private static final List<String> SAFE_SNACK_CATEGORIES =
      List.of("fruits", "vegetables", "snacks-low-protein", "beverages");

  // Maximum safe serving sizes to prevent overconsumption
  private static final Map<String, BigDecimal> MAX_SERVING_SIZES =
      Map.of(
          "fruits", BigDecimal.valueOf(150), // 150g
          "vegetables", BigDecimal.valueOf(200), // 200g
          "snacks-low-protein", BigDecimal.valueOf(50), // 50g
          "beverages", BigDecimal.valueOf(250) // 250ml
          );

  /** Generate snack suggestions for a menu day with calorie deficit */
  public SnackSuggestion.SnackSuggestionsResponse generateSnackSuggestions(UUID dayId) {
    log.debug("Generating snack suggestions for menu day: {}", dayId);

    Optional<MenuDay> menuDayOpt = menuDayRepository.findById(dayId);
    if (menuDayOpt.isEmpty()) {
      log.warn("Menu day not found: {}", dayId);
      return createWarningResponse("Menu day not found");
    }

    MenuDay menuDay = menuDayOpt.get();

    // Get active norm for the patient
    Optional<NormPrescriptionDto> normOpt =
        normService.getCurrentNormForPatient(menuDay.getPatient().getId());
    if (normOpt.isEmpty()) {
      log.warn("No active norm found for patient: {}", menuDay.getPatient().getId());
      return createWarningResponse("No active nutritional norms found for patient");
    }

    NormPrescriptionDto norm = normOpt.get();

    // Calculate current totals
    NutritionCalculator.DayTotals planned = nutritionCalculator.calculatePlannedTotals(menuDay);

    // Check if there's a calorie deficit
    int calorieDeficit = calculateCalorieDeficit(planned, norm);
    if (calorieDeficit <= 0) {
      log.debug("No calorie deficit detected for menu day: {}", dayId);
      return createNoDeficitResponse(planned, norm);
    }

    // Calculate remaining PHE and protein budgets
    BigDecimal remainingPheBudget = calculateRemainingPheBudget(planned, norm);
    BigDecimal remainingProteinBudget = calculateRemainingProteinBudget(planned, norm);

    // Check if we have enough budget for safe snacking
    if (remainingPheBudget.compareTo(BigDecimal.valueOf(10)) < 0
        || remainingProteinBudget.compareTo(BigDecimal.valueOf(1)) < 0) {
      log.warn(
          "Insufficient PHE/protein budget for safe snacking: PHE={}, Protein={}",
          remainingPheBudget,
          remainingProteinBudget);
      return createUnsafeResponse(calorieDeficit, remainingPheBudget, remainingProteinBudget);
    }

    // Generate snack suggestions
    List<SnackSuggestion> suggestions =
        generateSafeSnackOptions(
            calorieDeficit, remainingPheBudget, remainingProteinBudget, menuDay.getPatient());

    return new SnackSuggestion.SnackSuggestionsResponse(
        calorieDeficit,
        Math.min(calorieDeficit, 200), // Cap at 200 kcal per snack session
        remainingPheBudget,
        remainingProteinBudget,
        suggestions,
        suggestions.isEmpty() ? "No safe snack options found within PHE/protein limits" : null);
  }

  private int calculateCalorieDeficit(
      NutritionCalculator.DayTotals planned, NormPrescriptionDto norm) {
    if (planned.kcal() == null || norm.dailyKcalMin() == null) {
      return 0;
    }
    return norm.dailyKcalMin().intValue() - planned.kcal();
  }

  private BigDecimal calculateRemainingPheBudget(
      NutritionCalculator.DayTotals planned, NormPrescriptionDto norm) {
    if (planned.pheMg() == null || norm.dailyPheMgLimit() == null) {
      return BigDecimal.ZERO;
    }
    return norm.dailyPheMgLimit().subtract(planned.pheMg()).max(BigDecimal.ZERO);
  }

  private BigDecimal calculateRemainingProteinBudget(
      NutritionCalculator.DayTotals planned, NormPrescriptionDto norm) {
    if (planned.proteinG() == null || norm.dailyProteinGLimit() == null) {
      return BigDecimal.ZERO;
    }
    return norm.dailyProteinGLimit().subtract(planned.proteinG()).max(BigDecimal.ZERO);
  }

  private List<SnackSuggestion> generateSafeSnackOptions(
      int calorieDeficit,
      BigDecimal remainingPheBudget,
      BigDecimal remainingProteinBudget,
      com.chubini.pku.patients.PatientProfile patient) {

    List<SnackSuggestion> suggestions = new ArrayList<>();

    // Get products from safe snack categories
    List<Product> safeProducts =
        productRepository.findAll().stream()
            .filter(product -> SAFE_SNACK_CATEGORIES.contains(product.getCategory()))
            .filter(
                product -> product.getPhenylalanine() != null && product.getKilocalories() != null)
            .collect(Collectors.toList());

    log.debug("Found {} safe snack products", safeProducts.size());

    for (Product product : safeProducts) {
      // Calculate optimal serving size within budget constraints
      BigDecimal optimalServing =
          calculateOptimalServingSize(
              product, remainingPheBudget, remainingProteinBudget, calorieDeficit);

      if (optimalServing.compareTo(BigDecimal.valueOf(10)) >= 0) { // Minimum 10g serving
        SnackSuggestion suggestion = createSnackSuggestion(product, optimalServing, patient);
        suggestions.add(suggestion);
      }
    }

    // Sort by safety score (descending) and then by calorie contribution
    suggestions.sort(
        (a, b) -> {
          int safetyCompare = Integer.compare(b.safetyScore(), a.safetyScore());
          if (safetyCompare != 0) return safetyCompare;

          Integer aKcal = a.nutrition() != null ? a.nutrition().kcal() : 0;
          Integer bKcal = b.nutrition() != null ? b.nutrition().kcal() : 0;
          return Integer.compare(bKcal, aKcal);
        });

    // Return top 5 suggestions
    return suggestions.stream().limit(5).collect(Collectors.toList());
  }

  private BigDecimal calculateOptimalServingSize(
      Product product,
      BigDecimal remainingPheBudget,
      BigDecimal remainingProteinBudget,
      int targetCalories) {

    // Calculate maximum serving based on PHE constraint
    BigDecimal maxByPhe = BigDecimal.valueOf(10000); // Very high default
    if (product.getPhenylalanine() != null
        && product.getPhenylalanine().compareTo(BigDecimal.ZERO) > 0) {
      maxByPhe =
          remainingPheBudget
              .multiply(BigDecimal.valueOf(100))
              .divide(product.getPhenylalanine(), 2, RoundingMode.DOWN);
    }

    // Calculate maximum serving based on protein constraint
    BigDecimal maxByProtein = BigDecimal.valueOf(10000); // Very high default
    if (product.getProtein() != null && product.getProtein().compareTo(BigDecimal.ZERO) > 0) {
      maxByProtein =
          remainingProteinBudget
              .multiply(BigDecimal.valueOf(100))
              .divide(product.getProtein(), 2, RoundingMode.DOWN);
    }

    // Calculate serving size for target calories (aim for 1/3 of deficit)
    BigDecimal targetCalorieServing = BigDecimal.valueOf(10000); // Very high default
    if (product.getKilocalories() != null
        && product.getKilocalories().compareTo(BigDecimal.ZERO) > 0) {
      BigDecimal targetCals = BigDecimal.valueOf(targetCalories / 3.0); // 1/3 of deficit
      targetCalorieServing =
          targetCals
              .multiply(BigDecimal.valueOf(100))
              .divide(product.getKilocalories(), 2, RoundingMode.DOWN);
    }

    // Take the minimum of all constraints
    BigDecimal optimalServing = maxByPhe.min(maxByProtein).min(targetCalorieServing);

    // Apply category-specific maximum serving size
    String category = product.getCategory();
    BigDecimal maxCategoryServing =
        MAX_SERVING_SIZES.getOrDefault(category, BigDecimal.valueOf(100));
    optimalServing = optimalServing.min(maxCategoryServing);

    return optimalServing.max(BigDecimal.ZERO);
  }

  private SnackSuggestion createSnackSuggestion(
      Product product, BigDecimal servingGrams, com.chubini.pku.patients.PatientProfile patient) {

    // Calculate nutrition for this serving
    NutritionBreakdown nutrition = nutritionScaler.from(product, servingGrams, "G");

    // Get cost information
    BigDecimal cost = pantryAwareService.getCurrentCost(product, servingGrams, patient);

    // Check pantry availability
    PantryAwareService.PantryAvailability pantryAvail =
        pantryAwareService.checkPantryAvailability(product, patient, servingGrams);

    // Calculate safety score (0-100)
    int safetyScore = calculateSafetyScore(nutrition);

    // Generate reason
    String reason = generateSnackReason(nutrition);

    return SnackSuggestion.builder()
        .itemName(product.getProductName())
        .category(product.getCategory())
        .servingGrams(servingGrams)
        .costPerServing(cost)
        .availableInPantry(pantryAvail.isAvailable())
        .reason(reason)
        .safetyScore(safetyScore)
        .nutrition(
            new MealAlternative.NutritionalSummary(
                nutrition.pheMg(), nutrition.proteinG(), nutrition.kcal(), nutrition.fatG()))
        .build();
  }

  private int calculateSafetyScore(NutritionBreakdown nutrition) {
    // Start with base score
    int score = 100;

    // Penalize high PHE content (more than 20mg per serving)
    if (nutrition.pheMg() != null && nutrition.pheMg().compareTo(BigDecimal.valueOf(20)) > 0) {
      score -= Math.min(30, nutrition.pheMg().intValue() - 20);
    }

    // Penalize high protein content (more than 2g per serving)
    if (nutrition.proteinG() != null && nutrition.proteinG().compareTo(BigDecimal.valueOf(2)) > 0) {
      score -= Math.min(25, (nutrition.proteinG().intValue() - 2) * 5);
    }

    // Bonus for good calorie contribution (50-100 kcal range)
    if (nutrition.kcal() != null) {
      if (nutrition.kcal() >= 50 && nutrition.kcal() <= 100) {
        score += 10;
      }
    }

    return Math.max(0, Math.min(100, score));
  }

  private String generateSnackReason(NutritionBreakdown nutrition) {
    if (nutrition.kcal() != null) {
      return String.format("Adds %d kcal with %.1f mg PHE", nutrition.kcal(), nutrition.pheMg());
    }
    return "Low-PHE snack option";
  }

  private SnackSuggestion.SnackSuggestionsResponse createWarningResponse(String warning) {
    return new SnackSuggestion.SnackSuggestionsResponse(
        0, 0, BigDecimal.ZERO, BigDecimal.ZERO, new ArrayList<>(), warning);
  }

  private SnackSuggestion.SnackSuggestionsResponse createNoDeficitResponse(
      NutritionCalculator.DayTotals planned, NormPrescriptionDto norm) {
    return new SnackSuggestion.SnackSuggestionsResponse(
        0,
        0,
        calculateRemainingPheBudget(planned, norm),
        calculateRemainingProteinBudget(planned, norm),
        new ArrayList<>(),
        "No calorie deficit detected - snacks not needed");
  }

  private SnackSuggestion.SnackSuggestionsResponse createUnsafeResponse(
      int calorieDeficit, BigDecimal remainingPheBudget, BigDecimal remainingProteinBudget) {
    return new SnackSuggestion.SnackSuggestionsResponse(
        calorieDeficit,
        calorieDeficit,
        remainingPheBudget,
        remainingProteinBudget,
        new ArrayList<>(),
        "Insufficient PHE/protein budget remaining for safe snack additions");
  }
}
