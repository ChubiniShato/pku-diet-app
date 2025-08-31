package com.chubini.pku.validation.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of nutritional validation")
public record ValidationResult(
    @Schema(description = "Validation level", example = "OK") ValidationLevel level,
    @Schema(description = "Nutritional deltas showing differences from limits")
        Map<String, BigDecimal> deltas,
    @Schema(description = "List of validation messages") List<String> messages,
    @Schema(description = "List of suggestions for improvement") List<String> suggestions) {

  public enum ValidationLevel {
    OK,
    WARN,
    BREACH
  }

  public boolean hasBreaches() {
    return level == ValidationLevel.BREACH;
  }

  public boolean hasWarnings() {
    return level == ValidationLevel.WARN;
  }

  public boolean isOk() {
    return level == ValidationLevel.OK;
  }

  public boolean hasMessages() {
    return messages != null && !messages.isEmpty();
  }

  public boolean hasSuggestions() {
    return suggestions != null && !suggestions.isEmpty();
  }

  public int getTotalIssueCount() {
    int count = 0;
    if (messages != null) count += messages.size();
    return count;
  }

  /** Create a simple OK result */
  public static ValidationResult ok() {
    return new ValidationResult(ValidationLevel.OK, Map.of(), List.of(), List.of());
  }

  /** Create a warning result */
  public static ValidationResult warning(
      Map<String, BigDecimal> deltas, List<String> messages, List<String> suggestions) {
    return new ValidationResult(ValidationLevel.WARN, deltas, messages, suggestions);
  }

  /** Create a breach result */
  public static ValidationResult breach(
      Map<String, BigDecimal> deltas, List<String> messages, List<String> suggestions) {
    return new ValidationResult(ValidationLevel.BREACH, deltas, messages, suggestions);
  }
}
