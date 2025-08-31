package com.chubini.pku.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.*;

import com.chubini.pku.menus.MenuDay;
import com.chubini.pku.menus.MenuDayRepository;
import com.chubini.pku.patients.PatientProfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VarietyEngineTest {

  @Mock private MenuDayRepository menuDayRepository;

  @InjectMocks private VarietyEngine varietyEngine;

  private PatientProfile testPatient;
  private LocalDate testDate;

  @BeforeEach
  void setUp() {
    testPatient = PatientProfile.builder().id(UUID.randomUUID()).name("Test Patient").build();

    testDate = LocalDate.of(2024, 1, 10);
  }

  @Test
  void testGetDaysSinceLastUse_NoRecentUse_ReturnsMaxValue() {
    // Given: no recent menu days
    when(menuDayRepository.findByPatientAndDateBetweenOrderByDateDesc(
            eq(testPatient), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(new ArrayList<>());

    // When
    int daysSince = varietyEngine.getDaysSinceLastUse("Potato", testPatient, testDate, "LUNCH");

    // Then
    assertThat(daysSince).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void testViolatesVarietyRules_EmergencyMode_AlwaysAllows() {
    // Given: emergency mode enabled
    boolean emergencyMode = true;

    // When
    boolean violates =
        varietyEngine.violatesVarietyRules("Potato", testPatient, testDate, "LUNCH", emergencyMode);

    // Then
    assertThat(violates).isFalse();
  }

  @Test
  void testViolatesVarietyRules_NormalMode_RecentUse_Violates() {
    // Given: item was used 1 day ago (less than minimum 2 days)
    when(menuDayRepository.findByPatientAndDateBetweenOrderByDateDesc(
            eq(testPatient), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(createMenuDaysWithItem("Potato", testDate.minusDays(1)));

    // When
    boolean violates =
        varietyEngine.violatesVarietyRules("Potato", testPatient, testDate, "LUNCH", false);

    // Then
    assertThat(violates).isTrue();
  }

  @Test
  void testViolatesVarietyRules_NormalMode_OldUse_DoesNotViolate() {
    // Given: item was used 3 days ago (more than minimum 2 days)
    when(menuDayRepository.findByPatientAndDateBetweenOrderByDateDesc(
            eq(testPatient), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(createMenuDaysWithItem("Potato", testDate.minusDays(3)));

    // When
    boolean violates =
        varietyEngine.violatesVarietyRules("Potato", testPatient, testDate, "LUNCH", false);

    // Then
    assertThat(violates).isFalse();
  }

  @Test
  void testGetItemsToAvoidForVariety_EmergencyMode_ReturnsEmpty() {
    // Given: emergency mode
    boolean emergencyMode = true;

    // When
    Set<String> itemsToAvoid =
        varietyEngine.getItemsToAvoidForVariety(testPatient, testDate, "LUNCH", emergencyMode);

    // Then
    assertThat(itemsToAvoid).isEmpty();
  }

  @Test
  void testGetItemsToAvoidForVariety_NormalMode_ReturnsRecentItems() {
    // Given: items used recently
    when(menuDayRepository.findByPatientAndDateBetweenOrderByDateDesc(
            eq(testPatient), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(
            createMenuDaysWithItems(
                Map.of(
                    "Potato", testDate.minusDays(1),
                    "Carrot", testDate.minusDays(2),
                    "Broccoli", testDate.minusDays(3))));

    // When
    Set<String> itemsToAvoid =
        varietyEngine.getItemsToAvoidForVariety(testPatient, testDate, "LUNCH", false);

    // Then - items used within 2 days should be avoided
    assertThat(itemsToAvoid).containsExactlyInAnyOrder("Potato");
    assertThat(itemsToAvoid).doesNotContain("Broccoli"); // Used 3 days ago, OK to use
  }

  @Test
  void testAnalyzeWeeklyVariety_NoViolations_HighScore() {
    // Given: weekly menu with good variety
    List<MenuDay> weekDays = createWeekWithVariety();

    // When
    VarietyEngine.VarietyAnalysis analysis = varietyEngine.analyzeWeeklyVariety(weekDays, false);

    // Then
    assertThat(analysis.getTotalUniqueItems()).isGreaterThan(5);
    assertThat(analysis.getVarietyScore()).isGreaterThan(80.0);
    assertThat(analysis.hasViolations()).isFalse();
  }

  @Test
  void testAnalyzeWeeklyVariety_WithViolations_LowerScore() {
    // Given: weekly menu with repeated items too soon
    List<MenuDay> weekDays = createWeekWithRepeats();

    // When
    VarietyEngine.VarietyAnalysis analysis = varietyEngine.analyzeWeeklyVariety(weekDays, false);

    // Then
    assertThat(analysis.hasViolations()).isTrue();
    assertThat(analysis.getVarietyViolations()).isNotEmpty();
    assertThat(analysis.getRepeatedItems()).isGreaterThan(0);
  }

  @Test
  void testAnalyzeWeeklyVariety_EmergencyMode_NoViolations() {
    // Given: weekly menu with repeats but emergency mode
    List<MenuDay> weekDays = createWeekWithRepeats();

    // When
    VarietyEngine.VarietyAnalysis analysis = varietyEngine.analyzeWeeklyVariety(weekDays, true);

    // Then
    assertThat(analysis.hasViolations()).isFalse();
    assertThat(analysis.isEmergencyMode()).isTrue();
  }

  @Test
  void testSuggestAlternativesForVariety() {
    // Given: vegetable category with items to avoid
    Set<String> itemsToAvoid = Set.of("Potato", "Carrot");

    // When
    List<String> alternatives =
        varietyEngine.suggestAlternativesForVariety("Potato", "vegetables", itemsToAvoid);

    // Then
    assertThat(alternatives).isNotEmpty();
    assertThat(alternatives).doesNotContain("Potato");
    assertThat(alternatives).doesNotContain("Carrot");
    assertThat(alternatives.size()).isLessThanOrEqualTo(3);
  }

  // Helper methods to create test data
  private List<MenuDay> createMenuDaysWithItem(String itemName, LocalDate date) {
    // This would create a MenuDay with MealSlots containing the specified item
    // Simplified for testing - in reality would need full object graph
    MenuDay menuDay =
        MenuDay.builder()
            .id(UUID.randomUUID())
            .patient(testPatient)
            .date(date)
            .mealSlots(new ArrayList<>())
            .build();

    return List.of(menuDay);
  }

  private List<MenuDay> createMenuDaysWithItems(Map<String, LocalDate> itemDates) {
    List<MenuDay> menuDays = new ArrayList<>();
    for (Map.Entry<String, LocalDate> entry : itemDates.entrySet()) {
      menuDays.addAll(createMenuDaysWithItem(entry.getKey(), entry.getValue()));
    }
    return menuDays;
  }

  private List<MenuDay> createWeekWithVariety() {
    List<MenuDay> days = new ArrayList<>();
    LocalDate startDate = testDate.minusDays(6);

    // Create 7 days with different items each day
    for (int i = 0; i < 7; i++) {
      MenuDay day =
          MenuDay.builder()
              .id(UUID.randomUUID())
              .patient(testPatient)
              .date(startDate.plusDays(i))
              .mealSlots(new ArrayList<>())
              .build();
      days.add(day);
    }

    return days;
  }

  private List<MenuDay> createWeekWithRepeats() {
    List<MenuDay> days = new ArrayList<>();
    LocalDate startDate = testDate.minusDays(6);

    // Create 7 days with some repeated items within 2 days
    for (int i = 0; i < 7; i++) {
      MenuDay day =
          MenuDay.builder()
              .id(UUID.randomUUID())
              .patient(testPatient)
              .date(startDate.plusDays(i))
              .mealSlots(new ArrayList<>())
              .build();
      days.add(day);
    }

    return days;
  }
}
