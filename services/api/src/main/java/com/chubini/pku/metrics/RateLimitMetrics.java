package com.chubini.pku.metrics;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/** Metrics component for tracking rate limiting events and performance. */
@Component
@RequiredArgsConstructor
public class RateLimitMetrics {
  private final MeterRegistry meterRegistry;

  /**
   * Records a blocked request due to rate limiting.
   *
   * @param tier the rate limit tier (e.g., "strict", "moderate", "standard")
   * @param route the route pattern (e.g., "/api/v1/products")
   */
  public void recordBlocked(String tier, String route) {
    meterRegistry.counter("pku.ratelimit.blocked", "tier", tier, "route", route).increment();
  }

  /**
   * Times a request and records metrics.
   *
   * @param tier the rate limit tier
   * @param route the route pattern
   * @param fn the function to execute and time
   * @return the result of the function
   */
  public <T> T timeRequest(String tier, String route, Supplier<T> fn) {
    return Timer.builder("pku.ratelimit.request.timer")
        .tags("tier", tier, "route", route)
        .publishPercentileHistogram()
        .register(meterRegistry)
        .record(fn);
  }
}
