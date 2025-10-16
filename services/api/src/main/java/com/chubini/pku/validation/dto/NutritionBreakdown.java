package com.chubini.pku.validation.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Detailed nutritional breakdown for a food item with specific quantity")
public record NutritionBreakdown(
    @Schema(description = "Phenylalanine content in mg", example = "250.5")
        @PositiveOrZero(message = "PHE content must be non-negative")
        BigDecimal pheMg,
    @Schema(description = "Natural protein content in grams", example = "12.3")
        @PositiveOrZero(message = "Protein content must be non-negative")
        BigDecimal proteinG,
    @Schema(description = "Calorie content (kcal)", example = "450")
        @PositiveOrZero(message = "Calorie content must be non-negative")
        Integer kcal,
    @Schema(description = "Fat content in grams", example = "15.2")
        @PositiveOrZero(message = "Fat content must be non-negative")
        BigDecimal fatG,
    @Schema(description = "Quantity used for calculation", example = "150.0") BigDecimal quantity,
    @Schema(description = "Unit used for calculation", example = "G") String unit) {

  public static NutritionBreakdown zero() {
    return new NutritionBreakdown(
        BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, "G");
  }

  public NutritionBreakdown add(NutritionBreakdown other) {
    return new NutritionBreakdown(
        this.pheMg != null && other.pheMg != null
            ? this.pheMg.add(other.pheMg)
            : (this.pheMg != null ? this.pheMg : other.pheMg),
        this.proteinG != null && other.proteinG != null
            ? this.proteinG.add(other.proteinG)
            : (this.proteinG != null ? this.proteinG : other.proteinG),
        this.kcal != null && other.kcal != null
            ? this.kcal + other.kcal
            : (this.kcal != null ? this.kcal : other.kcal),
        this.fatG != null && other.fatG != null
            ? this.fatG.add(other.fatG)
            : (this.fatG != null ? this.fatG : other.fatG),
        this.quantity != null && other.quantity != null
            ? this.quantity.add(other.quantity)
            : (this.quantity != null ? this.quantity : other.quantity),
        this.unit != null ? this.unit : other.unit);
  }

  /** Convert to NutritionalValues for validation */
  public NutritionalValues toNutritionalValues() {
    return new NutritionalValues(
        this.pheMg,
        this.proteinG,
        this.kcal != null ? BigDecimal.valueOf(this.kcal) : null,
        this.fatG);
  }
}
