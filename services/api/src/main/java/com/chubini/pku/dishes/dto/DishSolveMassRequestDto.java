package com.chubini.pku.dishes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishSolveMassRequestDto {

    @NotNull(message = "Target phenylalanine cannot be null")
    @DecimalMin(value = "0.0", message = "Target phenylalanine must be non-negative")
    private BigDecimal targetPhenylalanine;

    @DecimalMin(value = "0.0", message = "Target protein must be non-negative")
    private BigDecimal targetProtein; // Optional: secondary target
}
