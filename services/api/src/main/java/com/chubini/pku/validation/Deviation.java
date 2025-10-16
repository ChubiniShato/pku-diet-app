package com.chubini.pku.validation;

import java.math.BigDecimal;

/**
 * Represents a deviation from a target value with direction and magnitude. Provides unified
 * semantics for validation results across the application.
 */
public record Deviation(Direction direction, BigDecimal amount) {

  public enum Direction {
    OVER,
    UNDER,
    EXACT
  }

  /**
   * Creates a Deviation by comparing actual value to target.
   *
   * @param actual the actual value
   * @param target the target value
   * @return Deviation with direction and absolute magnitude
   */
  public static Deviation of(BigDecimal actual, BigDecimal target) {
    BigDecimal diff = actual.subtract(target);
    if (diff.signum() == 0) {
      return new Deviation(Direction.EXACT, BigDecimal.ZERO);
    }
    return new Deviation(diff.signum() > 0 ? Direction.OVER : Direction.UNDER, diff.abs());
  }

  /** Checks if this deviation represents an overage. */
  public boolean isOver() {
    return direction == Direction.OVER;
  }

  /** Checks if this deviation represents an underage. */
  public boolean isUnder() {
    return direction == Direction.UNDER;
  }

  /** Checks if this deviation represents an exact match. */
  public boolean isExact() {
    return direction == Direction.EXACT;
  }
}
