package com.chubini.pku.products;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "product_number")
  private Integer productNumber;

  @Column(name = "product_code", unique = true, nullable = false)
  private String productCode;

  @Column(name = "category")
  private String category;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonManagedReference
  private List<ProductTranslation> translations;

  @Column(name = "phenylalanine")
  private BigDecimal phenylalanine;

  @Column(name = "leucine")
  private BigDecimal leucine;

  @Column(name = "tyrosine")
  private BigDecimal tyrosine;

  @Column(name = "methionine")
  private BigDecimal methionine;

  @Column(name = "kilojoules")
  private BigDecimal kilojoules;

  @Column(name = "kilocalories")
  private BigDecimal kilocalories;

  @Column(name = "protein")
  private BigDecimal protein;

  @Column(name = "carbohydrates")
  private BigDecimal carbohydrates;

  @Column(name = "fats")
  private BigDecimal fats;
}
