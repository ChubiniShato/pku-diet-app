package com.chubini.pku.generator;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.chubini.pku.menus.*;
import com.chubini.pku.patients.PatientProfile;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Engine for managing variety rules and detecting repeats in menu generation */
@Component
@RequiredArgsConstructor
@Slf4j
public class VarietyEngine {

  private final MenuDayRepository menuDayRepository;

  // Minimum days between repeats (configurable)
  private static final int MIN_DAYS_BETWEEN_REPEATS = 2;

  /** Check if a food item was used recently (within MIN_DAYS_BETWEEN_REPEATS) */
  public int getDaysSinceLastUse(
      String itemName, PatientProfile patient, LocalDate targetDate, String mealType) {
    if (itemName == null || patient == null || targetDate == null) {
      return Integer.MAX_VALUE; // No recent use
    }

    // Look back up to 7 days
    LocalDate startDate = targetDate.minusDays(7);
    List<MenuDay> recentDays =
        menuDayRepository.findByPatientAndDateBetweenOrderByDateDesc(
            patient, startDate, targetDate.minusDays(1));

    for (MenuDay day : recentDays) {
      int daysAgo = (int) (targetDate.toEpochDay() - day.getDate().toEpochDay());

      if (wasItemUsedInDay(itemName, day, mealType)) {
        log.debug("Found recent use of {} in {} {} days ago", itemName, mealType, daysAgo);
        return daysAgo;
      }
    }

    return Integer.MAX_VALUE; // No recent use found
  }

