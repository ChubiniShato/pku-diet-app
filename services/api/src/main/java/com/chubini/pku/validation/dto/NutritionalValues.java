package com.chubini.pku.validation.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Nutritional values for validation")
public record NutritionalValues(
    @Schema(description = "Phenylalanine content in mg", example = "250.5")
        @PositiveOrZero(message = "PHE content must be non-negative")
        BigDecimal pheMg,
    @Schema(description = "Protein content in grams", example = "12.3")
        @PositiveOrZero(message = "Protein content must be non-negative")
        BigDecimal proteinG,
    @Schema(description = "Calorie content", example = "450")
        @PositiveOrZero(message = "Calorie content must be non-negative")
        BigDecimal kcal,
    @Schema(description = "Fat content in grams", example = "15.2")
        @PositiveOrZero(message = "Fat content must be non-negative")
        BigDecimal fatG) {

  public static NutritionalValues zero() {
    return new NutritionalValues(
        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
  }

  public NutritionalValues add(NutritionalValues other) {
    return new NutritionalValues(
        this.pheMg != null && other.pheMg != null
            ? this.pheMg.add(other.pheMg)
            : (this.pheMg != null ? this.pheMg : other.pheMg),
        this.proteinG != null && other.proteinG != null
            ? this.proteinG.add(other.proteinG)
            : (this.proteinG != null ? this.proteinG : other.proteinG),
        this.kcal != null && other.kcal != null
            ? this.kcal.add(other.kcal)
            : (this.kcal != null ? this.kcal : other.kcal),
        this.fatG != null && other.fatG != null
            ? this.fatG.add(other.fatG)
            : (this.fatG != null ? this.fatG : other.fatG));
  }

  public NutritionalValues multiply(BigDecimal factor) {
    return new NutritionalValues(
        this.pheMg != null ? this.pheMg.multiply(factor) : null,
        this.proteinG != null ? this.proteinG.multiply(factor) : null,
        this.kcal != null ? this.kcal.multiply(factor) : null,
        this.fatG != null ? this.fatG.multiply(factor) : null);
  }
}
