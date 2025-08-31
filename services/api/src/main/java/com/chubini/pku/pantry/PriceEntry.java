package com.chubini.pku.pantry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chubini.pku.products.CustomProduct;
import com.chubini.pku.products.Product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "price_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "custom_product_id")
  private CustomProduct customProduct;

  @Enumerated(EnumType.STRING)
  @Column(name = "item_type", nullable = false)
  private PantryItem.ItemType itemType;

  @Column(name = "store_name")
  private String storeName;

  @Column(name = "region")
  private String region;

  @Column(name = "price_per_unit", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerUnit;

  @Column(name = "unit_size_grams", nullable = false, precision = 10, scale = 2)
  private BigDecimal unitSizeGrams;

  @Column(name = "currency", nullable = false)
  @Builder.Default
  private String currency = "USD";

  @Column(name = "recorded_date", nullable = false)
  @Builder.Default
  private LocalDate recordedDate = LocalDate.now();

  @Column(name = "is_current", nullable = false)
  @Builder.Default
  private Boolean isCurrent = true;

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

  /** Calculate price per gram */
  public BigDecimal getPricePerGram() {
    if (pricePerUnit == null
        || unitSizeGrams == null
        || unitSizeGrams.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return pricePerUnit.divide(unitSizeGrams, 4, java.math.RoundingMode.HALF_UP);
  }

  /** Get the item name regardless of type */
  public String getItemName() {
    return switch (itemType) {
      case PRODUCT -> product != null ? product.getProductName() : null;
      case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getName() : null;
    };
  }

  /** Check if this price entry is recent (within 30 days) */
  public boolean isRecent() {
    return recordedDate != null && recordedDate.isAfter(LocalDate.now().minusDays(30));
  }
}
