package com.chubini.pku.dishes;

import jakarta.persistence.*;
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
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;

    @Column(name = "nominal_serving_grams", nullable = false, precision = 8, scale = 2)
    private BigDecimal nominalServingGrams;

    @Column(name = "manual_serving_override", nullable = false)
    @Builder.Default
    private Boolean manualServingOverride = false;

    // Total nutritional values for the nominal serving
    @Column(name = "total_phenylalanine", precision = 8, scale = 2)
    private BigDecimal totalPhenylalanine;

    @Column(name = "total_leucine", precision = 8, scale = 2)
    private BigDecimal totalLeucine;

    @Column(name = "total_tyrosine", precision = 8, scale = 2)
    private BigDecimal totalTyrosine;

    @Column(name = "total_methionine", precision = 8, scale = 2)
    private BigDecimal totalMethionine;

    @Column(name = "total_kilojoules", precision = 8, scale = 2)
    private BigDecimal totalKilojoules;

    @Column(name = "total_kilocalories", precision = 8, scale = 2)
    private BigDecimal totalKilocalories;

    @Column(name = "total_protein", precision = 8, scale = 2)
    private BigDecimal totalProtein;

    @Column(name = "total_carbohydrates", precision = 8, scale = 2)
    private BigDecimal totalCarbohydrates;

    @Column(name = "total_fats", precision = 8, scale = 2)
    private BigDecimal totalFats;

    // Per 100g nutritional values
    @Column(name = "per100_phenylalanine", precision = 8, scale = 2)
    private BigDecimal per100Phenylalanine;

    @Column(name = "per100_leucine", precision = 8, scale = 2)
    private BigDecimal per100Leucine;

    @Column(name = "per100_tyrosine", precision = 8, scale = 2)
    private BigDecimal per100Tyrosine;

    @Column(name = "per100_methionine", precision = 8, scale = 2)
    private BigDecimal per100Methionine;

    @Column(name = "per100_kilojoules", precision = 8, scale = 2)
    private BigDecimal per100Kilojoules;

    @Column(name = "per100_kilocalories", precision = 8, scale = 2)
    private BigDecimal per100Kilocalories;

    @Column(name = "per100_protein", precision = 8, scale = 2)
    private BigDecimal per100Protein;

    @Column(name = "per100_carbohydrates", precision = 8, scale = 2)
    private BigDecimal per100Carbohydrates;

    @Column(name = "per100_fats", precision = 8, scale = 2)
    private BigDecimal per100Fats;

    @Column(name = "preparation_time_minutes")
    private Integer preparationTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    @Column(name = "recipe_instructions", columnDefinition = "TEXT")
    private String recipeInstructions;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "is_visible", nullable = false)
    @Builder.Default
    private Boolean isVisible = true;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DishIngredient> ingredients = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
}
