package com.chubini.pku.products;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public record ProductUpsertDto(
    @NotBlank String productName,
    @NotBlank String category,
    @NotNull @DecimalMin("0.0") BigDecimal phenylalanine,
    @NotNull @DecimalMin("0.0") BigDecimal leucine,
    @NotNull @DecimalMin("0.0") BigDecimal tyrosine,
    @NotNull @DecimalMin("0.0") BigDecimal methionine,
    @NotNull @DecimalMin("0.0") BigDecimal kilojoules,
    @NotNull @DecimalMin("0.0") BigDecimal kilocalories,
    @NotNull @DecimalMin("0.0") BigDecimal protein,
    @NotNull @DecimalMin("0.0") BigDecimal carbohydrates,
    @NotNull @DecimalMin("0.0") BigDecimal fats) {}
