package com.chubini.pku.products;

import java.math.BigDecimal;
import java.util.UUID;

/** DTO for localized product view with fallback to English */
public record ProductDto(
    UUID id,
    String productCode,
    String name,
    String category,
    BigDecimal phenylalanine,
    BigDecimal leucine,
    BigDecimal tyrosine,
    BigDecimal methionine,
    BigDecimal kilojoules,
    BigDecimal kilocalories,
    BigDecimal protein,
    BigDecimal carbohydrates,
    BigDecimal fats) {}
