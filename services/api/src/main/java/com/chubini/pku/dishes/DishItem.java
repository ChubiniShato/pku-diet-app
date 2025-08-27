package com.chubini.pku.dishes;

import com.chubini.pku.products.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dish_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DishItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    @NotNull(message = "Dish cannot be null")
    private Dish dish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product cannot be null")
    private Product product;

    @Column(name = "grams", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "Grams cannot be null")
    @DecimalMin(value = "0.01", message = "Grams must be greater than 0")
    private BigDecimal grams;

    // Snapshot of product nutritional values per 100g at creation time
    // This ensures historical calculations remain valid even if product data changes
    @Column(name = "snapshot_phenylalanine", precision = 10, scale = 2)
    private BigDecimal snapshotPhenylalanine;

    @Column(name = "snapshot_leucine", precision = 10, scale = 2)
    private BigDecimal snapshotLeucine;

    @Column(name = "snapshot_tyrosine", precision = 10, scale = 2)
    private BigDecimal snapshotTyrosine;

    @Column(name = "snapshot_methionine", precision = 10, scale = 2)
    private BigDecimal snapshotMethionine;

    @Column(name = "snapshot_kilojoules")
    private Integer snapshotKilojoules;

    @Column(name = "snapshot_kilocalories")
    private Integer snapshotKilocalories;

    @Column(name = "snapshot_protein", precision = 10, scale = 2)
    private BigDecimal snapshotProtein;

    @Column(name = "snapshot_carbohydrates", precision = 10, scale = 2)
    private BigDecimal snapshotCarbohydrates;

    @Column(name = "snapshot_fats", precision = 10, scale = 2)
    private BigDecimal snapshotFats;

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

    // Helper method to capture product snapshot
    public void captureProductSnapshot(Product product) {
        this.snapshotPhenylalanine = product.getPhenylalanine();
        this.snapshotLeucine = product.getLeucine();
        this.snapshotTyrosine = product.getTyrosine();
        this.snapshotMethionine = product.getMethionine();
        this.snapshotKilojoules = product.getKilojoules();
        this.snapshotKilocalories = product.getKilocalories();
        this.snapshotProtein = product.getProtein();
        this.snapshotCarbohydrates = product.getCarbohydrates();
        this.snapshotFats = product.getFats();
    }

    // Helper methods to get snapshot values as BigDecimal for calculations
    public BigDecimal getSnapshotKilojoulesAsBigDecimal() {
        return snapshotKilojoules != null ? BigDecimal.valueOf(snapshotKilojoules) : BigDecimal.ZERO;
    }

    public BigDecimal getSnapshotKilocaloriesAsBigDecimal() {
        return snapshotKilocalories != null ? BigDecimal.valueOf(snapshotKilocalories) : BigDecimal.ZERO;
    }
}
