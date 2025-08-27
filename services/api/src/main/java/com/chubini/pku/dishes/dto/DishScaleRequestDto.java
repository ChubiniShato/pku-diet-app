package com.chubini.pku.dishes.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishScaleRequestDto {

    @NotNull(message = "Target grams cannot be null")
    @DecimalMin(value = "0.01", message = "Target grams must be greater than 0")
    private BigDecimal targetGrams;
}
