package com.chubini.pku.dishes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishSolveMassResponseDto {

    private BigDecimal requiredGrams;
    private BigDecimal scaleFactor;
    
    // Target values
    private BigDecimal targetPhenylalanine;
    private BigDecimal targetProtein;
    
    // Achieved values
    private BigDecimal achievedPhenylalanine;
    private BigDecimal achievedProtein;
    
    // Complete nutritional profile at required grams
    private DishResponseDto.NutritionalValues achievedValues;
    
    // Indicates if protein target was also met (if provided)
    private Boolean proteinTargetMet;
    private String message; // Explanation of the result
}
