package com.chubini.pku.dishes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishScaleResponseDto {

    private BigDecimal originalGrams;
    private BigDecimal targetGrams;
    private BigDecimal scaleFactor;
    
    // Scaled nutritional values
    private DishResponseDto.NutritionalValues scaledValues;
}
