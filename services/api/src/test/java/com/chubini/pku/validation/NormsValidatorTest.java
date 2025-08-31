package com.chubini.pku.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import com.chubini.pku.menus.MenuDay;
import com.chubini.pku.norms.NormPrescription;
import com.chubini.pku.patients.PatientProfile;
import com.chubini.pku.validation.dto.ValidationResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NormsValidatorTest {

  @Mock private NutritionCalculator nutritionCalculator;

  @InjectMocks private NormsValidator normsValidator;

  private NormPrescription testNorm;
  private MenuDay testMenuDay;

  @BeforeEach
  void setUp() {
    // Create test patient
    PatientProfile patient =
        PatientProfile.builder().id(UUID.randomUUID()).name("Test Patient").build();

    // Create test norm prescription
    testNorm =
        NormPrescription.builder()
            .id(UUID.randomUUID())
            .patient(patient)
            .pheLimitMgPerDay(new BigDecimal("300.00"))
            .proteinLimitGPerDay(new BigDecimal("15.00"))
            .kcalMinPerDay(new BigDecimal("1800.00"))
            .fatLimitGPerDay(new BigDecimal("60.00"))
            .build();

    // Create test menu day
    testMenuDay =
        MenuDay.builder()
            .id(UUID.randomUUID())
            .patient(patient)
            .date(LocalDate.now())
            .mealSlots(new ArrayList<>())
            .build();
  }

  @Test
  void testValidate_AllWithinLimits_ReturnsOK() {
    // Given: totals within all limits
    NutritionCalculator.DayTotals plannedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("250.00"), // PHE within limit
            new BigDecimal("12.00"), // Protein within limit
            1900, // Kcal above minimum
            new BigDecimal("50.00") // Fat within limit
            );
    NutritionCalculator.DayTotals consumedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("200.00"), new BigDecimal("10.00"), 1850, new BigDecimal("45.00"));

    when(nutritionCalculator.calculatePlannedTotals(any(MenuDay.class))).thenReturn(plannedTotals);
    when(nutritionCalculator.calculateConsumedTotals(any(MenuDay.class)))
        .thenReturn(consumedTotals);

    // When
    ValidationResult result = normsValidator.validate(testNorm, testMenuDay);

    // Then
    assertThat(result.level()).isEqualTo(ValidationResult.ValidationLevel.OK);
    assertThat(result.messages()).isEmpty();
  }

  @Test
  void testValidate_PheExceeded_ReturnsBreach() {
    // Given: PHE exceeds limit
    NutritionCalculator.DayTotals plannedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("350.00"), // PHE exceeds limit of 300
            new BigDecimal("12.00"),
            1900,
            new BigDecimal("50.00"));
    NutritionCalculator.DayTotals consumedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("200.00"), new BigDecimal("10.00"), 1850, new BigDecimal("45.00"));

    when(nutritionCalculator.calculatePlannedTotals(any(MenuDay.class))).thenReturn(plannedTotals);
    when(nutritionCalculator.calculateConsumedTotals(any(MenuDay.class)))
        .thenReturn(consumedTotals);

    // When
    ValidationResult result = normsValidator.validate(testNorm, testMenuDay);

    // Then
    assertThat(result.level()).isEqualTo(ValidationResult.ValidationLevel.BREACH);
    assertThat(result.deltas()).containsKey("phe");
    assertThat(result.deltas().get("phe")).isEqualTo(new BigDecimal("50.00")); // 350 - 300
    assertThat(result.messages()).anyMatch(msg -> msg.contains("PHE") && msg.contains("exceeds"));
  }

  @Test
  void testValidate_ProteinExceeded_ReturnsBreach() {
    // Given: Protein exceeds limit
    NutritionCalculator.DayTotals plannedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("250.00"),
            new BigDecimal("18.00"), // Protein exceeds limit of 15
            1900,
            new BigDecimal("50.00"));
    NutritionCalculator.DayTotals consumedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("200.00"), new BigDecimal("10.00"), 1850, new BigDecimal("45.00"));

    when(nutritionCalculator.calculatePlannedTotals(any(MenuDay.class))).thenReturn(plannedTotals);
    when(nutritionCalculator.calculateConsumedTotals(any(MenuDay.class)))
        .thenReturn(consumedTotals);

    // When
    ValidationResult result = normsValidator.validate(testNorm, testMenuDay);

    // Then
    assertThat(result.level()).isEqualTo(ValidationResult.ValidationLevel.BREACH);
    assertThat(result.deltas()).containsKey("protein");
    assertThat(result.deltas().get("protein")).isEqualTo(new BigDecimal("3.00")); // 18 - 15
    assertThat(result.messages())
        .anyMatch(msg -> msg.contains("protein") && msg.contains("exceeds"));
  }

  @Test
  void testValidate_KcalDeficit_ReturnsBreach() {
    // Given: Calories below minimum
    NutritionCalculator.DayTotals plannedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("250.00"),
            new BigDecimal("12.00"),
            1600, // Below minimum of 1800
            new BigDecimal("50.00"));
    NutritionCalculator.DayTotals consumedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("200.00"), new BigDecimal("10.00"), 1500, new BigDecimal("45.00"));

    when(nutritionCalculator.calculatePlannedTotals(any(MenuDay.class))).thenReturn(plannedTotals);
    when(nutritionCalculator.calculateConsumedTotals(any(MenuDay.class)))
        .thenReturn(consumedTotals);

    // When
    ValidationResult result = normsValidator.validate(testNorm, testMenuDay);

    // Then
    assertThat(result.level()).isEqualTo(ValidationResult.ValidationLevel.BREACH);
    assertThat(result.deltas()).containsKey("kcal");
    assertThat(result.deltas().get("kcal")).isEqualTo(new BigDecimal("-200.00")); // 1600 - 1800
    assertThat(result.messages()).anyMatch(msg -> msg.contains("Calorie") && msg.contains("below"));
  }

  @Test
  void testValidate_FatExceeded_ReturnsWarning() {
    // Given: Fat exceeds limit (treated as warning)
    NutritionCalculator.DayTotals plannedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("250.00"),
            new BigDecimal("12.00"),
            1900,
            new BigDecimal("70.00") // Exceeds limit of 60
            );
    NutritionCalculator.DayTotals consumedTotals =
        new NutritionCalculator.DayTotals(
            new BigDecimal("200.00"), new BigDecimal("10.00"), 1850, new BigDecimal("45.00"));

    when(nutritionCalculator.calculatePlannedTotals(any(MenuDay.class))).thenReturn(plannedTotals);
    when(nutritionCalculator.calculateConsumedTotals(any(MenuDay.class)))
        .thenReturn(consumedTotals);

    // When
    ValidationResult result = normsValidator.validate(testNorm, testMenuDay);

    // Then
    assertThat(result.level()).isEqualTo(ValidationResult.ValidationLevel.WARN);
    assertThat(result.deltas()).containsKey("fat");
    assertThat(result.deltas().get("fat")).isEqualTo(new BigDecimal("10.00")); // 70 - 60
    assertThat(result.messages()).anyMatch(msg -> msg.contains("Fat") && msg.contains("exceeds"));
  }

  @Test
  void testValidate_NullInputs_ReturnsOK() {
    // When: null inputs
    ValidationResult result1 = normsValidator.validate(null, testMenuDay);
    ValidationResult result2 = normsValidator.validate(testNorm, null);

    // Then: should return OK without error
    assertThat(result1.level()).isEqualTo(ValidationResult.ValidationLevel.OK);
    assertThat(result2.level()).isEqualTo(ValidationResult.ValidationLevel.OK);
  }
}
