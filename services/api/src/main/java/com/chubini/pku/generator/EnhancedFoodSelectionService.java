package com.chubini.pku.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import com.chubini.pku.generator.dto.FoodCandidate;
import com.chubini.pku.generator.dto.MenuGenerationRequest;
import com.chubini.pku.menus.MealSlot;
import com.chubini.pku.menus.MenuEntry;
import com.chubini.pku.norms.dto.NormPrescriptionDto;
import com.chubini.pku.patients.PatientProfile;
import com.chubini.pku.products.Product;
import com.chubini.pku.products.ProductRepository;
import com.chubini.pku.validation.NutritionScaler;
import com.chubini.pku.validation.dto.NutritionBreakdown;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Enhanced food selection service with Phase 2 features: - Variety enforcement - Pantry awareness -
 * Budget constraints - Scoring-based selection
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedFoodSelectionService {

  private final ProductRepository productRepository;
  private final NutritionScaler nutritionScaler;
  private final ScoringEngine scoringEngine;
  private final VarietyEngine varietyEngine;
  private final PantryAwareService pantryAwareService;

  // Core meal slot mappings (4 main meals - no snacks auto-inserted)
  private static final Set<MealSlot.SlotName> CORE_MEALS =
      Set.of(
          MealSlot.SlotName.BREAKFAST,
          MealSlot.SlotName.LUNCH,
          MealSlot.SlotName.DINNER,
          MealSlot.SlotName.EVENING_SNACK // Using evening snack as "supper"
          );

  /** Generate food candidates for a meal slot with Phase 2 enhancements */
  public List<FoodCandidate> generateCandidates(
      MealSlot mealSlot,
      PatientProfile patient,
      NormPrescriptionDto norm,
      MenuGenerationRequest request) {

    log.debug(
        "Generating enhanced candidates for {} on {}",
        mealSlot.getSlotName(),
        mealSlot.getMenuDay().getDate());

    // Get suitable products (basic filtering)
    List<Product> suitableProducts = getSuitableProducts(mealSlot.getSlotName(), request);

    // Apply variety filtering
    Set<String> itemsToAvoid =
        varietyEngine.getItemsToAvoidForVariety(
            patient,
            mealSlot.getMenuDay().getDate(),
            mealSlot.getSlotName().name(),
            request.emergencyMode());

    suitableProducts =
        suitableProducts.stream()
            .filter(p -> !itemsToAvoid.contains(p.getProductName()))
            .collect(Collectors.toList());

    if (suitableProducts.isEmpty()) {
      log.warn("No suitable products after variety filtering for {}", mealSlot.getSlotName());
      return new ArrayList<>();
    }

    // Convert to candidates with nutrition and cost calculations
    List<FoodCandidate> candidates = new ArrayList<>();

    for (Product product : suitableProducts) {
      FoodCandidate candidate = createCandidate(product, mealSlot, patient, norm, request);
      if (candidate != null) {
        candidates.add(candidate);
      }
    }

    // Score and rank candidates
    candidates.forEach(
        candidate -> {
          int repeatDays =
              varietyEngine.getDaysSinceLastUse(
                  candidate.getItemName(),
                  patient,
                  mealSlot.getMenuDay().getDate(),
                  mealSlot.getSlotName().name());

          BigDecimal dailyBudget =
              request.dailyBudgetLimit() != null
                  ? BigDecimal.valueOf(request.dailyBudgetLimit())
                  : null;

          scoringEngine.calculateScore(candidate, mealSlot, norm, dailyBudget, repeatDays);
        });

    // Sort by score (lower is better) and return top candidates
    candidates.sort(Comparator.comparing(FoodCandidate::getScore));

    log.debug("Generated {} scored candidates for {}", candidates.size(), mealSlot.getSlotName());
    return candidates.stream().limit(10).collect(Collectors.toList());
  }

  /** Create a food candidate from a product */
  private FoodCandidate createCandidate(
      Product product,
      MealSlot mealSlot,
      PatientProfile patient,
      NormPrescriptionDto norm,
      MenuGenerationRequest request) {

    // Calculate optimal serving size
    BigDecimal servingSize = calculateOptimalServing(product, mealSlot, norm);
    if (servingSize.compareTo(BigDecimal.ZERO) <= 0) {
      return null; // Not viable
    }

    // Calculate nutrition for this serving
    NutritionBreakdown nutrition = nutritionScaler.from(product, servingSize, "G");

    // Create candidate
    FoodCandidate candidate =
        FoodCandidate.builder()
            .entryType(MenuEntry.EntryType.PRODUCT)
            .product(product)
            .suggestedServingGrams(servingSize)
            .calculatedPheMg(nutrition.pheMg())
            .calculatedProteinG(nutrition.proteinG())
            .calculatedKcal(nutrition.kcal())
            .calculatedFatG(nutrition.fatG())
            .build();

    // Enhance with pantry and cost information if requested
    if (request.respectPantry()) {
      pantryAwareService.enhanceCandidateWithPantryInfo(candidate, patient);
    } else {
      // Just calculate market cost
      BigDecimal cost = pantryAwareService.getCurrentCost(product, servingSize, patient);
      candidate.setCostPerServing(cost);
    }

    return candidate;
  }

  /** Calculate optimal serving size for a product in a meal slot */
  private BigDecimal calculateOptimalServing(
      Product product, MealSlot mealSlot, NormPrescriptionDto norm) {
    if (product.getPhenylalanine() == null
        || product.getPhenylalanine().compareTo(BigDecimal.ZERO) <= 0) {
      return BigDecimal.ZERO;
    }

    // Base serving on PHE target for the meal
    BigDecimal targetPhe =
        mealSlot.getTargetPheMg() != null ? mealSlot.getTargetPheMg() : BigDecimal.ZERO;
    if (targetPhe.compareTo(BigDecimal.ZERO) <= 0) {
      // Fallback: use 20% of daily PHE limit
      targetPhe =
          norm.dailyPheMgLimit() != null
              ? norm.dailyPheMgLimit().multiply(new BigDecimal("0.20"))
              : new BigDecimal("50.0"); // Default 50mg
    }

    // Calculate serving size based on PHE content per 100g
    BigDecimal servingSize =
        targetPhe
            .multiply(new BigDecimal("100"))
            .divide(product.getPhenylalanine(), 2, RoundingMode.HALF_UP);

    // Apply reasonable bounds (10g - 500g)
    servingSize = servingSize.max(new BigDecimal("10.0"));
    servingSize = servingSize.min(new BigDecimal("500.0"));

    return servingSize;
  }

  /** Get suitable products for a meal type (from original service) */
  private List<Product> getSuitableProducts(
      MealSlot.SlotName slotName, MenuGenerationRequest request) {
    // Meal categories mapping
    Map<MealSlot.SlotName, List<String>> MEAL_CATEGORIES =
        Map.of(
            MealSlot.SlotName.BREAKFAST,
                List.of("breakfast", "cereals", "bread", "fruits", "dairy"),
            MealSlot.SlotName.LUNCH, List.of("vegetables", "grains", "protein", "bread", "dairy"),
            MealSlot.SlotName.DINNER, List.of("vegetables", "protein", "grains", "bread"),
            MealSlot.SlotName.EVENING_SNACK, List.of("vegetables", "protein", "grains"));

    List<Product> allProducts = productRepository.findAll();
    List<String> suitableCategories = MEAL_CATEGORIES.getOrDefault(slotName, List.of());

    List<Product> suitableProducts =
        allProducts.stream()
            .filter(product -> isSuitableForMeal(product, suitableCategories))
            .filter(product -> !isAvoidedFood(product, request.foodsToAvoid()))
            .filter(this::hasValidNutrition)
            .collect(Collectors.toList());

    // If too few suitable products, broaden the search
    if (suitableProducts.size() < 5) {
      suitableProducts =
          allProducts.stream()
              .filter(product -> !isAvoidedFood(product, request.foodsToAvoid()))
              .filter(this::hasValidNutrition)
              .collect(Collectors.toList());
    }

    return suitableProducts;
  }

  private boolean isSuitableForMeal(Product product, List<String> suitableCategories) {
    if (product.getCategory() == null) return false;
    String category = product.getCategory().toLowerCase();
    return suitableCategories.stream()
        .anyMatch(suitable -> category.contains(suitable.toLowerCase()));
  }

  private boolean isAvoidedFood(Product product, List<String> foodsToAvoid) {
    if (foodsToAvoid == null || foodsToAvoid.isEmpty()) return false;

    String productName = product.getProductName().toLowerCase();
    String category = product.getCategory() != null ? product.getCategory().toLowerCase() : "";

    return foodsToAvoid.stream()
        .anyMatch(
            avoid ->
                productName.contains(avoid.toLowerCase())
                    || category.contains(avoid.toLowerCase()));
  }

  private boolean hasValidNutrition(Product product) {
    return product.getPhenylalanine() != null
        && product.getKilocalories() != null
        && product.getPhenylalanine().compareTo(BigDecimal.ZERO) >= 0
        && product.getKilocalories().compareTo(BigDecimal.ZERO) > 0;
  }

  /** Select best candidates for core meals (no snacks auto-inserted) */
  public List<FoodCandidate> selectForCoreMeals(
      List<FoodCandidate> candidates, MealSlot mealSlot, int maxSelections) {
    if (!CORE_MEALS.contains(mealSlot.getSlotName())) {
      log.debug("Skipping non-core meal slot: {}", mealSlot.getSlotName());
      return new ArrayList<>(); // Don't auto-populate snack slots
    }

    // Select top candidates based on score
    return candidates.stream().limit(maxSelections).collect(Collectors.toList());
  }

  /** Create menu entry from food candidate */
  public MenuEntry createMenuEntry(FoodCandidate candidate, MealSlot mealSlot) {
    if (candidate == null) {
      return null;
    }

    MenuEntry entry =
        MenuEntry.builder()
            .mealSlot(mealSlot)
            .entryType(candidate.getEntryType())
            .product(candidate.getProduct())
            .customProduct(candidate.getCustomProduct())
            .dish(candidate.getDish())
            .customDish(candidate.getCustomDish())
            .plannedServingGrams(candidate.getSuggestedServingGrams())
            .calculatedPheMg(candidate.getCalculatedPheMg())
            .calculatedProteinG(candidate.getCalculatedProteinG())
            .calculatedKcal(
                candidate.getCalculatedKcal() != null
                    ? BigDecimal.valueOf(candidate.getCalculatedKcal())
                    : null)
            .calculatedFatG(candidate.getCalculatedFatG())
            .isConsumed(false)
            .notes("Auto-generated with Phase 2 algorithm")
            .build();

    // Reserve pantry quantity if applicable
    if (candidate.isAvailableInPantry() && candidate.getPantryItems() != null) {
      pantryAwareService.reservePantryQuantity(
          candidate.getPantryItems(), candidate.getSuggestedServingGrams());
    }

    return entry;
  }
}
