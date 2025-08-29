package com.chubini.pku.menus.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Weekly menu information")
public record MenuWeekDto(
    @Schema(description = "Unique week menu identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID weekId,

    @Schema(description = "Patient identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID patientId,

    @Schema(description = "Week starting date", example = "2024-01-01")
    LocalDate weekStartDate,

    @Schema(description = "Week ending date", example = "2024-01-07")
    LocalDate weekEndDate,

    @Schema(description = "Menu generation method", allowableValues = {"MANUAL", "HEURISTIC", "OPTIMIZED"})
    String generationMethod,

    @Schema(description = "Menu status", allowableValues = {"DRAFT", "GENERATED", "APPROVED", "CONSUMED", "ARCHIVED"})
    String status,

    @Schema(description = "Menu title", example = "Balanced Week Menu")
    String title,

    @Schema(description = "Menu description", example = "Low-phenylalanine weekly menu plan")
    String description,

    @Schema(description = "List of daily menus for the week")
    List<MenuDayDto> menuDays,

    @Schema(description = "Menu creation timestamp")
    LocalDateTime createdAt,

    @Schema(description = "Last menu update timestamp")
    LocalDateTime updatedAt
) {
}
