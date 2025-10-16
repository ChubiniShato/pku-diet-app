package com.chubini.pku.pantry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;
import com.chubini.pku.products.CustomProduct;
import com.chubini.pku.products.Product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pantry_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PantryItem {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private PatientProfile patient;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "custom_product_id")
  private CustomProduct customProduct;

  @Enumerated(EnumType.STRING)
  @Column(name = "item_type", nullable = false)
  private ItemType itemType;

  @Column(name = "quantity_grams", nullable = false, precision = 10, scale = 2)
  private BigDecimal quantityGrams;

  @Column(name = "purchase_date")
  private LocalDate purchaseDate;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;

  @Column(name = "location")
  private String location; // FRIDGE, PANTRY, FREEZER

  @Column(name = "cost_per_unit", precision = 10, scale = 2)
  private BigDecimal costPerUnit;

  @Column(name = "currency")
  @Builder.Default
  private String currency = "USD";

  @Column(name = "notes")
  private String notes;

  @Column(name = "is_available", nullable = false)
  @Builder.Default
  private Boolean isAvailable = true;

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

  public enum ItemType {
    PRODUCT,
    CUSTOM_PRODUCT
  }

  /** Get the name of the item regardless of type */
  public String getItemName() {
    return switch (itemType) {
      case PRODUCT -> product != null ? product.getProductName() : null;
      case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getName() : null;
    };
  }

  /** Get the category of the item regardless of type */
  public String getItemCategory() {
    return switch (itemType) {
      case PRODUCT -> product != null ? product.getCategory() : null;
      case CUSTOM_PRODUCT -> customProduct != null ? customProduct.getCategory() : null;
    };
  }

  /** Check if item is expired */
  public boolean isExpired() {
    return expiryDate != null && LocalDate.now().isAfter(expiryDate);
  }

  /** Check if item is expiring soon (within 3 days) */
  public boolean isExpiringSoon() {
    return expiryDate != null && LocalDate.now().plusDays(3).isAfter(expiryDate);
  }

  /** Reserve quantity for menu planning (simulation only) */
  public boolean canReserve(BigDecimal amount) {
    return isAvailable && quantityGrams.compareTo(amount) >= 0;
  }

  /** Calculate cost per gram */
  public BigDecimal getCostPerGram() {
    if (costPerUnit == null
        || quantityGrams == null
        || quantityGrams.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return costPerUnit.divide(quantityGrams, 4, java.math.RoundingMode.HALF_UP);
  }
}
