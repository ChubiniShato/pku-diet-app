package com.chubini.pku.validation;

import com.chubini.pku.norms.dto.NormPrescriptionDto;
import com.chubini.pku.validation.dto.ValidationResult;
import com.chubini.pku.validation.dto.NutritionalValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class NormsValidator {

    /**
     * Validate nutritional values against patient's norm prescription
     */
    public ValidationResult validateNutrition(NutritionalValues values, NormPrescriptionDto norm) {
        log.debug("Validating nutrition values against norm: {}", norm.prescriptionId());
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> deltas = new ArrayList<>();
        
        // Validate PHE limits (hard constraint)
        if (norm.dailyPheMgLimit() != null && values.pheMg() != null) {
            if (values.pheMg().compareTo(norm.dailyPheMgLimit()) > 0) {
                BigDecimal excess = values.pheMg().subtract(norm.dailyPheMgLimit());
                errors.add(String.format("PHE exceeds daily limit by %.2f mg (%.2f/%.2f mg)", 
                        excess, values.pheMg(), norm.dailyPheMgLimit()));
                deltas.add(String.format("PHE: +%.2f mg over limit", excess));
            } else {
                BigDecimal remaining = norm.dailyPheMgLimit().subtract(values.pheMg());
                deltas.add(String.format("PHE: %.2f mg remaining (%.2f/%.2f mg)", 
                        remaining, values.pheMg(), norm.dailyPheMgLimit()));
            }
        }
        
        // Validate protein limits (hard constraint)
        if (norm.dailyProteinGLimit() != null && values.proteinG() != null) {
            if (values.proteinG().compareTo(norm.dailyProteinGLimit()) > 0) {
                BigDecimal excess = values.proteinG().subtract(norm.dailyProteinGLimit());
                errors.add(String.format("Protein exceeds daily limit by %.2f g (%.2f/%.2f g)", 
                        excess, values.proteinG(), norm.dailyProteinGLimit()));
                deltas.add(String.format("Protein: +%.2f g over limit", excess));
            } else {
                BigDecimal remaining = norm.dailyProteinGLimit().subtract(values.proteinG());
                deltas.add(String.format("Protein: %.2f g remaining (%.2f/%.2f g)", 
                        remaining, values.proteinG(), norm.dailyProteinGLimit()));
            }
        }
        
        // Validate calorie minimum (hard constraint)
        if (norm.dailyKcalMin() != null && values.kcal() != null) {
            if (values.kcal().compareTo(norm.dailyKcalMin()) < 0) {
                BigDecimal deficit = norm.dailyKcalMin().subtract(values.kcal());
                errors.add(String.format("Calories below minimum requirement by %.0f kcal (%.0f/%.0f kcal)", 
                        deficit, values.kcal(), norm.dailyKcalMin()));
                deltas.add(String.format("Calories: -%.0f kcal below minimum", deficit));
            } else {
                BigDecimal surplus = values.kcal().subtract(norm.dailyKcalMin());
                deltas.add(String.format("Calories: +%.0f kcal above minimum (%.0f/%.0f kcal)", 
                        surplus, values.kcal(), norm.dailyKcalMin()));
            }
        }
        
        boolean isValid = errors.isEmpty();
        
        log.debug("Validation complete. Valid: {}, Errors: {}, Warnings: {}", 
                isValid, errors.size(), warnings.size());
        
        return new ValidationResult(isValid, errors, warnings, deltas);
    }

    /**
     * Calculate percentage of daily limits used
     */
    public ValidationResult calculateDailyProgress(NutritionalValues currentValues, NormPrescriptionDto norm) {
        log.debug("Calculating daily progress against norm: {}", norm.prescriptionId());
        
        List<String> deltas = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // PHE progress
        if (norm.dailyPheMgLimit() != null && currentValues.pheMg() != null) {
            BigDecimal phePercentage = currentValues.pheMg()
                    .divide(norm.dailyPheMgLimit(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            deltas.add(String.format("PHE: %.1f%% of daily limit used (%.2f/%.2f mg)", 
                    phePercentage, currentValues.pheMg(), norm.dailyPheMgLimit()));
            
            if (phePercentage.compareTo(BigDecimal.valueOf(80)) > 0) {
                warnings.add("PHE consumption is approaching daily limit (>80%)");
            }
        }
        
        return new ValidationResult(true, List.of(), warnings, deltas);
    }
}