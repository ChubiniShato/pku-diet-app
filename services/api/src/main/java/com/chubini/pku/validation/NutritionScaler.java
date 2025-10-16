package com.chubini.pku.validation;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.chubini.pku.dishes.CustomDish;
import com.chubini.pku.dishes.Dish;
import com.chubini.pku.products.CustomProduct;
import com.chubini.pku.products.Product;
import com.chubini.pku.validation.dto.NutritionBreakdown;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** Utility class for scaling nutritional values based on quantity and unit */
@Component
@Slf4j
public class NutritionScaler {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
  private static final int SCALE = 2;

  /** Calculate nutrition breakdown from Product with specified quantity and unit */
  public NutritionBreakdown from(Product product, BigDecimal quantity, String unit) {
    if (product == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      return NutritionBreakdown.zero();
    }

    log.debug(
        "Scaling nutrition for product: {} with quantity: {} {}",
        product.getProductName(),
        quantity,
        unit);

    // For products, values are per 100g, so we scale based on actual quantity
    BigDecimal scaleFactor = quantity.divide(HUNDRED, 4, RoundingMode.HALF_UP);

    BigDecimal pheMg = scaleValue(product.getPhenylalanine(), scaleFactor);
    BigDecimal proteinG = scaleValue(product.getProtein(), scaleFactor);
    Integer kcal = scaleValueToInt(product.getKilocalories(), scaleFactor);
    BigDecimal fatG = scaleValue(product.getFats(), scaleFactor);

    return new NutritionBreakdown(pheMg, proteinG, kcal, fatG, quantity, unit);
  }

  /** Calculate nutrition breakdown from CustomProduct with specified quantity and unit */
  public NutritionBreakdown from(CustomProduct product, BigDecimal quantity, String unit) {
    if (product == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      return NutritionBreakdown.zero();
    }

    log.debug(
        "Scaling nutrition for custom product: {} with quantity: {} {}",
        product.getName(),
        quantity,
        unit);

    BigDecimal scaleFactor;
    if ("PIECE".equals(unit) && product.getStandardServingGrams() != null) {
      // For pieces, use the standard serving size
      scaleFactor =
          product
              .getStandardServingGrams()
              .multiply(quantity)
              .divide(HUNDRED, 4, RoundingMode.HALF_UP);
    } else {
      // For grams/ml, scale directly
      scaleFactor = quantity.divide(HUNDRED, 4, RoundingMode.HALF_UP);
    }

    BigDecimal pheMg = scaleValue(product.getPhenylalanine(), scaleFactor);
    BigDecimal proteinG = scaleValue(product.getProtein(), scaleFactor);
    Integer kcal = scaleValueToInt(product.getKilocalories(), scaleFactor);
    BigDecimal fatG = scaleValue(product.getFats(), scaleFactor);

    return new NutritionBreakdown(pheMg, proteinG, kcal, fatG, quantity, unit);
  }

  /** Calculate nutrition breakdown from Dish with specified quantity and unit */
  public NutritionBreakdown from(Dish dish, BigDecimal quantity, String unit) {
    if (dish == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      return NutritionBreakdown.zero();
    }

    log.debug(
        "Scaling nutrition for dish: {} with quantity: {} {}", dish.getName(), quantity, unit);

    BigDecimal scaleFactor;
    if ("PIECE".equals(unit) && dish.getNominalServingGrams() != null) {
      // For pieces, use the nominal serving size
      scaleFactor =
          dish.getNominalServingGrams().multiply(quantity).divide(HUNDRED, 4, RoundingMode.HALF_UP);
    } else {
      // For grams/ml, scale directly from per-100g values
      scaleFactor = quantity.divide(HUNDRED, 4, RoundingMode.HALF_UP);
    }

    BigDecimal pheMg = scaleValue(dish.getPer100Phenylalanine(), scaleFactor);
    BigDecimal proteinG = scaleValue(dish.getPer100Protein(), scaleFactor);
    Integer kcal = scaleValueToInt(dish.getPer100Kilocalories(), scaleFactor);
    BigDecimal fatG = scaleValue(dish.getPer100Fats(), scaleFactor);

    return new NutritionBreakdown(pheMg, proteinG, kcal, fatG, quantity, unit);
  }

  /** Calculate nutrition breakdown from CustomDish with specified quantity and unit */
  public NutritionBreakdown from(CustomDish dish, BigDecimal quantity, String unit) {
    if (dish == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
      return NutritionBreakdown.zero();
    }

    log.debug(
        "Scaling nutrition for custom dish: {} with quantity: {} {}",
        dish.getName(),
        quantity,
        unit);

    BigDecimal scaleFactor;
    if ("PIECE".equals(unit) && dish.getNominalServingGrams() != null) {
      // For pieces, use the nominal serving size
      scaleFactor =
          dish.getNominalServingGrams().multiply(quantity).divide(HUNDRED, 4, RoundingMode.HALF_UP);
    } else {
      // For grams/ml, scale directly from per-100g values
      scaleFactor = quantity.divide(HUNDRED, 4, RoundingMode.HALF_UP);
    }

    BigDecimal pheMg = scaleValue(dish.getPer100Phenylalanine(), scaleFactor);
    BigDecimal proteinG = scaleValue(dish.getPer100Protein(), scaleFactor);
    Integer kcal = scaleValueToInt(dish.getPer100Kilocalories(), scaleFactor);
    BigDecimal fatG = scaleValue(dish.getPer100Fats(), scaleFactor);

    return new NutritionBreakdown(pheMg, proteinG, kcal, fatG, quantity, unit);
  }

  /** Helper method to scale a BigDecimal value safely */
  private BigDecimal scaleValue(BigDecimal value, BigDecimal scaleFactor) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    return value.multiply(scaleFactor).setScale(SCALE, RoundingMode.HALF_UP);
  }

  /** Helper method to scale a BigDecimal value to integer (for calories) */
  private Integer scaleValueToInt(BigDecimal value, BigDecimal scaleFactor) {
    if (value == null) {
      return 0;
    }
    return value.multiply(scaleFactor).setScale(0, RoundingMode.HALF_UP).intValue();
  }
}
