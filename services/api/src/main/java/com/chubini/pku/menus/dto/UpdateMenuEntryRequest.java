package com.chubini.pku.menus.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request to update a menu entry")
public record UpdateMenuEntryRequest(
    @Schema(description = "Serving quantity", example = "2.5")
        @Positive(message = "Quantity must be positive")
        BigDecimal quantity,
    @Schema(description = "Serving unit", example = "slices") String unit,
    @Schema(description = "Whether this entry has been consumed", example = "true")
        Boolean consumed,
    @Schema(description = "Entry-specific notes", example = "Extra toasted") String notes,
    @Schema(description = "Display order within the slot", example = "1") Integer displayOrder) {}
