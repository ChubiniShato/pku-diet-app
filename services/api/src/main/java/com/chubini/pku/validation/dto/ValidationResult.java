package com.chubini.pku.validation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Result of nutritional validation")
public record ValidationResult(
    @Schema(description = "Whether the validation passed", example = "true")
    boolean isValid,

    @Schema(description = "List of validation errors (hard constraint violations)")
    List<String> errors,

    @Schema(description = "List of validation warnings (soft constraint violations)")
    List<String> warnings,

    @Schema(description = "List of nutritional deltas and progress information")
    List<String> deltas
) {
    
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
    
    public boolean hasDeltaInfo() {
        return deltas != null && !deltas.isEmpty();
    }
    
    public int getTotalIssueCount() {
        int count = 0;
        if (errors != null) count += errors.size();
        if (warnings != null) count += warnings.size();
        return count;
    }
}
