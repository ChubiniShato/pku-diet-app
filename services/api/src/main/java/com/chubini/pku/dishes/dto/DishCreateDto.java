package com.chubini.pku.dishes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DishCreateDto {

    @NotBlank(message = "Dish name cannot be blank")
    private String name;

    private String category;

    @DecimalMin(value = "0.01", message = "Manual serving grams must be greater than 0")
    private BigDecimal manualServingGrams; // Optional: if provided, enables manual override

    @NotEmpty(message = "Dish must have at least one item")
    @Valid
    private List<DishItemCreateDto> items;

    @Data
    public static class DishItemCreateDto {
        @NotBlank(message = "Product ID cannot be blank")
        private String productId; // UUID as string

        @DecimalMin(value = "0.01", message = "Grams must be greater than 0")
        private BigDecimal grams;
    }
}
