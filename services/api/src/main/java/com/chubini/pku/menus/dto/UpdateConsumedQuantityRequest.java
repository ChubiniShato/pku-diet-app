package com.chubini.pku.menus.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Request to update consumed quantity for a menu entry")
public record UpdateConsumedQuantityRequest(
    @Schema(
            description = "Consumed quantity in grams",
            example = "75.5",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero(message = "Consumed quantity must be non-negative")
        BigDecimal consumedQty) {}
