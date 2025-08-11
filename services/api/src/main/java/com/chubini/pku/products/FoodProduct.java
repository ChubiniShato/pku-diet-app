package com.chubini.pku.api.products;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.math.BigDecimal; // ✅ დავამატოთ

@Entity
@Table(name = "food_products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoodProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String unit; // g/ml

    // ✅ numeric(8,3) ↔ BigDecimal + precision/scale
    @Column(name = "protein_100g", nullable = false, precision = 8, scale = 3)
    private BigDecimal proteinPer100g;

    @Column(name = "phe_100g", nullable = false, precision = 8, scale = 3)
    private BigDecimal phePer100g;

    // kcal_100g არის INT → Integer სწორია
    @Column(name = "kcal_100g", nullable = false)
    private Integer kcalPer100g;

    private String category;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
