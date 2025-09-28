package com.chubini.pku.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import com.chubini.pku.generator.dto.FoodCandidate;
import com.chubini.pku.menus.MealSlot;
import com.chubini.pku.menus.MenuEntry;
import com.chubini.pku.norms.dto.NormPrescriptionDto;
import com.chubini.pku.products.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScoringEngineTest {

  private ScoringEngine scoringEngine;
  private NormPrescriptionDto testNorm;
  private MealSlot testSlot;
  private FoodCandidate testCandidate;

  @BeforeEach
  void setUp() {
    scoringEngine = new ScoringEngine();

    // Create test norm
    testNorm =
        new NormPrescriptionDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("300.00"), // PHE limit
            new BigDecimal("15.00"), // Protein limit
            new BigDecimal("1800.00"), // Kcal min
            new BigDecimal("2200.00"), // Kcal max
            new BigDecimal("60.00"), // Fat limit
            LocalDate.now(),
            null,
            "Dr. Test",
            "Test notes",
            null,
            null);

    // Create test meal slot
    testSlot =
        MealSlot.builder()
            .id(UUID.randomUUID())
            .slotName(MealSlot.SlotName.LUNCH)
            .menuEntries(new ArrayList<>())
            .build();

    // Create test product
    Product testProduct =
        Product.builder()
            .id(UUID.randomUUID())
            .productName("Test Product")
            .phenylalanine(BigDecimal.valueOf(100))
            .protein(BigDecimal.valueOf(5))
            .kilocalories(BigDecimal.valueOf(200))
            .fats(BigDecimal.valueOf(2))
            .build();

    // Create test candidate with calculated nutrition values
    testCandidate =
        FoodCandidate.builder()
            .entryType(MenuEntry.EntryType.PRODUCT)
            .product(testProduct)
            .suggestedServingGrams(BigDecimal.valueOf(100))
            .costPerServing(BigDecimal.valueOf(2.50))
            .availableInPantry(false)
            // Calculate nutrition for 100g serving
            .calculatedPheMg(BigDecimal.valueOf(100)) // 100mg/100g * 100g / 100 = 100mg
            .calculatedProteinG(BigDecimal.valueOf(5)) // 5g/100g * 100g / 100 = 5g
            .calculatedKcal(200) // 200kcal/100g * 100g / 100 = 200kcal
            .calculatedFatG(BigDecimal.valueOf(2)) // 2g/100g * 100g / 100 = 2g
            .build();
  }

  @Test
  void testCalculateScore_ValidCandidate_ReturnsScore() {
    // When
    BigDecimal score =
        scoringEngine.calculateScore(testCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);

    // Then
    assertThat(score).isNotNull();
    assertThat(score.compareTo(BigDecimal.ZERO)).isGreaterThanOrEqualTo(0);
  }

  @Test
  void testCalculateScore_HighPheCandidate_LowerScore() {
    // Given: candidate with high PHE content
    Product highPheProduct =
        Product.builder()
            .id(UUID.randomUUID())
            .productName("High PHE Product")
            .phenylalanine(BigDecimal.valueOf(500)) // High PHE
            .protein(BigDecimal.valueOf(5))
            .kilocalories(BigDecimal.valueOf(200))
            .fats(BigDecimal.valueOf(2))
            .build();

    FoodCandidate highPheCandidate =
        FoodCandidate.builder()
            .entryType(MenuEntry.EntryType.PRODUCT)
            .product(highPheProduct)
            .suggestedServingGrams(BigDecimal.valueOf(100))
            .costPerServing(BigDecimal.valueOf(2.50))
            .availableInPantry(false)
            // Calculate nutrition for 100g serving
            .calculatedPheMg(BigDecimal.valueOf(500)) // 500mg/100g * 100g / 100 = 500mg
            .calculatedProteinG(BigDecimal.valueOf(5)) // 5g/100g * 100g / 100 = 5g
            .calculatedKcal(200) // 200kcal/100g * 100g / 100 = 200kcal
            .calculatedFatG(BigDecimal.valueOf(2)) // 2g/100g * 100g / 100 = 2g
            .build();

    // When
    BigDecimal normalScore =
        scoringEngine.calculateScore(testCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);
    BigDecimal highPheScore =
        scoringEngine.calculateScore(
            highPheCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);

    // Then - high PHE should have higher penalty (worse score)
    // Both candidates exceed 25% threshold, but highPhe has much higher excess
    assertThat(highPheScore).isGreaterThan(normalScore); // Higher penalty = worse score
  }

  @Test
  void testCalculateScore_HighProteinCandidate_LowerScore() {
    // Given: candidate with high protein content
    Product highProteinProduct =
        Product.builder()
            .id(UUID.randomUUID())
            .productName("High Protein Product")
            .phenylalanine(BigDecimal.valueOf(50))
            .protein(BigDecimal.valueOf(25)) // High protein
            .kilocalories(BigDecimal.valueOf(200))
            .fats(BigDecimal.valueOf(2))
            .build();

    FoodCandidate highProteinCandidate =
        FoodCandidate.builder()
            .entryType(MenuEntry.EntryType.PRODUCT)
            .product(highProteinProduct)
            .suggestedServingGrams(BigDecimal.valueOf(100))
            .costPerServing(BigDecimal.valueOf(2.50))
            .availableInPantry(false)
            // Calculate nutrition for 100g serving
            .calculatedPheMg(BigDecimal.valueOf(50)) // 50mg/100g * 100g / 100 = 50mg
            .calculatedProteinG(BigDecimal.valueOf(25)) // 25g/100g * 100g / 100 = 25g
            .calculatedKcal(200) // 200kcal/100g * 100g / 100 = 200kcal
            .calculatedFatG(BigDecimal.valueOf(2)) // 2g/100g * 100g / 100 = 2g
            .build();

    // When
    BigDecimal normalScore =
        scoringEngine.calculateScore(testCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);
    BigDecimal highProteinScore =
        scoringEngine.calculateScore(
            highProteinCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);

    // Then - high protein should have higher penalty (worse score)
    // Both candidates exceed 25% threshold, but highProtein has much higher excess
    assertThat(highProteinScore).isGreaterThan(normalScore); // Higher penalty = worse score
  }

  @Test
  void testCalculateScore_ExpensiveCandidate_LowerScore() {
    // Given: expensive candidate
    FoodCandidate expensiveCandidate =
        FoodCandidate.builder()
            .entryType(MenuEntry.EntryType.PRODUCT)
            .product(testCandidate.getProduct())
            .suggestedServingGrams(BigDecimal.valueOf(100))
            .costPerServing(BigDecimal.valueOf(15.00)) // Expensive
            .availableInPantry(false)
            // Calculate nutrition for 100g serving (same as testCandidate)
            .calculatedPheMg(BigDecimal.valueOf(100))
            .calculatedProteinG(BigDecimal.valueOf(5))
            .calculatedKcal(200)
            .calculatedFatG(BigDecimal.valueOf(2))
            .build();

    // When
    BigDecimal normalScore =
        scoringEngine.calculateScore(testCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);
    BigDecimal expensiveScore =
        scoringEngine.calculateScore(
            expensiveCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);

    // Then - expensive should have higher penalty (worse score) due to cost penalty
    // Both candidates have cost penalties, but expensive has much higher cost percentage
    assertThat(expensiveScore).isGreaterThan(normalScore); // Higher penalty = worse score
  }

  @Test
  void testCalculateScore_WithRepeatPenalty_LowerScore() {
    // When - comparing no repeats vs recent repeat
    BigDecimal noRepeatScore =
        scoringEngine.calculateScore(testCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);
    BigDecimal repeatScore =
        scoringEngine.calculateScore(
            testCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 1); // Recent repeat

    // Then - repeat should have higher (worse) score due to repeat penalty
    assertThat(repeatScore).isGreaterThan(noRepeatScore);
  }

  @Test
  void testCalculateScore_StoresComponentsInCandidate() {
    // When
    scoringEngine.calculateScore(testCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);

    // Then - scoring components should be stored in candidate
    assertThat(testCandidate.getPheOverPenalty()).isNotNull();
    assertThat(testCandidate.getProteinOverPenalty()).isNotNull();
    assertThat(testCandidate.getKcalDeficitPenalty()).isNotNull();
    assertThat(testCandidate.getCostPenalty()).isNotNull();
    assertThat(testCandidate.getRepeatPenalty()).isNotNull();
  }

  @Test
  void testCalculateScore_PantryItem_BetterScore() {
    // Given: same candidate but one is available in pantry
    FoodCandidate pantryCandidate =
        FoodCandidate.builder()
            .entryType(MenuEntry.EntryType.PRODUCT)
            .product(testCandidate.getProduct())
            .suggestedServingGrams(BigDecimal.valueOf(100))
            .costPerServing(BigDecimal.valueOf(2.50))
            .availableInPantry(true) // Available in pantry
            .pantryQuantityAvailable(BigDecimal.valueOf(500))
            // Calculate nutrition for 100g serving (same as testCandidate)
            .calculatedPheMg(BigDecimal.valueOf(100))
            .calculatedProteinG(BigDecimal.valueOf(5))
            .calculatedKcal(200)
            .calculatedFatG(BigDecimal.valueOf(2))
            .build();

    // When
    BigDecimal normalScore =
        scoringEngine.calculateScore(testCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);
    BigDecimal pantryScore =
        scoringEngine.calculateScore(
            pantryCandidate, testSlot, testNorm, BigDecimal.valueOf(25), 0);

    // Then - pantry item should have better score (pantry preference bonus)
    assertThat(pantryScore).isLessThanOrEqualTo(normalScore);
  }
}
