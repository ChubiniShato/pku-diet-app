package com.chubini.pku.products;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductUpsertDto(
    @NotBlank String productName,
    @NotBlank String category,
    @NotNull @DecimalMin("0.0") BigDecimal phenylalanine,
    @NotNull @DecimalMin("0.0") BigDecimal leucine,
    @NotNull @DecimalMin("0.0") BigDecimal tyrosine,
    @NotNull @DecimalMin("0.0") BigDecimal methionine,
    @NotNull @Min(0) Integer kilojoules,
    @NotNull @Min(0) Integer kilocalories,
    @NotNull @DecimalMin("0.0") BigDecimal protein,
    @NotNull @DecimalMin("0.0") BigDecimal carbohydrates,
    @NotNull @DecimalMin("0.0") BigDecimal fats
) {}
