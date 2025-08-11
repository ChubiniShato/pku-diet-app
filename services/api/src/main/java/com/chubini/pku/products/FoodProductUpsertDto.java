package com.chubini.pku.api.products;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record FoodProductUpsertDto(
    @NotBlank String name,
    @NotBlank String unit,
    @NotNull @DecimalMin("0.0") BigDecimal proteinPer100g, // ✅
    @NotNull @DecimalMin("0.0") BigDecimal phePer100g,      // ✅
    @NotNull @Min(0) Integer kcalPer100g,
    String category,
    Boolean isActive
) {}
