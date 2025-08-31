package com.chubini.pku.generator.dto;

import java.math.BigDecimal;
import java.util.List;

import com.chubini.pku.dishes.CustomDish;
import com.chubini.pku.dishes.Dish;
import com.chubini.pku.menus.MenuEntry;
import com.chubini.pku.pantry.PantryItem;
import com.chubini.pku.products.CustomProduct;
import com.chubini.pku.products.Product;

import lombok.Builder;
import lombok.Data;

/** Represents a candidate food item for menu generation with scoring information */
@Data
@Builder
public class FoodCandidate {

  private MenuEntry.EntryType entryType;
  private Product product;
  private CustomProduct customProduct;
  private Dish dish;
  private CustomDish customDish;

  // Scoring components
  private BigDecimal score;
  private BigDecimal pheOverPenalty;
  private BigDecimal proteinOverPenalty;
  private BigDecimal kcalDeficitPenalty;
  private BigDecimal costPenalty;
  private BigDecimal repeatPenalty;

  // Serving information
  private BigDecimal suggestedServingGrams;
  private BigDecimal costPerServing;

  // Pantry availability
  private boolean availableInPantry;
  private BigDecimal pantryQuantityAvailable;
  private List<PantryItem> pantryItems;

  // Nutritional values for suggested serving
  private BigDecimal calculatedPheMg;
  private BigDecimal calculatedProteinG;
  private Integer calculatedKcal;
  private BigDecimal calculatedFatG;

  // Alternative information
  private String alternativeReason;
  private BigDecimal improvementValue; // How much better this alternative is

  /** Get the name of the food item regardless of type */
  public String getItemName() {
    return switch (entryType) {
      case PRODUCT -> product != null ? product.getProductName() : null;
      case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getName() : null;
      case DISH -> dish != null ? dish.getName() : null;
      case CUSTOM_DISH -> customDish != null ? customDish.getName() : null;
    };
  }

  /** Get the category of the food item regardless of type */
  public String getItemCategory() {
    return switch (entryType) {
      case PRODUCT -> product != null ? product.getCategory() : null;
      case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getCategory() : null;
      case DISH -> dish != null ? dish.getCategory() : null;
      case CUSTOM_DISH -> customDish != null ? customDish.getCategory() : null;
    };
  }

  /** Check if this candidate has sufficient pantry quantity for the suggested serving */
  public boolean hasSufficientPantryQuantity() {
    return !availableInPantry
        || (pantryQuantityAvailable != null
            && suggestedServingGrams != null
            && pantryQuantityAvailable.compareTo(suggestedServingGrams) >= 0);
  }

  /** Get base nutrition per 100g for calculations */
  public NutritionPer100g getBasePer100gNutrition() {
    return switch (entryType) {
      case PRODUCT ->
          product != null
              ? NutritionPer100g.builder()
                  .pheMg(product.getPhenylalanine())
                  .proteinG(product.getProtein())
                  .kcal(product.getKilocalories().intValue())
                  .fatG(product.getFats())
                  .build()
              : null;
      case CUSTOM_PRODUCT ->
          customProduct != null
              ? NutritionPer100g.builder()
                  .pheMg(customProduct.getPhenylalanine())
                  .proteinG(customProduct.getProtein())
                  .kcal(
                      customProduct.getKilocalories() != null
                          ? customProduct.getKilocalories().intValue()
                          : 0)
                  .fatG(customProduct.getFats())
                  .build()
              : null;
      case DISH ->
          dish != null
              ? NutritionPer100g.builder()
                  .pheMg(dish.getPer100Phenylalanine())
                  .proteinG(dish.getPer100Protein())
                  .kcal(
                      dish.getPer100Kilocalories() != null
                          ? dish.getPer100Kilocalories().intValue()
                          : 0)
                  .fatG(dish.getPer100Fats())
                  .build()
              : null;
      case CUSTOM_DISH ->
          customDish != null
              ? NutritionPer100g.builder()
                  .pheMg(customDish.getPer100Phenylalanine())
                  .proteinG(customDish.getPer100Protein())
                  .kcal(
                      customDish.getPer100Kilocalories() != null
                          ? customDish.getPer100Kilocalories().intValue()
                          : 0)
                  .fatG(customDish.getPer100Fats())
                  .build()
              : null;
    };
  }

  @Data
  @Builder
  public static class NutritionPer100g {
    private BigDecimal pheMg;
    private BigDecimal proteinG;
    private Integer kcal;
    private BigDecimal fatG;
  }
}
