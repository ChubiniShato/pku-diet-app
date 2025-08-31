package com.chubini.pku.validation;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.chubini.pku.menus.MealSlot;
import com.chubini.pku.menus.MenuDay;
import com.chubini.pku.menus.MenuEntry;
import com.chubini.pku.validation.dto.NutritionBreakdown;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Service for calculating nutritional totals for menu entries and days */
@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionCalculator {

  private final NutritionScaler nutritionScaler;

  private static final int SCALE = 2;

  /** Calculate nutrition breakdown for a single menu entry */
  public NutritionBreakdown calculateEntryNutrition(
      MenuEntry entry, BigDecimal quantity, String unit) {
    if (entry == null) {
      return NutritionBreakdown.zero();
    }

    log.debug(
        "Calculating nutrition for entry: {} with quantity: {} {}", entry.getId(), quantity, unit);

    return switch (entry.getEntryType()) {
      case PRODUCT -> nutritionScaler.from(entry.getProduct(), quantity, unit);
      case CUSTOM_PRODUCT -> nutritionScaler.from(entry.getCustomProduct(), quantity, unit);
      case DISH -> nutritionScaler.from(entry.getDish(), quantity, unit);
      case CUSTOM_DISH -> nutritionScaler.from(entry.getCustomDish(), quantity, unit);
    };
  }

  /** Calculate planned nutrition totals for a menu day */
  public DayTotals calculatePlannedTotals(MenuDay menuDay) {
    if (menuDay == null || menuDay.getMealSlots() == null) {
      return DayTotals.zero();
    }

    log.debug("Calculating planned totals for menu day: {}", menuDay.getId());

    BigDecimal totalPheMg = BigDecimal.ZERO;
    BigDecimal totalProteinG = BigDecimal.ZERO;
    Integer totalKcal = 0;
    BigDecimal totalFatG = BigDecimal.ZERO;

    for (MealSlot mealSlot : menuDay.getMealSlots()) {
      if (mealSlot.getMenuEntries() != null) {
        for (MenuEntry entry : mealSlot.getMenuEntries()) {
          if (entry.getPlannedServingGrams() != null) {
            NutritionBreakdown nutrition =
                calculateEntryNutrition(entry, entry.getPlannedServingGrams(), "G");

            totalPheMg = totalPheMg.add(nutrition.pheMg());
            totalProteinG = totalProteinG.add(nutrition.proteinG());
            totalKcal += nutrition.kcal();
            totalFatG = totalFatG.add(nutrition.fatG());
          }
        }
      }
    }

    return new DayTotals(
        totalPheMg.setScale(SCALE, RoundingMode.HALF_UP),
        totalProteinG.setScale(SCALE, RoundingMode.HALF_UP),
        totalKcal,
        totalFatG.setScale(SCALE, RoundingMode.HALF_UP));
  }

  /** Calculate consumed nutrition totals for a menu day */
  public DayTotals calculateConsumedTotals(MenuDay menuDay) {
    if (menuDay == null || menuDay.getMealSlots() == null) {
      return DayTotals.zero();
    }

    log.debug("Calculating consumed totals for menu day: {}", menuDay.getId());

    BigDecimal totalPheMg = BigDecimal.ZERO;
    BigDecimal totalProteinG = BigDecimal.ZERO;
    Integer totalKcal = 0;
    BigDecimal totalFatG = BigDecimal.ZERO;

    for (MealSlot mealSlot : menuDay.getMealSlots()) {
      if (mealSlot.getMenuEntries() != null) {
        for (MenuEntry entry : mealSlot.getMenuEntries()) {
          if (entry.getIsConsumed() && entry.getEffectiveConsumedQuantity() != null) {
            NutritionBreakdown nutrition =
                calculateEntryNutrition(entry, entry.getEffectiveConsumedQuantity(), "G");

            totalPheMg = totalPheMg.add(nutrition.pheMg());
            totalProteinG = totalProteinG.add(nutrition.proteinG());
            totalKcal += nutrition.kcal();
            totalFatG = totalFatG.add(nutrition.fatG());
          }
        }
      }
    }

    return new DayTotals(
        totalPheMg.setScale(SCALE, RoundingMode.HALF_UP),
        totalProteinG.setScale(SCALE, RoundingMode.HALF_UP),
        totalKcal,
        totalFatG.setScale(SCALE, RoundingMode.HALF_UP));
  }

  /** Update calculated nutrition values for a menu entry */
  public void updateEntryCalculatedValues(MenuEntry entry) {
    if (entry == null || entry.getPlannedServingGrams() == null) {
      return;
    }

    log.debug("Updating calculated values for entry: {}", entry.getId());

    NutritionBreakdown nutrition =
        calculateEntryNutrition(entry, entry.getPlannedServingGrams(), "G");

    entry.setCalculatedPheMg(nutrition.pheMg());
    entry.setCalculatedProteinG(nutrition.proteinG());
    entry.setCalculatedKcal(nutrition.kcal() != null ? BigDecimal.valueOf(nutrition.kcal()) : null);
    entry.setCalculatedFatG(nutrition.fatG());
  }

  /** Update totals for a menu day */
  public void updateDayTotals(MenuDay menuDay) {
    if (menuDay == null) {
      return;
    }

    log.debug("Updating day totals for menu day: {}", menuDay.getId());

    DayTotals planned = calculatePlannedTotals(menuDay);

    menuDay.setTotalDayPheMg(planned.pheMg());
    menuDay.setTotalDayProteinG(planned.proteinG());
    menuDay.setTotalDayKcal(planned.kcal() != null ? BigDecimal.valueOf(planned.kcal()) : null);
    menuDay.setTotalDayFatG(planned.fatG());
  }

  /** Data class for day totals */
  public record DayTotals(BigDecimal pheMg, BigDecimal proteinG, Integer kcal, BigDecimal fatG) {
    public static DayTotals zero() {
      return new DayTotals(BigDecimal.ZERO, BigDecimal.ZERO, 0, BigDecimal.ZERO);
    }
  }
}
