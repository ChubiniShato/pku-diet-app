package com.chubini.pku.api.products;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FoodProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String unit; // g/ml

    @Column(name="protein_100g", nullable = false)
    private Double proteinPer100g;

    @Column(name="phe_100g", nullable = false)
    private Double phePer100g;

    @Column(name="kcal_100g", nullable = false)
    private Integer kcalPer100g;

    private String category;

    @Column(name="is_active", nullable = false)
    private Boolean isActive = true;
}
