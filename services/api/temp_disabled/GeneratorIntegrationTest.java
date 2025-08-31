package com.chubini.pku.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.chubini.pku.BaseIntegrationTest;
import com.chubini.pku.generator.dto.MenuGenerationRequest;
import com.chubini.pku.generator.dto.MenuGenerationResult;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

/**
 * Integration test for generator daily/weekly: generate with variety/pantry/budget and assert
 * acceptance conditions from Phase 2
 */
class GeneratorIntegrationTest extends BaseIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void testWeeklyMenuGeneration() {
    // Given: Patient with norms and preferences
    UUID patientId = UUID.randomUUID();

    MenuGenerationRequest request =
        new MenuGenerationRequest(
            patientId,
            LocalDate.now(),
            "WEEKLY", // generationType
            List.of("breakfast", "lunch", "dinner"), // preferredCategories
            List.of(), // foodsToAvoid
            50.0, // maxPhePerMeal
            2000.0, // targetCaloriesPerDay
            true, // includeVariety
            false, // generateAlternatives
            "Integration test menu", // notes
            false, // emergencyMode
            true, // respectPantry
            25.0, // dailyBudgetLimit
            150.0, // weeklyBudgetLimit
            "EUR" // budgetCurrency
            );

    // When: Generate weekly menu
    ResponseEntity<MenuGenerationResult> response =
        restTemplate.postForEntity("/api/v1/generator/weekly", request, MenuGenerationResult.class);

    // Then: Verify successful generation
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    MenuGenerationResult result = response.getBody();
    assertThat(result).isNotNull();
    assertThat(result.success()).isTrue();
    assertThat(result.generatedMenu()).isNotNull();
    assertThat(result.generationStats()).isNotNull();

    // Verify acceptance conditions
    assertThat(result.generationStats().acceptanceRate()).isGreaterThan(0.7);
    assertThat(result.generationStats().totalItems()).isGreaterThan(0);
  }

  @Test
  void testDailyMenuGeneration() {
    // Given: Patient with norms
    UUID patientId = UUID.randomUUID();

    MenuGenerationRequest request =
        new MenuGenerationRequest(
            patientId,
            LocalDate.now(),
            "DAILY", // generationType
            List.of("breakfast", "lunch", "dinner"), // preferredCategories
            List.of(), // foodsToAvoid
            50.0, // maxPhePerMeal
            2000.0, // targetCaloriesPerDay
            true, // includeVariety
            false, // generateAlternatives
            "Integration test daily menu", // notes
            false, // emergencyMode
            true, // respectPantry
            25.0, // dailyBudgetLimit
            25.0, // weeklyBudgetLimit (same as daily for single day)
            "EUR" // budgetCurrency
            );

    // When: Generate daily menu
    ResponseEntity<MenuGenerationResult> response =
        restTemplate.postForEntity("/api/v1/generator/daily", request, MenuGenerationResult.class);

    // Then: Verify successful generation
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    MenuGenerationResult result = response.getBody();
    assertThat(result).isNotNull();
    assertThat(result.success()).isTrue();
  }
}
