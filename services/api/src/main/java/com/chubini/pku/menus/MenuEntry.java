package com.chubini.pku.menus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chubini.pku.dishes.CustomDish;
import com.chubini.pku.dishes.Dish;
import com.chubini.pku.products.CustomProduct;
import com.chubini.pku.products.Product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "meal_slot_id", nullable = false)
  private MealSlot mealSlot;

  @Enumerated(EnumType.STRING)
  @Column(name = "entry_type", nullable = false)
  private EntryType entryType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "custom_product_id")
  private CustomProduct customProduct;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dish_id")
  private Dish dish;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "custom_dish_id")
  private CustomDish customDish;

  @Column(name = "planned_serving_grams", nullable = false, precision = 8, scale = 2)
  private BigDecimal plannedServingGrams;

  @Column(name = "actual_serving_grams", precision = 8, scale = 2)
  private BigDecimal actualServingGrams;

  @Column(name = "consumed_qty", precision = 8, scale = 2)
  private BigDecimal consumedQty;

  @Column(name = "calculated_phe_mg", precision = 8, scale = 2)
  private BigDecimal calculatedPheMg;

  @Column(name = "calculated_protein_g", precision = 8, scale = 2)
  private BigDecimal calculatedProteinG;

  @Column(name = "calculated_kcal", precision = 8, scale = 2)
  private BigDecimal calculatedKcal;

  @Column(name = "calculated_fat_g", precision = 8, scale = 2)
  private BigDecimal calculatedFatG;

  @Column(name = "is_alternative", nullable = false)
  @Builder.Default
  private Boolean isAlternative = false;

  @Column(name = "alternative_group")
  private Integer alternativeGroup;

  @Column(name = "is_consumed", nullable = false)
  @Builder.Default
  private Boolean isConsumed = false;

  @Column(name = "notes")
  private String notes;

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

  public enum EntryType {
    PRODUCT,
    CUSTOM_PRODUCT,
    DISH,
    CUSTOM_DISH
  }

  // Helper methods to get the actual food item regardless of type
  public String getItemName() {
    return switch (entryType) {
      case PRODUCT -> product != null ? product.getProductName() : null;
      case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getName() : null;
      case DISH -> dish != null ? dish.getName() : null;
      case CUSTOM_DISH -> customDish != null ? customDish.getName() : null;
    };
  }

  public String getItemCategory() {
    return switch (entryType) {
      case PRODUCT -> product != null ? product.getCategory() : null;
      case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getCategory() : null;
      case DISH -> dish != null ? dish.getCategory() : null;
      case CUSTOM_DISH -> customDish != null ? customDish.getCategory() : null;
    };
  }

  /** Get the effective consumed quantity - uses consumedQty if set, otherwise actualServingGrams */
  public BigDecimal getEffectiveConsumedQuantity() {
    if (consumedQty != null) {
      return consumedQty;
    }
    return actualServingGrams;
  }

  /** Update consumed quantity and mark as consumed */
  public void updateConsumedQuantity(BigDecimal quantity) {
    this.consumedQty = quantity;
    this.isConsumed = quantity != null && quantity.compareTo(BigDecimal.ZERO) > 0;
  }

  /** Check if this entry has been partially consumed */
  public boolean isPartiallyConsumed() {
    if (!isConsumed || consumedQty == null || plannedServingGrams == null) {
      return false;
    }
    return consumedQty.compareTo(plannedServingGrams) < 0;
  }

  /** Check if this entry has been over-consumed */
  public boolean isOverConsumed() {
    if (!isConsumed || consumedQty == null || plannedServingGrams == null) {
      return false;
    }
    return consumedQty.compareTo(plannedServingGrams) > 0;
  }
}
