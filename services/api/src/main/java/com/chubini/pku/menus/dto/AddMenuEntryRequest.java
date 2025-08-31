package com.chubini.pku.menus.dto;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request to add an entry to a meal slot")
public record AddMenuEntryRequest(
    @Schema(
            description = "Entry type",
            allowableValues = {"PRODUCT", "DISH", "CUSTOM_PRODUCT", "CUSTOM_DISH"})
        @NotNull(message = "Entry type is required")
        String entryType,
    @Schema(
            description = "Referenced item ID (product or dish)",
            example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "Item ID is required")
        UUID itemId,
    @Schema(description = "Serving quantity", example = "2.5")
        @Positive(message = "Quantity must be positive")
        BigDecimal quantity,
    @Schema(description = "Serving unit", example = "slices") String unit,
    @Schema(description = "Entry-specific notes", example = "Extra toasted") String notes,
    @Schema(description = "Display order within the slot", example = "1") Integer displayOrder) {}
