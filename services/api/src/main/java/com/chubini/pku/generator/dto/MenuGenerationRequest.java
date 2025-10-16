package com.chubini.pku.generator.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for generating a menu (daily or weekly)")
public record MenuGenerationRequest(
    @Schema(
            description = "Patient unique identifier",
            required = true,
            example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "Patient ID is required")
        UUID patientId,
    @Schema(description = "Start date for menu generation", required = true, example = "2024-01-01")
        @NotNull(message = "Start date is required")
        LocalDate startDate,
    @Schema(
            description = "Generation type",
            allowableValues = {"DAILY", "WEEKLY"},
            example = "WEEKLY")
        String generationType,
    @Schema(
            description = "Preferred meal categories",
            example = "[\"breakfast\", \"lunch\", \"dinner\"]")
        List<String> preferredCategories,
    @Schema(description = "Foods to avoid (allergens, dislikes)", example = "[\"nuts\", \"dairy\"]")
        List<String> foodsToAvoid,
    @Schema(description = "Maximum PHE per meal in mg", example = "50.0") Double maxPhePerMeal,
    @Schema(description = "Target calories per day", example = "2000.0")
        Double targetCaloriesPerDay,
    @Schema(description = "Include variety in meals", example = "true") Boolean includeVariety,
    @Schema(description = "Generate alternatives for each meal", example = "true")
        Boolean generateAlternatives,
    @Schema(description = "Additional generation notes", example = "Focus on low-sodium options")
        String notes,

    // Phase 2 additions
    @Schema(description = "Emergency mode - allows dish repeats within 2 days", example = "false")
        Boolean emergencyMode,
    @Schema(description = "Respect pantry availability when selecting foods", example = "true")
        Boolean respectPantry,
    @Schema(description = "Daily budget limit in specified currency", example = "25.00")
        Double dailyBudgetLimit,
    @Schema(description = "Weekly budget limit in specified currency", example = "150.00")
        Double weeklyBudgetLimit,
    @Schema(description = "Currency for budget calculations", example = "USD")
        String budgetCurrency) {

  public MenuGenerationRequest {
    // Set defaults
    if (generationType == null) generationType = "WEEKLY";
    if (preferredCategories == null) preferredCategories = List.of();
    if (foodsToAvoid == null) foodsToAvoid = List.of();
    if (includeVariety == null) includeVariety = true;
    if (generateAlternatives == null) generateAlternatives = false;
    if (emergencyMode == null) emergencyMode = false;
    if (respectPantry == null) respectPantry = true;
    if (budgetCurrency == null) budgetCurrency = "USD";
  }
}