  /** Check if item was used in a specific day and meal type */
  private boolean wasItemUsedInDay(String itemName, MenuDay day, String mealType) {
    if (day.getMealSlots() == null) {
      return false;
    }

    for (MealSlot slot : day.getMealSlots()) {
      // If mealType specified, only check that meal type
      if (mealType != null && !slot.getSlotName().name().equalsIgnoreCase(mealType)) {
        continue;
      }

      if (slot.getMenuEntries() != null) {
        for (MenuEntry entry : slot.getMenuEntries()) {
          if (itemName.equalsIgnoreCase(entry.getItemName())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  /** Check if using this item would violate variety rules */
  public boolean violatesVarietyRules(
      String itemName,
      PatientProfile patient,
      LocalDate targetDate,
      String mealType,
      boolean emergencyMode) {
    if (emergencyMode) {
      log.debug("Emergency mode enabled - allowing repeats for {}", itemName);
      return false; // Emergency mode allows repeats
    }

    int daysSinceLastUse = getDaysSinceLastUse(itemName, patient, targetDate, mealType);
    boolean violates = daysSinceLastUse < MIN_DAYS_BETWEEN_REPEATS;

    if (violates) {
      log.debug(
          "Variety violation: {} was used {} days ago (minimum: {})",
          itemName,
          daysSinceLastUse,
          MIN_DAYS_BETWEEN_REPEATS);
    }

    return violates;
  }

  /** Get all items used in recent days for variety analysis */
  public Map<String, Integer> getRecentItemUsage(
      PatientProfile patient, LocalDate targetDate, int daysBack) {
    Map<String, Integer> itemUsage = new HashMap<>();

    LocalDate startDate = targetDate.minusDays(daysBack);
    List<MenuDay> recentDays =
        menuDayRepository.findByPatientAndDateBetweenOrderByDateDesc(
            patient, startDate, targetDate.minusDays(1));

    for (MenuDay day : recentDays) {
      int daysAgo = (int) (targetDate.toEpochDay() - day.getDate().toEpochDay());

      if (day.getMealSlots() != null) {
        for (MealSlot slot : day.getMealSlots()) {
          if (slot.getMenuEntries() != null) {
            for (MenuEntry entry : slot.getMenuEntries()) {
              String itemName = entry.getItemName();
              if (itemName != null) {
                // Keep track of most recent use
                itemUsage.merge(itemName, daysAgo, Integer::min);
              }
            }
          }
        }
      }
    }

    log.debug("Found {} unique items used in last {} days", itemUsage.size(), daysBack);
    return itemUsage;
  }

  /** Get items that should be avoided due to variety rules */
  public Set<String> getItemsToAvoidForVariety(
      PatientProfile patient, LocalDate targetDate, String mealType, boolean emergencyMode) {
    if (emergencyMode) {
      return Collections.emptySet(); // No restrictions in emergency mode
    }

    Map<String, Integer> recentUsage =
        getRecentItemUsage(patient, targetDate, MIN_DAYS_BETWEEN_REPEATS);

    Set<String> itemsToAvoid =
        recentUsage.entrySet().stream()
            .filter(entry -> entry.getValue() < MIN_DAYS_BETWEEN_REPEATS)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

    log.debug("Avoiding {} items due to variety rules: {}", itemsToAvoid.size(), itemsToAvoid);
    return itemsToAvoid;
  }

  /** Analyze variety across a weekly menu */
  public VarietyAnalysis analyzeWeeklyVariety(List<MenuDay> weekDays, boolean emergencyMode) {
    Map<String, Integer> itemFrequency = new HashMap<>();
    Map<String, List<LocalDate>> itemDates = new HashMap<>();
    List<String> varietyViolations = new ArrayList<>();

    // Count item frequencies and track dates
    for (MenuDay day : weekDays) {
      if (day.getMealSlots() != null) {
        for (MealSlot slot : day.getMealSlots()) {
          if (slot.getMenuEntries() != null) {
            for (MenuEntry entry : slot.getMenuEntries()) {
              String itemName = entry.getItemName();
              if (itemName != null) {
                itemFrequency.merge(itemName, 1, Integer::sum);
                itemDates.computeIfAbsent(itemName, k -> new ArrayList<>()).add(day.getDate());
              }
            }
          }
        }
      }
    }

    // Check for variety violations
    if (!emergencyMode) {
      for (Map.Entry<String, List<LocalDate>> entry : itemDates.entrySet()) {
        String itemName = entry.getKey();
        List<LocalDate> dates = entry.getValue();

        // Sort dates to check intervals
        dates.sort(LocalDate::compareTo);

        for (int i = 1; i < dates.size(); i++) {
          LocalDate prevDate = dates.get(i - 1);
          LocalDate currentDate = dates.get(i);
          long daysBetween = currentDate.toEpochDay() - prevDate.toEpochDay();

          if (daysBetween < MIN_DAYS_BETWEEN_REPEATS) {
            varietyViolations.add(
                String.format(
                    "%s repeated after %d days (%s to %s)",
                    itemName, daysBetween, prevDate, currentDate));
          }
        }
      }
    }

    int totalItems = itemFrequency.size();
    int repeatedItems = (int) itemFrequency.values().stream().filter(count -> count > 1).count();
    double varietyScore =
        totalItems > 0 ? (double) (totalItems - repeatedItems) / totalItems * 100 : 100;

    return VarietyAnalysis.builder()
        .totalUniqueItems(totalItems)
        .repeatedItems(repeatedItems)
        .varietyScore(varietyScore)
        .itemFrequencies(itemFrequency)
        .varietyViolations(varietyViolations)
        .emergencyMode(emergencyMode)
        .build();
  }

  /** Suggest alternatives to avoid variety violations */
  public List<String> suggestAlternativesForVariety(
      String originalItem, String category, Set<String> itemsToAvoid) {
    // This would ideally query a database of similar items
    // For now, return a simple suggestion based on category
    List<String> alternatives = new ArrayList<>();

    if (category != null) {
      // Add some category-based alternatives (this would be enhanced with actual DB queries)
      switch (category.toLowerCase()) {
        case "vegetables" ->
            alternatives.addAll(List.of("Broccoli", "Spinach", "Carrots", "Bell Peppers"));
        case "fruits" -> alternatives.addAll(List.of("Apples", "Bananas", "Berries", "Oranges"));
        case "grains" -> alternatives.addAll(List.of("Rice", "Quinoa", "Pasta", "Bread"));
        case "protein" -> alternatives.addAll(List.of("Chicken", "Fish", "Tofu", "Eggs"));
        default -> alternatives.add("Similar item in " + category);
      }
    }

    // Filter out items that should also be avoided
    alternatives.removeAll(itemsToAvoid);
    alternatives.remove(originalItem);

    return alternatives.stream().limit(3).collect(Collectors.toList());
  }

  /** Data class for variety analysis results */
  public static class VarietyAnalysis {
    private final int totalUniqueItems;
    private final int repeatedItems;
    private final double varietyScore;
    private final Map<String, Integer> itemFrequencies;
    private final List<String> varietyViolations;
    private final boolean emergencyMode;

    @lombok.Builder
    public VarietyAnalysis(
        int totalUniqueItems,
        int repeatedItems,
        double varietyScore,
        Map<String, Integer> itemFrequencies,
        List<String> varietyViolations,
        boolean emergencyMode) {
      this.totalUniqueItems = totalUniqueItems;
      this.repeatedItems = repeatedItems;
      this.varietyScore = varietyScore;
      this.itemFrequencies = itemFrequencies;
      this.varietyViolations = varietyViolations;
      this.emergencyMode = emergencyMode;
    }

    // Getters
    public int getTotalUniqueItems() {
      return totalUniqueItems;
    }

    public int getRepeatedItems() {
      return repeatedItems;
    }

    public double getVarietyScore() {
      return varietyScore;
    }

    public Map<String, Integer> getItemFrequencies() {
      return itemFrequencies;
    }

    public List<String> getVarietyViolations() {
      return varietyViolations;
    }

    public boolean isEmergencyMode() {
      return emergencyMode;
    }

    public boolean hasViolations() {
      return !emergencyMode && varietyViolations != null && !varietyViolations.isEmpty();
    }
  }
}
