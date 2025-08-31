package com.chubini.pku.notifications;

/** Base interface for all notification providers */
public interface NotificationProvider {

  /** Get the type of this provider */
  NotificationType getType();

  /** Check if this provider is configured and available */
  boolean isAvailable();

  /** Send a notification */
  NotificationResult send(NotificationMessage message);

  /** Provider types */
  enum NotificationType {
    PUSH,
    EMAIL,
    SMS
  }

  /** Result of a notification send attempt */
  class NotificationResult {
    private final boolean success;
    private final String messageId;
    private final String errorMessage;

    public NotificationResult(boolean success, String messageId, String errorMessage) {
      this.success = success;
      this.messageId = messageId;
      this.errorMessage = errorMessage;
    }

    public static NotificationResult success(String messageId) {
      return new NotificationResult(true, messageId, null);
    }

    public static NotificationResult failure(String errorMessage) {
      return new NotificationResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
      return success;
    }

    public String getMessageId() {
      return messageId;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }
}
