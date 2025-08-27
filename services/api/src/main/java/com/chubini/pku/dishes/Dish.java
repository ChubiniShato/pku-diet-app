package com.chubini.pku.dishes;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dishes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Dish name cannot be blank")
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "nominal_serving_grams", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Nominal serving grams cannot be null")
    @DecimalMin(value = "0.01", message = "Nominal serving grams must be greater than 0")
    private BigDecimal nominalServingGrams;

    @Column(name = "manual_serving_override", nullable = false)
    @Builder.Default
    private Boolean manualServingOverride = false;

    // Total nutritional values (based on nominal serving)
    @Column(name = "total_phenylalanine", precision = 10, scale = 2)
    private BigDecimal totalPhenylalanine;

    @Column(name = "total_leucine", precision = 10, scale = 2)
    private BigDecimal totalLeucine;

    @Column(name = "total_tyrosine", precision = 10, scale = 2)
    private BigDecimal totalTyrosine;

    @Column(name = "total_methionine", precision = 10, scale = 2)
    private BigDecimal totalMethionine;

    @Column(name = "total_kilojoules", precision = 10, scale = 2)
    private BigDecimal totalKilojoules;

    @Column(name = "total_kilocalories", precision = 10, scale = 2)
    private BigDecimal totalKilocalories;

    @Column(name = "total_protein", precision = 10, scale = 2)
    private BigDecimal totalProtein;

    @Column(name = "total_carbohydrates", precision = 10, scale = 2)
    private BigDecimal totalCarbohydrates;

    @Column(name = "total_fats", precision = 10, scale = 2)
    private BigDecimal totalFats;

    // Per 100g nutritional values (normalized)
    @Column(name = "per100_phenylalanine", precision = 10, scale = 2)
    private BigDecimal per100Phenylalanine;

    @Column(name = "per100_leucine", precision = 10, scale = 2)
    private BigDecimal per100Leucine;

    @Column(name = "per100_tyrosine", precision = 10, scale = 2)
    private BigDecimal per100Tyrosine;

    @Column(name = "per100_methionine", precision = 10, scale = 2)
    private BigDecimal per100Methionine;

    @Column(name = "per100_kilojoules", precision = 10, scale = 2)
    private BigDecimal per100Kilojoules;

    @Column(name = "per100_kilocalories", precision = 10, scale = 2)
    private BigDecimal per100Kilocalories;

    @Column(name = "per100_protein", precision = 10, scale = 2)
    private BigDecimal per100Protein;

    @Column(name = "per100_carbohydrates", precision = 10, scale = 2)
    private BigDecimal per100Carbohydrates;

    @Column(name = "per100_fats", precision = 10, scale = 2)
    private BigDecimal per100Fats;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DishItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for managing items
    public void addItem(DishItem item) {
        items.add(item);
        item.setDish(this);
    }

    public void removeItem(DishItem item) {
        items.remove(item);
        item.setDish(null);
    }

    public void clearItems() {
        items.forEach(item -> item.setDish(null));
        items.clear();
    }
}
