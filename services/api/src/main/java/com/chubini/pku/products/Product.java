package com.chubini.pku.products;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_number")
    private Integer productNumber;

    @Column(name = "category")
    private String category;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "phenylalanine")
    private BigDecimal phenylalanine;

    @Column(name = "leucine")
    private BigDecimal leucine;

    @Column(name = "tyrosine")
    private BigDecimal tyrosine;

    @Column(name = "methionine")
    private BigDecimal methionine;

    @Column(name = "kilojoules")
    private Integer kilojoules;

    @Column(name = "kilocalories")
    private Integer kilocalories;

    @Column(name = "protein")
    private BigDecimal protein;

    @Column(name = "carbohydrates")
    private BigDecimal carbohydrates;

    @Column(name = "fats")
    private BigDecimal fats;
}