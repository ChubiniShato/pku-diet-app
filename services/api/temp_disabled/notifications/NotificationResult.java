package com.chubini.pku.notifications;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/** Result of a notification delivery attempt */
@Data
@Builder
public class NotificationResult {

  private final boolean success;
  private final String provider;
  private final String messageId;
  private final String errorMessage;
  private final LocalDateTime sentAt;
  private final Integer retryCount;
  private final String recipient;

  /** Create successful result */
  public static NotificationResult success(String provider, String messageId, String recipient) {
    return NotificationResult.builder()
        .success(true)
        .provider(provider)
        .messageId(messageId)
        .recipient(recipient)
        .sentAt(LocalDateTime.now())
        .retryCount(0)
        .build();
  }

  /** Create failed result */
  public static NotificationResult failure(
      String provider, String errorMessage, String recipient, Integer retryCount) {
    return NotificationResult.builder()
        .success(false)
        .provider(provider)
        .errorMessage(errorMessage)
        .recipient(recipient)
        .sentAt(LocalDateTime.now())
        .retryCount(retryCount != null ? retryCount : 0)
        .build();
  }

  /** Check if result indicates success */
  public boolean isSuccessful() {
    return success;
  }

  /** Check if result indicates failure */
  public boolean isFailed() {
    return !success;
  }

  /** Get human-readable status */
  public String getStatus() {
    return success ? "SENT" : "FAILED";
  }
}
