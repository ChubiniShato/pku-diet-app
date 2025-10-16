package com.chubini.pku.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import com.chubini.pku.generator.dto.MenuGenerationRequest;
import com.chubini.pku.menus.MealSlot;
import com.chubini.pku.menus.MenuEntry;
import com.chubini.pku.norms.dto.NormPrescriptionDto;
import com.chubini.pku.products.Product;
import com.chubini.pku.products.ProductRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodSelectionService {

  private final ProductRepository productRepository;

  // Food categories suitable for different meal types
  private static final Map<MealSlot.SlotName, List<String>> MEAL_CATEGORIES =
      Map.of(
          MealSlot.SlotName.BREAKFAST, List.of("breakfast", "cereals", "bread", "fruits", "dairy"),
          MealSlot.SlotName.MORNING_SNACK, List.of("fruits", "snacks", "nuts", "dairy"),
          MealSlot.SlotName.LUNCH, List.of("vegetables", "grains", "protein", "bread", "dairy"),
          MealSlot.SlotName.AFTERNOON_SNACK, List.of("fruits", "snacks", "vegetables", "dairy"),
          MealSlot.SlotName.DINNER, List.of("vegetables", "protein", "grains", "bread"),
          MealSlot.SlotName.EVENING_SNACK, List.of("fruits", "dairy", "snacks"));

  /** Generate food entries for a meal slot based on nutritional targets */
  public List<MenuEntry> generateMealEntries(
      MealSlot mealSlot, NormPrescriptionDto norm, MenuGenerationRequest request) {
    log.debug("Generating food entries for meal slot: {}", mealSlot.getSlotName());

    List<MenuEntry> entries = new ArrayList<>();

    // Get suitable products for this meal type
    List<Product> suitableProducts = getSuitableProducts(mealSlot.getSlotName(), request);

    if (suitableProducts.isEmpty()) {
      log.warn("No suitable products found for meal slot: {}", mealSlot.getSlotName());
      return entries;
    }

    // Target nutritional values for this meal
    BigDecimal targetPhe =
        mealSlot.getTargetPheMg() != null ? mealSlot.getTargetPheMg() : BigDecimal.ZERO;
    BigDecimal targetKcal =
        mealSlot.getTargetKcal() != null ? mealSlot.getTargetKcal() : BigDecimal.ZERO;

    // Use heuristic algorithm to select foods
    entries = selectFoodsHeuristic(suitableProducts, targetPhe, targetKcal, mealSlot);

    log.info("Generated {} food entries for {}", entries.size(), mealSlot.getSlotName());
    return entries;
  }

  /** Get products suitable for a specific meal type */
  private List<Product> getSuitableProducts(
      MealSlot.SlotName slotName, MenuGenerationRequest request) {
    List<Product> allProducts = productRepository.findAll();

    // Filter by meal-appropriate categories
    List<String> suitableCategories = MEAL_CATEGORIES.get(slotName);

    List<Product> suitableProducts =
        allProducts.stream()
            .filter(product -> isSuitableForMeal(product, suitableCategories))
            .filter(product -> !isAvoidedFood(product, request.foodsToAvoid()))
            .filter(product -> hasValidNutrition(product))
            .collect(Collectors.toList());

    // If we don't have enough suitable products, include more categories
    if (suitableProducts.size() < 5) {
      suitableProducts =
          allProducts.stream()
              .filter(product -> !isAvoidedFood(product, request.foodsToAvoid()))
              .filter(product -> hasValidNutrition(product))
              .collect(Collectors.toList());
    }

    return suitableProducts;
  }

  /** Check if a product is suitable for a meal based on categories */
  private boolean isSuitableForMeal(Product product, List<String> suitableCategories) {
    if (product.getCategory() == null) return false;

    String category = product.getCategory().toLowerCase();
    return suitableCategories.stream()
        .anyMatch(suitableCategory -> category.contains(suitableCategory.toLowerCase()));
  }

  /** Check if a product should be avoided based on user preferences */
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

  /** Check if product has valid nutritional data */
  private boolean hasValidNutrition(Product product) {
    return product.getPhenylalanine() != null
        && product.getKilocalories() != null
        && product.getPhenylalanine().compareTo(BigDecimal.ZERO) >= 0
        && product.getKilocalories().compareTo(BigDecimal.ZERO) > 0;
  }

  /** Heuristic algorithm to select foods for a meal */
  private List<MenuEntry> selectFoodsHeuristic(
      List<Product> products, BigDecimal targetPhe, BigDecimal targetKcal, MealSlot mealSlot) {
    List<MenuEntry> entries = new ArrayList<>();

    // Sort products by PHE efficiency (calories per mg PHE)
    List<Product> sortedProducts =
        products.stream()
            .filter(p -> p.getPhenylalanine().compareTo(BigDecimal.ZERO) > 0)
            .sorted(
                (p1, p2) -> {
                  BigDecimal efficiency1 =
                      p1.getKilocalories().divide(p1.getPhenylalanine(), 2, RoundingMode.HALF_UP);
                  BigDecimal efficiency2 =
                      p2.getKilocalories().divide(p2.getPhenylalanine(), 2, RoundingMode.HALF_UP);
                  return efficiency2.compareTo(efficiency1); // Higher efficiency first
                })
            .collect(Collectors.toList());

    BigDecimal remainingPhe = targetPhe;
    BigDecimal remainingKcal = targetKcal;

    // Select 2-4 foods per meal
    int maxFoods = Math.min(4, sortedProducts.size());
    int selectedFoods = 0;

    for (Product product : sortedProducts) {
      if (selectedFoods >= maxFoods || remainingPhe.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }

      // Calculate optimal serving size
      BigDecimal servingSize = calculateOptimalServing(product, remainingPhe, remainingKcal);

      if (servingSize.compareTo(BigDecimal.ZERO) > 0) {
        MenuEntry entry = createMenuEntry(product, servingSize, mealSlot);
        entries.add(entry);

        // Update remaining targets
        remainingPhe = remainingPhe.subtract(entry.getCalculatedPheMg());
        remainingKcal = remainingKcal.subtract(entry.getCalculatedKcal());
        selectedFoods++;
      }
    }

    return entries;
  }

  /** Calculate optimal serving size for a product */
  private BigDecimal calculateOptimalServing(
      Product product, BigDecimal targetPhe, BigDecimal targetKcal) {
    // Base serving size on PHE constraint (most important for PKU)
    BigDecimal pheServing = BigDecimal.ZERO;
    if (product.getPhenylalanine().compareTo(BigDecimal.ZERO) > 0) {
      pheServing = targetPhe.divide(product.getPhenylalanine(), 2, RoundingMode.HALF_UP);
    }

    // Consider calorie constraint
    BigDecimal kcalServing = BigDecimal.ZERO;
    if (product.getKilocalories().compareTo(BigDecimal.ZERO) > 0) {
      kcalServing = targetKcal.divide(product.getKilocalories(), 2, RoundingMode.HALF_UP);
    }

    // Use the more restrictive constraint, but ensure reasonable serving sizes
    BigDecimal servingSize = pheServing.min(kcalServing);

    // Ensure serving size is reasonable (between 10g and 500g)
    servingSize = servingSize.max(new BigDecimal("10"));
    servingSize = servingSize.min(new BigDecimal("500"));

    return servingSize;
  }

  /** Create a menu entry for a selected product */
  private MenuEntry createMenuEntry(Product product, BigDecimal servingSize, MealSlot mealSlot) {
    // Calculate nutritional values for the serving
    BigDecimal calculatedPhe =
        product
            .getPhenylalanine()
            .multiply(servingSize)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    BigDecimal calculatedProtein =
        product.getProtein() != null
            ? product
                .getProtein()
                .multiply(servingSize)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    BigDecimal calculatedKcal =
        product
            .getKilocalories()
            .multiply(servingSize)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    BigDecimal calculatedFat =
        product.getFats() != null
            ? product
                .getFats()
                .multiply(servingSize)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    return MenuEntry.builder()
        .mealSlot(mealSlot)
        .entryType(MenuEntry.EntryType.PRODUCT)
        .product(product)
        .plannedServingGrams(servingSize)
        .calculatedPheMg(calculatedPhe)
        .calculatedProteinG(calculatedProtein)
        .calculatedKcal(calculatedKcal)
        .calculatedFatG(calculatedFat)
        .isConsumed(false)
        .notes("Auto-generated food selection")
        .build();
  }
}
