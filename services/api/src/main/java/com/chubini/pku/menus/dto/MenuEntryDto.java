package com.chubini.pku.menus.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Individual menu entry (product or dish) within a meal slot")
public record MenuEntryDto(
    @Schema(
            description = "Unique menu entry identifier",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID entryId,
    @Schema(description = "Meal slot identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID slotId,
    @Schema(
            description = "Entry type",
            allowableValues = {"PRODUCT", "DISH", "CUSTOM_PRODUCT", "CUSTOM_DISH"})
        String entryType,
    @Schema(
            description = "Referenced item ID (product or dish)",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID itemId,
    @Schema(description = "Item name for display", example = "Low-protein bread") String itemName,
    @Schema(description = "Serving quantity", example = "2.5") BigDecimal quantity,
    @Schema(description = "Serving unit", example = "slices") String unit,
    @Schema(description = "Calculated phenylalanine for this serving in mg", example = "15.3")
        BigDecimal calculatedPheMg,
    @Schema(description = "Calculated protein for this serving in grams", example = "1.2")
        BigDecimal calculatedProteinG,
    @Schema(description = "Calculated calories for this serving", example = "85")
        BigDecimal calculatedKcal,
    @Schema(description = "Calculated fat for this serving in grams", example = "2.1")
        BigDecimal calculatedFatG,
    @Schema(description = "Whether this entry has been consumed", example = "false")
        Boolean consumed,
    @Schema(description = "Entry-specific notes", example = "Extra toasted") String notes,
    @Schema(description = "Display order within the slot", example = "1") Integer displayOrder,
    @Schema(description = "Menu entry creation timestamp") LocalDateTime createdAt,
    @Schema(description = "Last menu entry update timestamp") LocalDateTime updatedAt) {}
