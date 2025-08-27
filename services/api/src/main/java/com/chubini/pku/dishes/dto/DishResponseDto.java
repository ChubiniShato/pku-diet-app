package com.chubini.pku.dishes.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class DishResponseDto {

    private UUID id;
    private String name;
    private String category;
    private BigDecimal nominalServingGrams;
    private Boolean manualServingOverride;

    // Total nutritional values
    private NutritionalValues totalValues;

    // Per 100g nutritional values
    private NutritionalValues per100Values;

    private List<DishItemResponseDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class NutritionalValues {
        private BigDecimal phenylalanine;
        private BigDecimal leucine;
        private BigDecimal tyrosine;
        private BigDecimal methionine;
        private BigDecimal kilojoules;
        private BigDecimal kilocalories;
        private BigDecimal protein;
        private BigDecimal carbohydrates;
        private BigDecimal fats;
    }

    @Data
    public static class DishItemResponseDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private String productCategory;
        private BigDecimal grams;
        private NutritionalValues contribution; // Contribution to dish totals
        private LocalDateTime createdAt;
    }
}
