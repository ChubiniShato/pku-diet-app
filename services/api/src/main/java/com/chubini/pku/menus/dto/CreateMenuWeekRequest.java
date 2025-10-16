package com.chubini.pku.menus.dto;

import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a new weekly menu")
public record CreateMenuWeekRequest(
    @Schema(description = "Patient identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "Patient ID is required")
        UUID patientId,
    @Schema(description = "Week starting date", example = "2024-01-01")
        @NotNull(message = "Week start date is required")
        LocalDate weekStartDate,
    @Schema(
            description = "Menu generation method",
            allowableValues = {"MANUAL", "HEURISTIC", "OPTIMIZED"})
        String generationMethod,
    @Schema(description = "Menu title", example = "Balanced Week Menu") String title,
    @Schema(description = "Menu description", example = "Low-phenylalanine weekly menu plan")
        String description) {}
