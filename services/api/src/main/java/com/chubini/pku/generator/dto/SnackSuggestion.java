package com.chubini.pku.generator.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/** Represents snack suggestions when calorie deficit exists */
@Schema(description = "Snack suggestion to address calorie deficit")
@Builder
public record SnackSuggestion(
    @Schema(description = "Name of the snack item", example = "Apple Slices") String itemName,
    @Schema(description = "Food category", example = "Fruits") String category,
    @Schema(description = "Suggested serving size in grams", example = "100.0")
        BigDecimal servingGrams,
    @Schema(description = "Cost per serving", example = "1.25") BigDecimal costPerServing,
    @Schema(description = "Whether available in pantry", example = "false")
        boolean availableInPantry,
    @Schema(
            description = "Reason for suggesting this snack",
            example = "Adds 85 kcal with minimal PHE")
        String reason,
    @Schema(description = "Safety score (0-100, higher is safer)", example = "95")
        Integer safetyScore,
    @Schema(description = "Nutritional values for suggested serving")
        MealAlternative.NutritionalSummary nutrition) {

  /** Create a list of snack suggestions response */
  @Schema(description = "Response containing snack suggestions")
  public record SnackSuggestionsResponse(
      @Schema(description = "Current calorie deficit", example = "-150") Integer calorieDeficit,
      @Schema(description = "Target calories to add", example = "150") Integer targetCaloriesToAdd,
      @Schema(description = "Remaining PHE budget", example = "45.5") BigDecimal remainingPheBudget,
      @Schema(description = "Remaining protein budget", example = "3.2")
          BigDecimal remainingProteinBudget,
      @Schema(description = "List of snack suggestions") List<SnackSuggestion> suggestions,
      @Schema(description = "Warning if no safe snacks available") String warning) {}
}
