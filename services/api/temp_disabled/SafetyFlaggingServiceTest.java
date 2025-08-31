package com.chubini.pku.labelscan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import com.chubini.pku.patients.PatientProfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SafetyFlaggingServiceTest {

  private SafetyFlaggingService safetyFlaggingService;
  private PatientProfile testPatient;

  @BeforeEach
  void setUp() {
    safetyFlaggingService = new SafetyFlaggingService();

    // Create test patient with allergens
    testPatient =
        PatientProfile.builder()
            .id(UUID.randomUUID())
            .name("Test Patient")
            .allergens(Arrays.asList("nuts", "dairy"))
            .build();
  }

  @Test
  void testAnalyzeText_NoAllergens_Safe() {
    // Given
    String safeText = "INGREDIENTS: Water, Sugar, Artificial Flavors";

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeText(safeText, testPatient);

    // Then
    assertThat(analysis.getOverallLevel()).isEqualTo(SafetyFlaggingService.SafetyLevel.SAFE);
    assertThat(analysis.getAllergenHits()).isEmpty();
    assertThat(analysis.getForbiddenHits()).isEmpty();
    assertThat(analysis.hasSafetyConcerns()).isFalse();
  }

  @Test
  void testAnalyzeText_AllergenDetected_Danger() {
    // Given
    String textWithNuts = "INGREDIENTS: Water, Almonds, Sugar, Artificial Flavors";

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeText(textWithNuts, testPatient);

    // Then
    assertThat(analysis.getOverallLevel()).isEqualTo(SafetyFlaggingService.SafetyLevel.DANGER);
    assertThat(analysis.getAllergenHits()).contains("nuts");
    assertThat(analysis.hasSafetyConcerns()).isTrue();
  }

  @Test
  void testAnalyzeText_ForbiddenIngredientDetected_Warning() {
    // Given
    String textWithArtificialSweeteners =
        "INGREDIENTS: Water, Aspartame, Sugar, Artificial Flavors";

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeText(textWithArtificialSweeteners, testPatient);

    // Then
    assertThat(analysis.getOverallLevel()).isEqualTo(SafetyFlaggingService.SafetyLevel.WARNING);
    assertThat(analysis.getForbiddenHits()).contains("artificial_sweeteners");
    assertThat(analysis.hasSafetyConcerns()).isTrue();
  }

  @Test
  void testAnalyzeText_MultipleAllergens_Critical() {
    // Given
    PatientProfile patientWithMultipleAllergies =
        PatientProfile.builder()
            .id(UUID.randomUUID())
            .name("Test Patient")
            .allergens(Arrays.asList("nuts", "dairy", "eggs"))
            .build();

    String textWithMultipleAllergens = "INGREDIENTS: Water, Almonds, Milk, Eggs, Sugar";

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeText(textWithMultipleAllergens, patientWithMultipleAllergies);

    // Then
    assertThat(analysis.getOverallLevel()).isEqualTo(SafetyFlaggingService.SafetyLevel.CRITICAL);
    assertThat(analysis.getAllergenHits()).hasSizeGreaterThan(1);
    assertThat(analysis.hasSafetyConcerns()).isTrue();
  }

  @Test
  void testAnalyzeText_UnclearLabeling_AddsWarnings() {
    // Given
    String unclearText = "INGREDIENTS: Various flavors, Natural and artificial colors, Spices";

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeText(unclearText, testPatient);

    // Then
    assertThat(analysis.getWarnings()).isNotEmpty();
    assertThat(analysis.getWarnings()).anyMatch(w -> w.contains("unclear"));
  }

  @Test
  void testAnalyzeProductInfo_ProductWithAllergen_Danger() {
    // Given
    BarcodeLookupService.ProductInfo productWithAllergen =
        new BarcodeLookupService.ProductInfo(
            "Test Product",
            "Test Brand",
            "snacks",
            "INGREDIENTS: Water, Peanuts, Sugar",
            "Contains peanuts",
            Map.of("calories", 100.0),
            "http://example.com/image.jpg",
            "TestSource",
            0.9);

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeProductInfo(productWithAllergen, testPatient);

    // Then
    assertThat(analysis.getOverallLevel()).isEqualTo(SafetyFlaggingService.SafetyLevel.DANGER);
    assertThat(analysis.getAllergenHits()).contains("nuts");
    assertThat(analysis.hasSafetyConcerns()).isTrue();
  }

  @Test
  void testAnalyzeProductInfo_ProductWithHighSugar_Warning() {
    // Given
    BarcodeLookupService.ProductInfo productWithHighSugar =
        new BarcodeLookupService.ProductInfo(
            "Test Product",
            "Test Brand",
            "snacks",
            "INGREDIENTS: Water, Sugar",
            "",
            Map.of("sugars", 25.0), // High sugar content
            null,
            "TestSource",
            0.8);

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeProductInfo(productWithHighSugar, testPatient);

    // Then
    assertThat(analysis.getOverallLevel()).isEqualTo(SafetyFlaggingService.SafetyLevel.WARNING);
    assertThat(analysis.getWarnings()).anyMatch(w -> w.contains("sugar"));
  }

  @Test
  void testGetSupportedAllergens() {
    // When
    Set<String> supportedAllergens = safetyFlaggingService.getSupportedAllergens();

    // Then
    assertThat(supportedAllergens).isNotEmpty();
    assertThat(supportedAllergens).contains("nuts", "dairy", "eggs");
  }

  @Test
  void testGetSupportedForbiddenPatterns() {
    // When
    Set<String> supportedPatterns = safetyFlaggingService.getSupportedForbiddenPatterns();

    // Then
    assertThat(supportedPatterns).isNotEmpty();
    assertThat(supportedPatterns).contains("aspartame", "artificial_sweeteners");
  }

  @Test
  void testAnalyzeText_EmptyText_Safe() {
    // Given
    String emptyText = "";

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeText(emptyText, testPatient);

    // Then
    assertThat(analysis.getOverallLevel()).isEqualTo(SafetyFlaggingService.SafetyLevel.SAFE);
    assertThat(analysis.hasSafetyConcerns()).isFalse();
  }

  @Test
  void testAnalyzeText_NullText_Safe() {
    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeText(null, testPatient);

    // Then
    assertThat(analysis.getOverallLevel()).isEqualTo(SafetyFlaggingService.SafetyLevel.SAFE);
    assertThat(analysis.hasSafetyConcerns()).isFalse();
  }

  @Test
  void testAnalyzeProductInfo_NullProduct_Safe() {
    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeProductInfo(null, testPatient);

    // Then
    assertThat(analysis.getOverallLevel()).isEqualTo(SafetyFlaggingService.SafetyLevel.SAFE);
    assertThat(analysis.hasSafetyConcerns()).isFalse();
  }

  @Test
  void testAnalyzeText_ContainsMayContain_Warning() {
    // Given
    String textWithMayContain = "INGREDIENTS: Water, Sugar. May contain nuts.";

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeText(textWithMayContain, testPatient);

    // Then
    assertThat(analysis.getWarnings()).anyMatch(w -> w.contains("may contain"));
  }

  @Test
  void testAnalyzeText_HighUppercase_Warning() {
    // Given
    String highUppercaseText = "INGREDIENTS: WATER, SUGAR, ARTIFICIAL FLAVORS";

    // When
    SafetyFlaggingService.SafetyAnalysis analysis =
        safetyFlaggingService.analyzeText(highUppercaseText, testPatient);

    // Then
    assertThat(analysis.getWarnings()).anyMatch(w -> w.contains("uppercase"));
  }
}
