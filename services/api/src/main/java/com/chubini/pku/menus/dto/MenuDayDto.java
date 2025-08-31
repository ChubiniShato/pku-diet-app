package com.chubini.pku.menus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Daily menu information")
public record MenuDayDto(
    @Schema(
            description = "Unique day menu identifier",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID dayId,
    @Schema(description = "Week menu identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID weekId,
    @Schema(description = "Menu date", example = "2024-01-01") LocalDate menuDate,
    @Schema(
            description = "Day of week",
            allowableValues = {
              "MONDAY",
              "TUESDAY",
              "WEDNESDAY",
              "THURSDAY",
              "FRIDAY",
              "SATURDAY",
              "SUNDAY"
            })
        String dayOfWeek,
    @Schema(description = "Day menu title", example = "Monday Balanced Menu") String title,
    @Schema(
            description = "Special notes for the day",
            example = "High protein breakfast recommended")
        String notes,
    @Schema(description = "Total phenylalanine for the day in mg", example = "280.5")
        BigDecimal totalPheMg,
    @Schema(description = "Total protein for the day in grams", example = "14.2")
        BigDecimal totalProteinG,
    @Schema(description = "Total calories for the day", example = "1950") BigDecimal totalKcal,
    @Schema(description = "Total fat for the day in grams", example = "58.3") BigDecimal totalFatG,
    @Schema(description = "List of meal slots for the day") List<MealSlotDto> mealSlots,
    @Schema(description = "Menu creation timestamp") LocalDateTime createdAt,
    @Schema(description = "Last menu update timestamp") LocalDateTime updatedAt) {}
