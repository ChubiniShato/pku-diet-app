package com.chubini.pku.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import com.chubini.pku.generator.dto.FoodCandidate;
import com.chubini.pku.generator.dto.MenuGenerationRequest;
import com.chubini.pku.menus.MealSlot;
import com.chubini.pku.menus.MenuEntry;
import com.chubini.pku.norms.dto.NormPrescriptionDto;
import com.chubini.pku.patients.PatientProfile;
import com.chubini.pku.products.Product;
import com.chubini.pku.products.ProductRepository;
import com.chubini.pku.validation.NutritionScaler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnhancedFoodSelectionServiceTest {

  @Mock private ProductRepository productRepository;

  @Mock private NutritionScaler nutritionScaler;

  @Mock private ScoringEngine scoringEngine;

  @Mock private VarietyEngine varietyEngine;

  @Mock private PantryAwareService pantryAwareService;

  @InjectMocks private EnhancedFoodSelectionService enhancedFoodSelectionService;

  private PatientProfile testPatient;
  private NormPrescriptionDto testNorm;
  private MenuGenerationRequest testRequest;
  private MealSlot testSlot;

  @BeforeEach
  void setUp() {
    testPatient = PatientProfile.builder().id(UUID.randomUUID()).name("Test Patient").build();

    testNorm =
        new NormPrescriptionDto(
            UUID.randomUUID(),
            testPatient.getId(),
            BigDecimal.valueOf(500),
            BigDecimal.valueOf(15),
            BigDecimal.valueOf(1800),
            BigDecimal.valueOf(2200),
            BigDecimal.valueOf(60),
            LocalDate.now(),
            null,
            "Dr. Test",
            "Test notes",
            null,
            null);

    testRequest =
        new MenuGenerationRequest(
            testPatient.getId(),
            LocalDate.now(),
            "DAILY",
            null, // preferredCategories
            null, // foodsToAvoid
            null, // maxPhePerMeal
            null, // targetCaloriesPerDay
            null, // includeVariety
            null, // generateAlternatives
            null, // notes
            false, // emergencyMode
            true, // respectPantry
            25.0, // dailyBudgetLimit
            150.0, // weeklyBudgetLimit
            "USD" // budgetCurrency
            );

    testSlot =
        MealSlot.builder()
            .id(UUID.randomUUID())
            .slotName(MealSlot.SlotName.LUNCH)
            .menuEntries(new ArrayList<>())
            .build();
  }

  @Test
  void testGenerateCandidates_WithProducts_ReturnsCandidates() {
    // Given: some products are available
    List<Product> products =
        Arrays.asList(
            createTestProduct("Potato", 100, 2.5, 80, 0.1),
            createTestProduct("Rice", 150, 3.0, 130, 0.3));

    when(productRepository.findAll()).thenReturn(products);
    when(varietyEngine.getItemsToAvoidForVariety(any(), any(), any(), any()))
        .thenReturn(Collections.emptySet());
    when(scoringEngine.calculateScore(any(), any(), any(), any(), any()))
        .thenReturn(BigDecimal.valueOf(75.0));

    // When
    List<FoodCandidate> candidates =
        enhancedFoodSelectionService.generateCandidates(
            testSlot, testPatient, testNorm, testRequest);

    // Then
    assertThat(candidates).hasSize(2);
    assertThat(candidates.get(0).getItemName()).isIn("Potato", "Rice");
    assertThat(candidates.get(0).getScore()).isEqualTo(BigDecimal.valueOf(75.0));
  }

  @Test
  void testGenerateCandidates_WithVarietyRestrictions_FiltersItems() {
    // Given: some items should be avoided for variety
    List<Product> products =
        Arrays.asList(
            createTestProduct("Potato", 100, 2.5, 80, 0.1),
            createTestProduct("Rice", 150, 3.0, 130, 0.3));

    when(productRepository.findAll()).thenReturn(products);
    when(varietyEngine.getItemsToAvoidForVariety(any(), any(), any(), any()))
        .thenReturn(Set.of("Potato")); // Potato should be avoided
    when(scoringEngine.calculateScore(any(), any(), any(), any(), any()))
        .thenReturn(BigDecimal.valueOf(75.0));

    // When
    List<FoodCandidate> candidates =
        enhancedFoodSelectionService.generateCandidates(
            testSlot, testPatient, testNorm, testRequest);

    // Then
    assertThat(candidates).hasSize(1);
    assertThat(candidates.get(0).getItemName()).isEqualTo("Rice");
  }

  @Test
  void testSelectForCoreMeals_LimitsResults() {
    // Given: more candidates than requested
    List<FoodCandidate> candidates =
        Arrays.asList(
            createTestCandidate("Item1", 90.0),
            createTestCandidate("Item2", 85.0),
            createTestCandidate("Item3", 80.0),
            createTestCandidate("Item4", 75.0),
            createTestCandidate("Item5", 70.0));

    // When
    List<FoodCandidate> selected =
        enhancedFoodSelectionService.selectForCoreMeals(candidates, testSlot, 3);

    // Then
    assertThat(selected).hasSize(3);
    assertThat(selected.get(0).getScore())
        .isEqualTo(BigDecimal.valueOf(90.0)); // Highest score first
    assertThat(selected.get(1).getScore()).isEqualTo(BigDecimal.valueOf(85.0));
    assertThat(selected.get(2).getScore()).isEqualTo(BigDecimal.valueOf(80.0));
  }

  @Test
  void testSelectForCoreMeals_FewerCandidatesThanRequested_ReturnsAll() {
    // Given: fewer candidates than requested
    List<FoodCandidate> candidates =
        Arrays.asList(createTestCandidate("Item1", 90.0), createTestCandidate("Item2", 85.0));

    // When
    List<FoodCandidate> selected =
        enhancedFoodSelectionService.selectForCoreMeals(candidates, testSlot, 5);

    // Then
    assertThat(selected).hasSize(2);
    assertThat(selected.get(0).getScore()).isEqualTo(BigDecimal.valueOf(90.0));
    assertThat(selected.get(1).getScore()).isEqualTo(BigDecimal.valueOf(85.0));
  }

  @Test
  void testCreateMenuEntry_WithValidCandidate_ReturnsEntry() {
    // Given: a valid food candidate
    FoodCandidate candidate = createTestCandidate("Test Item", 85.0);

    // When
    MenuEntry entry = enhancedFoodSelectionService.createMenuEntry(candidate, testSlot);

    // Then
    assertThat(entry).isNotNull();
    assertThat(entry.getMealSlot()).isEqualTo(testSlot);
    // Note: More detailed assertions would depend on the actual implementation
    // of createMenuEntry which may involve complex object creation
  }

  @Test
  void testGenerateCandidates_EmptyProducts_ReturnsEmptyList() {
    // Given: no products available
    when(productRepository.findAll()).thenReturn(Collections.emptyList());
    when(varietyEngine.getItemsToAvoidForVariety(any(), any(), any(), any()))
        .thenReturn(Collections.emptySet());

    // When
    List<FoodCandidate> candidates =
        enhancedFoodSelectionService.generateCandidates(
            testSlot, testPatient, testNorm, testRequest);

    // Then
    assertThat(candidates).isEmpty();
  }

  @Test
  void testGenerateCandidates_AllItemsFiltered_ReturnsEmptyList() {
    // Given: all items should be avoided for variety
    List<Product> products =
        Arrays.asList(
            createTestProduct("Potato", 100, 2.5, 80, 0.1),
            createTestProduct("Rice", 150, 3.0, 130, 0.3));

    when(productRepository.findAll()).thenReturn(products);
    when(varietyEngine.getItemsToAvoidForVariety(any(), any(), any(), any()))
        .thenReturn(Set.of("Potato", "Rice")); // All items should be avoided

    // When
    List<FoodCandidate> candidates =
        enhancedFoodSelectionService.generateCandidates(
            testSlot, testPatient, testNorm, testRequest);

    // Then
    assertThat(candidates).isEmpty();
  }

  @Test
  void testSelectForCoreMeals_EmptyCandidates_ReturnsEmptyList() {
    // Given: no candidates
    List<FoodCandidate> candidates = Collections.emptyList();

    // When
    List<FoodCandidate> selected =
        enhancedFoodSelectionService.selectForCoreMeals(candidates, testSlot, 3);

    // Then
    assertThat(selected).isEmpty();
  }

  // Helper methods to create test objects
  private Product createTestProduct(
      String name, double pheMg, double proteinG, int kcal, double fatG) {
    return Product.builder()
        .id(UUID.randomUUID())
        .productName(name)
        .phenylalanine(BigDecimal.valueOf(pheMg))
        .protein(BigDecimal.valueOf(proteinG))
        .kilocalories(BigDecimal.valueOf(kcal))
        .fats(BigDecimal.valueOf(fatG))
        .build();
  }

  private FoodCandidate createTestCandidate(String name, double score) {
    Product testProduct = Product.builder().id(UUID.randomUUID()).productName(name).build();

    return FoodCandidate.builder()
        .entryType(MenuEntry.EntryType.PRODUCT)
        .product(testProduct)
        .score(BigDecimal.valueOf(score))
        .suggestedServingGrams(BigDecimal.valueOf(100))
        .costPerServing(BigDecimal.valueOf(2.50))
        .availableInPantry(false)
        .build();
  }
}
