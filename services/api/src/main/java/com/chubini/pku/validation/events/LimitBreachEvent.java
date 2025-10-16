package com.chubini.pku.validation.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chubini.pku.validation.CriticalFact;

/** Event published when a nutritional limit is breached */
public record LimitBreachEvent(
    UUID dayId,
    String breachType,
    BigDecimal delta,
    CriticalFact.Severity severity,
    String contextType,
    String description,
    LocalDateTime timestamp) {
  public LimitBreachEvent(
      UUID dayId,
      String breachType,
      BigDecimal delta,
      CriticalFact.Severity severity,
      String contextType,
      String description) {
    this(dayId, breachType, delta, severity, contextType, description, LocalDateTime.now());
  }
}
