package com.chubini.pku.validation.dto;

import com.chubini.pku.validation.NutritionCalculator;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing day totals and validation result")
public record DayValidationResponse(
    @Schema(description = "Planned nutrition totals for the day") DayTotalsDto plannedTotals,
    @Schema(description = "Consumed nutrition totals for the day") DayTotalsDto consumedTotals,
    @Schema(description = "Validation result against norms") ValidationResult validationResult) {

  @Schema(description = "Daily nutrition totals")
  public record DayTotalsDto(
      @Schema(description = "Total phenylalanine in mg", example = "245.50")
          java.math.BigDecimal pheMg,
      @Schema(description = "Total natural protein in grams", example = "12.30")
          java.math.BigDecimal proteinG,
      @Schema(description = "Total calories", example = "1850") Integer kcal,
      @Schema(description = "Total fat in grams", example = "45.20") java.math.BigDecimal fatG) {
    public static DayTotalsDto from(NutritionCalculator.DayTotals totals) {
      return new DayTotalsDto(totals.pheMg(), totals.proteinG(), totals.kcal(), totals.fatG());
    }
  }
}
