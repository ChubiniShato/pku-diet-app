package com.chubini.pku.api.products;

import jakarta.validation.constraints.*;

public record FoodProductUpsertDto(
    @NotBlank String name,
    @NotBlank String unit,
    @NotNull @DecimalMin("0.0") Double proteinPer100g,
    @NotNull @DecimalMin("0.0") Double phePer100g,
    @NotNull @Min(0) Integer kcalPer100g,
    String category,
    Boolean isActive
) {}
