package com.chubini.pku.dishes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DishUpdateItemsDto {

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<DishItemUpdateDto> items;

    @Data
    public static class DishItemUpdateDto {
        private String itemId; // Optional: if provided, updates existing item; if null, creates new

        @NotBlank(message = "Product ID cannot be blank")
        private String productId; // UUID as string

        @DecimalMin(value = "0.01", message = "Grams must be greater than 0")
        private BigDecimal grams;
    }
}
