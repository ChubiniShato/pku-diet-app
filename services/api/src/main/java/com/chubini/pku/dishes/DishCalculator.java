package com.chubini.pku.dishes;

import com.chubini.pku.dishes.dto.DishResponseDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class DishCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /**
     * Calculate total nutritional values for a dish based on its items
     */
    public DishNutritionalTotals calculateTotals(List<DishItem> items) {
        DishNutritionalTotals totals = new DishNutritionalTotals();
        BigDecimal totalGrams = BigDecimal.ZERO;

        for (DishItem item : items) {
            BigDecimal itemGrams = item.getGrams();
            totalGrams = totalGrams.add(itemGrams);

            // Calculate contribution for each nutritional attribute
            totals.totalPhenylalanine = totals.totalPhenylalanine.add(
                calculateContribution(itemGrams, item.getSnapshotPhenylalanine()));
            
            totals.totalLeucine = totals.totalLeucine.add(
                calculateContribution(itemGrams, item.getSnapshotLeucine()));
            
            totals.totalTyrosine = totals.totalTyrosine.add(
                calculateContribution(itemGrams, item.getSnapshotTyrosine()));
            
            totals.totalMethionine = totals.totalMethionine.add(
                calculateContribution(itemGrams, item.getSnapshotMethionine()));
            
            totals.totalKilojoules = totals.totalKilojoules.add(
                calculateContribution(itemGrams, item.getSnapshotKilojoulesAsBigDecimal()));
            
            totals.totalKilocalories = totals.totalKilocalories.add(
                calculateContribution(itemGrams, item.getSnapshotKilocaloriesAsBigDecimal()));
            
            totals.totalProtein = totals.totalProtein.add(
                calculateContribution(itemGrams, item.getSnapshotProtein()));
            
            totals.totalCarbohydrates = totals.totalCarbohydrates.add(
                calculateContribution(itemGrams, item.getSnapshotCarbohydrates()));
            
            totals.totalFats = totals.totalFats.add(
                calculateContribution(itemGrams, item.getSnapshotFats()));
        }

        totals.totalGrams = totalGrams;
        return totals;
    }

    /**
     * Calculate per 100g values based on totals and nominal serving grams
     */
    public DishNutritionalTotals calculatePer100Values(DishNutritionalTotals totals, BigDecimal nominalServingGrams) {
        if (nominalServingGrams.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Nominal serving grams cannot be zero");
        }

        DishNutritionalTotals per100Values = new DishNutritionalTotals();
        
        per100Values.totalPhenylalanine = calculatePer100(totals.totalPhenylalanine, nominalServingGrams);
        per100Values.totalLeucine = calculatePer100(totals.totalLeucine, nominalServingGrams);
        per100Values.totalTyrosine = calculatePer100(totals.totalTyrosine, nominalServingGrams);
        per100Values.totalMethionine = calculatePer100(totals.totalMethionine, nominalServingGrams);
        per100Values.totalKilojoules = calculatePer100(totals.totalKilojoules, nominalServingGrams);
        per100Values.totalKilocalories = calculatePer100(totals.totalKilocalories, nominalServingGrams);
        per100Values.totalProtein = calculatePer100(totals.totalProtein, nominalServingGrams);
        per100Values.totalCarbohydrates = calculatePer100(totals.totalCarbohydrates, nominalServingGrams);
        per100Values.totalFats = calculatePer100(totals.totalFats, nominalServingGrams);

        return per100Values;
    }

    /**
     * Scale nutritional values proportionally
     */
    public DishResponseDto.NutritionalValues scaleValues(DishNutritionalTotals baseValues, BigDecimal scaleFactor) {
        DishResponseDto.NutritionalValues scaled = new DishResponseDto.NutritionalValues();
        
        scaled.setPhenylalanine(scaleValue(baseValues.totalPhenylalanine, scaleFactor));
        scaled.setLeucine(scaleValue(baseValues.totalLeucine, scaleFactor));
        scaled.setTyrosine(scaleValue(baseValues.totalTyrosine, scaleFactor));
        scaled.setMethionine(scaleValue(baseValues.totalMethionine, scaleFactor));
        scaled.setKilojoules(scaleValue(baseValues.totalKilojoules, scaleFactor));
        scaled.setKilocalories(scaleValue(baseValues.totalKilocalories, scaleFactor));
        scaled.setProtein(scaleValue(baseValues.totalProtein, scaleFactor));
        scaled.setCarbohydrates(scaleValue(baseValues.totalCarbohydrates, scaleFactor));
        scaled.setFats(scaleValue(baseValues.totalFats, scaleFactor));

        return scaled;
    }

    /**
     * Calculate required grams to achieve target phenylalanine
     */
    public BigDecimal calculateRequiredGramsForPhenylalanine(BigDecimal targetPhenylalanine, 
                                                           BigDecimal per100Phenylalanine,
                                                           BigDecimal nominalServingGrams) {
        if (per100Phenylalanine.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Cannot calculate required grams: dish has zero phenylalanine per 100g");
        }

        // targetPhenylalanine = (requiredGrams * per100Phenylalanine) / 100
        // requiredGrams = (targetPhenylalanine * 100) / per100Phenylalanine
        return targetPhenylalanine
            .multiply(HUNDRED)
            .divide(per100Phenylalanine, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate contribution of an item to dish totals
     * Formula: contribution = (grams * per100Value) / 100
     */
    private BigDecimal calculateContribution(BigDecimal grams, BigDecimal per100Value) {
        if (per100Value == null) {
            per100Value = BigDecimal.ZERO;
        }
        return grams.multiply(per100Value)
            .divide(HUNDRED, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate per 100g value from total
     * Formula: per100 = (total * 100) / nominalServingGrams
     */
    private BigDecimal calculatePer100(BigDecimal totalValue, BigDecimal nominalServingGrams) {
        if (totalValue == null) {
            totalValue = BigDecimal.ZERO;
        }
        return totalValue.multiply(HUNDRED)
            .divide(nominalServingGrams, SCALE, ROUNDING_MODE);
    }

    /**
     * Scale a value by a factor
     */
    private BigDecimal scaleValue(BigDecimal value, BigDecimal scaleFactor) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        return value.multiply(scaleFactor).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Convert DishNutritionalTotals to NutritionalValues DTO
     */
    public DishResponseDto.NutritionalValues toNutritionalValuesDto(DishNutritionalTotals totals) {
        DishResponseDto.NutritionalValues dto = new DishResponseDto.NutritionalValues();
        dto.setPhenylalanine(totals.totalPhenylalanine);
        dto.setLeucine(totals.totalLeucine);
        dto.setTyrosine(totals.totalTyrosine);
        dto.setMethionine(totals.totalMethionine);
        dto.setKilojoules(totals.totalKilojoules);
        dto.setKilocalories(totals.totalKilocalories);
        dto.setProtein(totals.totalProtein);
        dto.setCarbohydrates(totals.totalCarbohydrates);
        dto.setFats(totals.totalFats);
        return dto;
    }

    /**
     * Calculate individual item contribution for display
     */
    public DishResponseDto.NutritionalValues calculateItemContribution(DishItem item) {
        BigDecimal grams = item.getGrams();
        DishResponseDto.NutritionalValues contribution = new DishResponseDto.NutritionalValues();

        contribution.setPhenylalanine(calculateContribution(grams, item.getSnapshotPhenylalanine()));
        contribution.setLeucine(calculateContribution(grams, item.getSnapshotLeucine()));
        contribution.setTyrosine(calculateContribution(grams, item.getSnapshotTyrosine()));
        contribution.setMethionine(calculateContribution(grams, item.getSnapshotMethionine()));
        contribution.setKilojoules(calculateContribution(grams, item.getSnapshotKilojoulesAsBigDecimal()));
        contribution.setKilocalories(calculateContribution(grams, item.getSnapshotKilocaloriesAsBigDecimal()));
        contribution.setProtein(calculateContribution(grams, item.getSnapshotProtein()));
        contribution.setCarbohydrates(calculateContribution(grams, item.getSnapshotCarbohydrates()));
        contribution.setFats(calculateContribution(grams, item.getSnapshotFats()));

        return contribution;
    }

    /**
     * Inner class to hold nutritional totals during calculations
     */
    public static class DishNutritionalTotals {
        public BigDecimal totalGrams = BigDecimal.ZERO;
        public BigDecimal totalPhenylalanine = BigDecimal.ZERO;
        public BigDecimal totalLeucine = BigDecimal.ZERO;
        public BigDecimal totalTyrosine = BigDecimal.ZERO;
        public BigDecimal totalMethionine = BigDecimal.ZERO;
        public BigDecimal totalKilojoules = BigDecimal.ZERO;
        public BigDecimal totalKilocalories = BigDecimal.ZERO;
        public BigDecimal totalProtein = BigDecimal.ZERO;
        public BigDecimal totalCarbohydrates = BigDecimal.ZERO;
        public BigDecimal totalFats = BigDecimal.ZERO;
    }
}
