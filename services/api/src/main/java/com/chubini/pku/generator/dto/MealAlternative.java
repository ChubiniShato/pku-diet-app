package com.chubini.pku.generator.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** Represents an alternative food option for a meal slot */
@Schema(description = "Alternative food option for a meal slot")
@Builder
public record MealAlternative(
    @Schema(description = "Name of the alternative food item", example = "Cauliflower Rice")
        String itemName,
    @Schema(description = "Food category", example = "Vegetables") String category,
    @Schema(description = "Suggested serving size in grams", example = "150.0")
        BigDecimal servingGrams,
    @Schema(description = "Cost per serving", example = "2.50") BigDecimal costPerServing,
    @Schema(description = "Whether available in pantry", example = "true")
        boolean availableInPantry,
    @Schema(description = "Reason for suggesting this alternative", example = "Cheaper by 1.20 USD")
        String reason,
    @Schema(description = "Improvement value compared to current selection", example = "1.20")
        BigDecimal improvementValue,
    @Schema(description = "Nutritional values for suggested serving")
        NutritionalSummary nutrition) {

  @Schema(description = "Nutritional summary for the alternative")
  public record NutritionalSummary(
      @Schema(description = "Phenylalanine content in mg", example = "45.2") BigDecimal pheMg,
      @Schema(description = "Protein content in grams", example = "2.1") BigDecimal proteinG,
      @Schema(description = "Calorie content", example = "25") Integer kcal,
      @Schema(description = "Fat content in grams", example = "0.3") BigDecimal fatG) {}
}
