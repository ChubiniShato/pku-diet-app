package com.chubini.pku.metrics;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/** Metrics component for tracking product-related operations and performance. */
@Component
@RequiredArgsConstructor
public class ProductMetrics {
  private final MeterRegistry meterRegistry;

  /**
   * Times a product search operation and records metrics.
   *
   * @param searchType the type of search (e.g., "by_phe", "by_category", "by_name")
   * @param fn the function to execute and time
   * @return the result of the function
   */
  public <T> T timedSearch(String searchType, Supplier<T> fn) {
    Counter.builder("pku.products.search.count")
        .tag("type", searchType)
        .register(meterRegistry)
        .increment();

    return Timer.builder("pku.products.search.timer")
        .tag("type", searchType)
        .publishPercentileHistogram()
        .register(meterRegistry)
        .record(fn);
  }

  /**
   * Records a product view event.
   *
   * @param productId the product ID
   * @param viewType the type of view (e.g., "detail", "list", "search")
   */
  public void recordView(String productId, String viewType) {
    meterRegistry.counter("pku.products.view.count", "view_type", viewType).increment();
  }
}
