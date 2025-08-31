package com.chubini.pku.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import com.chubini.pku.products.Product;
import com.chubini.pku.validation.dto.NutritionBreakdown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NutritionScalerTest {

  @InjectMocks private NutritionScaler nutritionScaler;

  private Product testProduct;

  @BeforeEach
  void setUp() {
    testProduct =
        Product.builder()
            .productName("Test Potato")
            .phenylalanine(new BigDecimal("100.00")) // per 100g
            .protein(new BigDecimal("2.00")) // per 100g
            .kilocalories(new BigDecimal("71.00")) // per 100g
            .fats(new BigDecimal("0.10")) // per 100g
            .build();
  }

  @Test
  void testScaleProductNutrition_ExactQuantity() {
    // Given: 100g of the product (should match per-100g values exactly)
    BigDecimal quantity = new BigDecimal("100.00");

    // When
    NutritionBreakdown result = nutritionScaler.from(testProduct, quantity, "G");

    // Then
    assertThat(result.pheMg()).isEqualTo(new BigDecimal("100.00"));
    assertThat(result.proteinG()).isEqualTo(new BigDecimal("2.00"));
    assertThat(result.kcal()).isEqualTo(71);
    assertThat(result.fatG()).isEqualTo(new BigDecimal("0.10"));
    assertThat(result.quantity()).isEqualTo(quantity);
    assertThat(result.unit()).isEqualTo("G");
  }

  @Test
  void testScaleProductNutrition_HalfQuantity() {
    // Given: 50g of the product (should be half of per-100g values)
    BigDecimal quantity = new BigDecimal("50.00");

    // When
    NutritionBreakdown result = nutritionScaler.from(testProduct, quantity, "G");

    // Then
    assertThat(result.pheMg()).isEqualTo(new BigDecimal("50.00"));
    assertThat(result.proteinG()).isEqualTo(new BigDecimal("1.00"));
    assertThat(result.kcal()).isEqualTo(36); // 71 * 0.5 = 35.5, rounded to 36
    assertThat(result.fatG()).isEqualTo(new BigDecimal("0.05"));
  }

  @Test
  void testScaleProductNutrition_DoubleQuantity() {
    // Given: 200g of the product (should be double of per-100g values)
    BigDecimal quantity = new BigDecimal("200.00");

    // When
    NutritionBreakdown result = nutritionScaler.from(testProduct, quantity, "G");

    // Then
    assertThat(result.pheMg()).isEqualTo(new BigDecimal("200.00"));
    assertThat(result.proteinG()).isEqualTo(new BigDecimal("4.00"));
    assertThat(result.kcal()).isEqualTo(142);
    assertThat(result.fatG()).isEqualTo(new BigDecimal("0.20"));
  }

  @Test
  void testScaleProductNutrition_ZeroQuantity() {
    // Given: 0g of the product
    BigDecimal quantity = BigDecimal.ZERO;

    // When
    NutritionBreakdown result = nutritionScaler.from(testProduct, quantity, "G");

    // Then - should return zero breakdown
    assertThat(result.pheMg()).isEqualTo(BigDecimal.ZERO);
    assertThat(result.proteinG()).isEqualTo(BigDecimal.ZERO);
    assertThat(result.kcal()).isEqualTo(0);
    assertThat(result.fatG()).isEqualTo(BigDecimal.ZERO);
  }

  @Test
  void testScaleProductNutrition_NullProduct() {
    // Given: null product
    BigDecimal quantity = new BigDecimal("100.00");

    // When
    NutritionBreakdown result = nutritionScaler.from((Product) null, quantity, "G");

    // Then - should return zero breakdown
    assertThat(result.pheMg()).isEqualTo(BigDecimal.ZERO);
    assertThat(result.proteinG()).isEqualTo(BigDecimal.ZERO);
    assertThat(result.kcal()).isEqualTo(0);
    assertThat(result.fatG()).isEqualTo(BigDecimal.ZERO);
  }

  @Test
  void testScaleProductNutrition_NullQuantity() {
    // Given: null quantity

    // When
    NutritionBreakdown result = nutritionScaler.from(testProduct, null, "G");

    // Then - should return zero breakdown
    assertThat(result.pheMg()).isEqualTo(BigDecimal.ZERO);
    assertThat(result.proteinG()).isEqualTo(BigDecimal.ZERO);
    assertThat(result.kcal()).isEqualTo(0);
    assertThat(result.fatG()).isEqualTo(BigDecimal.ZERO);
  }

  @Test
  void testNutritionBreakdownZero() {
    // When
    NutritionBreakdown zero = NutritionBreakdown.zero();

    // Then
    assertThat(zero.pheMg()).isEqualTo(BigDecimal.ZERO);
    assertThat(zero.proteinG()).isEqualTo(BigDecimal.ZERO);
    assertThat(zero.kcal()).isEqualTo(0);
    assertThat(zero.fatG()).isEqualTo(BigDecimal.ZERO);
    assertThat(zero.quantity()).isEqualTo(BigDecimal.ZERO);
    assertThat(zero.unit()).isEqualTo("G");
  }

  @Test
  void testNutritionBreakdownAdd() {
    // Given: two nutrition breakdowns
    NutritionBreakdown first =
        new NutritionBreakdown(
            new BigDecimal("50.00"),
            new BigDecimal("1.00"),
            35,
            new BigDecimal("0.05"),
            new BigDecimal("50.00"),
            "G");

    NutritionBreakdown second =
        new NutritionBreakdown(
            new BigDecimal("30.00"),
            new BigDecimal("0.50"),
            20,
            new BigDecimal("0.03"),
            new BigDecimal("30.00"),
            "G");

    // When
    NutritionBreakdown result = first.add(second);

    // Then
    assertThat(result.pheMg()).isEqualTo(new BigDecimal("80.00"));
    assertThat(result.proteinG()).isEqualTo(new BigDecimal("1.50"));
    assertThat(result.kcal()).isEqualTo(55);
    assertThat(result.fatG()).isEqualTo(new BigDecimal("0.08"));
    assertThat(result.quantity()).isEqualTo(new BigDecimal("80.00"));
  }
}
