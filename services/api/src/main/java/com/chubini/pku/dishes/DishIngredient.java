package com.chubini.pku.dishes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chubini.pku.products.Product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dish_ingredient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishIngredient {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dish_id", nullable = false)
  private Dish dish;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @Column(name = "quantity_grams", nullable = false, precision = 8, scale = 2)
  private BigDecimal quantityGrams;

  @Column(name = "preparation_notes")
  private String preparationNotes;

  @Column(name = "is_optional", nullable = false)
  @Builder.Default
  private Boolean isOptional = false;

  @Column(name = "sort_order", nullable = false)
  @Builder.Default
  private Integer sortOrder = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
