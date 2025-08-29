package com.chubini.pku.menus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request to create a new daily menu")
public record CreateMenuDayRequest(
    @Schema(description = "Week menu identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID weekId,

    @Schema(description = "Menu date", example = "2024-01-01")
    @NotNull(message = "Menu date is required")
    LocalDate menuDate,

    @Schema(description = "Day menu title", example = "Monday Balanced Menu")
    String title,

    @Schema(description = "Special notes for the day", example = "High protein breakfast recommended")
    String notes
) {
}
