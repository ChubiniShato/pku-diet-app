package com.chubini.pku.validation.events;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Event published when dietary suggestions are ready for a patient */
public record SuggestionsReadyEvent(
    UUID patientId,
    UUID dayId,
    List<String> suggestions,
    String suggestionType,
    LocalDateTime timestamp) {
  public SuggestionsReadyEvent(
      UUID patientId, UUID dayId, List<String> suggestions, String suggestionType) {
    this(patientId, dayId, suggestions, suggestionType, LocalDateTime.now());
  }
}
