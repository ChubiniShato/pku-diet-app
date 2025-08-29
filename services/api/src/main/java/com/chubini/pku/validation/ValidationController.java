package com.chubini.pku.validation;

import com.chubini.pku.norms.NormService;
import com.chubini.pku.norms.dto.NormPrescriptionDto;
import com.chubini.pku.validation.dto.NutritionalValues;
import com.chubini.pku.validation.dto.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/validation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Nutritional Validation", description = "APIs for validating nutritional values against patient norms")
public class ValidationController {

    private final NormsValidator normsValidator;
    private final NormService normService;

    @Operation(summary = "Validate nutritional values", description = "Validate nutritional values against a patient's active norm prescription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationResult.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found or no active norm prescription"),
            @ApiResponse(responseCode = "400", description = "Invalid nutritional values")
    })
    @PostMapping("/patient/{patientId}")
    public ResponseEntity<ValidationResult> validateNutrition(
            @Parameter(description = "Patient unique identifier", required = true)
            @PathVariable UUID patientId,
            @Parameter(description = "Nutritional values to validate", required = true)
            @Valid @RequestBody NutritionalValues values) {
        
        log.info("Validating nutrition for patient: {}", patientId);
        
        // Get the current active norm for the patient
        Optional<NormPrescriptionDto> currentNorm = normService.getCurrentNormForPatient(patientId);
        
        if (currentNorm.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ValidationResult result = normsValidator.validateNutrition(values, currentNorm.get());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Validate meal nutrition", description = "Validate nutritional values for a single meal against portion of daily norms")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meal validation completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationResult.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found or no active norm prescription"),
            @ApiResponse(responseCode = "400", description = "Invalid nutritional values or meal portion")
    })
    @PostMapping("/patient/{patientId}/meal")
    public ResponseEntity<ValidationResult> validateMeal(
            @Parameter(description = "Patient unique identifier", required = true)
            @PathVariable UUID patientId,
            @Parameter(description = "Meal portion of daily norms (0.0 to 1.0)", required = true, example = "0.25")
            @RequestParam double mealPortion,
            @Parameter(description = "Meal nutritional values to validate", required = true)
            @Valid @RequestBody NutritionalValues mealValues) {
        
        log.info("Validating meal nutrition for patient: {} (portion: {})", patientId, mealPortion);
        
        if (mealPortion <= 0 || mealPortion > 1.0) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get the current active norm for the patient
        Optional<NormPrescriptionDto> currentNorm = normService.getCurrentNormForPatient(patientId);
        
        if (currentNorm.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ValidationResult result = normsValidator.validateNutrition(mealValues, currentNorm.get());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Calculate daily progress", description = "Calculate progress towards daily nutritional limits")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily progress calculated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationResult.class))),
            @ApiResponse(responseCode = "404", description = "Patient not found or no active norm prescription"),
            @ApiResponse(responseCode = "400", description = "Invalid nutritional values")
    })
    @PostMapping("/patient/{patientId}/progress")
    public ResponseEntity<ValidationResult> calculateDailyProgress(
            @Parameter(description = "Patient unique identifier", required = true)
            @PathVariable UUID patientId,
            @Parameter(description = "Current day's nutritional values", required = true)
            @Valid @RequestBody NutritionalValues currentValues) {
        
        log.info("Calculating daily progress for patient: {}", patientId);
        
        // Get the current active norm for the patient
        Optional<NormPrescriptionDto> currentNorm = normService.getCurrentNormForPatient(patientId);
        
        if (currentNorm.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ValidationResult result = normsValidator.calculateDailyProgress(currentValues, currentNorm.get());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Validate against specific norm", description = "Validate nutritional values against a specific norm prescription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationResult.class))),
            @ApiResponse(responseCode = "404", description = "Norm prescription not found"),
            @ApiResponse(responseCode = "400", description = "Invalid nutritional values")
    })
    @PostMapping("/norm/{normId}")
    public ResponseEntity<ValidationResult> validateAgainstNorm(
            @Parameter(description = "Norm prescription unique identifier", required = true)
            @PathVariable UUID normId,
            @Parameter(description = "Nutritional values to validate", required = true)
            @Valid @RequestBody NutritionalValues values) {
        
        log.info("Validating nutrition against norm: {}", normId);
        
        NormPrescriptionDto norm = normService.getNormById(normId);
        ValidationResult result = normsValidator.validateNutrition(values, norm);
        return ResponseEntity.ok(result);
    }
}
