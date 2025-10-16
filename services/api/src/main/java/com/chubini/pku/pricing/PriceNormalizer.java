package com.chubini.pku.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for normalizing prices to a standard unit (per 100g). Ensures consistent price
 * comparisons across different unit types.
 */
public final class PriceNormalizer {

  private PriceNormalizer() {
    // Utility class
  }

  /**
   * Normalizes price to per 100g basis for consistent comparison.
   *
   * @param price the price to normalize
   * @param unit the unit of the price (kg, g, or per 100g)
   * @return price normalized to per 100g
   */
  public static BigDecimal toPer100g(BigDecimal price, String unit) {
    if (price == null || unit == null) {
      return price;
    }

    return switch (unit.toLowerCase()) {
      case "kg" -> price.divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP); // 100g = 0.1kg
      case "g" -> price.multiply(new BigDecimal("100")); // per g to per 100g
      case "100g", "per100g" -> price; // already per 100g
      default -> price; // assume already per 100g if unknown unit
    };
  }

  /**
   * Converts price from per 100g to per kg.
   *
   * @param pricePer100g price per 100g
   * @return price per kg
   */
  public static BigDecimal toPerKg(BigDecimal pricePer100g) {
    if (pricePer100g == null) {
      return null;
    }
    return pricePer100g.multiply(BigDecimal.TEN);
  }

  /**
   * Converts price from per 100g to per gram.
   *
   * @param pricePer100g price per 100g
   * @return price per gram
   */
  public static BigDecimal toPerGram(BigDecimal pricePer100g) {
    if (pricePer100g == null) {
      return null;
    }
    return pricePer100g.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
  }
}
