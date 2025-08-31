package com.chubini.pku.notifications;

/** Push notification provider interface (e.g., FCM, APNs) */
public interface PushProvider extends NotificationProvider {

  @Override
  default NotificationType getType() {
    return NotificationType.PUSH;
  }

  /** Send push notification with device token */
  NotificationResult sendToDevice(String deviceToken, PushNotificationMessage message);

  /** Send push notification to topic/channel */
  NotificationResult sendToTopic(String topic, PushNotificationMessage message);

  /** Push notification message structure */
  class PushNotificationMessage {
    private final String title;
    private final String body;
    private final String data; // JSON payload
    private final String icon;
    private final String clickAction;

    public PushNotificationMessage(
        String title, String body, String data, String icon, String clickAction) {
      this.title = title;
      this.body = body;
      this.data = data;
      this.icon = icon;
      this.clickAction = clickAction;
    }

    // Getters
    public String getTitle() {
      return title;
    }

    public String getBody() {
      return body;
    }

    public String getData() {
      return data;
    }

    public String getIcon() {
      return icon;
    }

    public String getClickAction() {
      return clickAction;
    }
  }
}
