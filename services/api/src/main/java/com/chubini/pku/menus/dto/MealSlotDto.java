package com.chubini.pku.menus.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Meal slot information within a daily menu")
public record MealSlotDto(
    @Schema(
            description = "Unique meal slot identifier",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID slotId,
    @Schema(description = "Day menu identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID dayId,
    @Schema(
            description = "Slot name/type",
            allowableValues = {
              "BREAKFAST",
              "MORNING_SNACK",
              "LUNCH",
              "AFTERNOON_SNACK",
              "DINNER",
              "EVENING_SNACK"
            })
        String slotName,
    @Schema(description = "Suggested meal time", example = "08:00") LocalTime suggestedTime,
    @Schema(description = "Display order for the meal", example = "1") Integer displayOrder,
    @Schema(description = "Slot-specific notes", example = "Light breakfast option") String notes,
    @Schema(description = "Total phenylalanine for this meal in mg", example = "45.2")
        BigDecimal totalPheMg,
    @Schema(description = "Total protein for this meal in grams", example = "3.1")
        BigDecimal totalProteinG,
    @Schema(description = "Total calories for this meal", example = "320") BigDecimal totalKcal,
    @Schema(description = "Total fat for this meal in grams", example = "12.5")
        BigDecimal totalFatG,
    @Schema(description = "List of menu entries in this slot") List<MenuEntryDto> menuEntries,
    @Schema(description = "Meal slot creation timestamp") LocalDateTime createdAt,
    @Schema(description = "Last meal slot update timestamp") LocalDateTime updatedAt) {}
