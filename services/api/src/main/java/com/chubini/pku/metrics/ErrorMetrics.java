package com.chubini.pku.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

/**
 * Metrics component for tracking application errors with cardinality control. Uses template
 * patterns to avoid high cardinality issues.
 */
@Component
@RequiredArgsConstructor
public class ErrorMetrics {
  private final MeterRegistry meterRegistry;

  /**
   * Records an error with endpoint pattern and error type.
   *
   * @param endpointPattern the endpoint pattern (e.g., "/products/{id}")
   * @param errorType the error type (e.g., "validation_error", "database_error")
   */
  public void record(String endpointPattern, String errorType) {
    meterRegistry
        .counter("pku.errors.count", "endpoint", endpointPattern, "type", errorType)
        .increment();
  }
}
