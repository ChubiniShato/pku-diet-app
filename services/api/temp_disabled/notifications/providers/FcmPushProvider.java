package com.chubini.pku.notifications.providers;

import com.chubini.pku.notifications.NotificationMessage;
import com.chubini.pku.notifications.PushProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** Firebase Cloud Messaging (FCM) push notification provider */
@Component
@Slf4j
public class FcmPushProvider implements PushProvider {

  @Value("${fcm.server.key:#{null}}")
  private String fcmServerKey;

  @Value("${fcm.project.id:#{null}}")
  private String fcmProjectId;

  @Value("${notifications.fcm.enabled:true}")
  private boolean enabled;

  @Override
  public boolean isAvailable() {
    return enabled && fcmServerKey != null && fcmProjectId != null;
  }

  @Override
  public NotificationResult send(NotificationMessage message) {
    if (!isAvailable()) {
      log.warn("FCM provider not configured, falling back to logging");
      logNotification(message);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation, this would send to FCM
    // For now, we'll just log the notification
    logNotification(message);
    return NotificationResult.success("FCM-" + System.currentTimeMillis());
  }

  @Override
  public NotificationResult sendToDevice(String deviceToken, PushNotificationMessage message) {
    if (!isAvailable()) {
      log.warn("FCM provider not configured, falling back to logging");
      logPushNotification(deviceToken, message);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation:
    // 1. Build FCM message
    // 2. Send to FCM API
    // 3. Handle response
    logPushNotification(deviceToken, message);
    return NotificationResult.success("FCM-" + deviceToken.hashCode());
  }

  @Override
  public NotificationResult sendToTopic(String topic, PushNotificationMessage message) {
    if (!isAvailable()) {
      log.warn("FCM provider not configured, falling back to logging");
      logTopicNotification(topic, message);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation:
    // 1. Build FCM topic message
    // 2. Send to FCM API
    // 3. Handle response
    logTopicNotification(topic, message);
    return NotificationResult.success("FCM-TOPIC-" + topic.hashCode());
  }

  private void logNotification(NotificationMessage message) {
    log.info("--- FCM PUSH NOTIFICATION (LOGGED) ---");
    log.info("Patient: {} ({})", message.getPatientName(), message.getPatientId());
    log.info("Type: {}", message.getType());
    log.info("Title: {}", message.getTitle());
    log.info("Body: {}", message.getBody());
    log.info("Priority: {}", message.getPriority());
    log.info("Data: {}", message.getData());
    log.info("Timestamp: {}", message.getTimestamp());
    log.info("-----------------------------------");
  }

  private void logPushNotification(String deviceToken, PushNotificationMessage message) {
    log.info("--- FCM DEVICE PUSH NOTIFICATION ---");
    log.info("Device Token: {}", maskToken(deviceToken));
    log.info("Title: {}", message.getTitle());
    log.info("Body: {}", message.getBody());
    log.info("Data: {}", message.getData());
    log.info("Icon: {}", message.getIcon());
    log.info("Click Action: {}", message.getClickAction());
    log.info("-----------------------------------");
  }

  private void logTopicNotification(String topic, PushNotificationMessage message) {
    log.info("--- FCM TOPIC PUSH NOTIFICATION ---");
    log.info("Topic: {}", topic);
    log.info("Title: {}", message.getTitle());
    log.info("Body: {}", message.getBody());
    log.info("Data: {}", message.getData());
    log.info("-----------------------------------");
  }

  private String maskToken(String token) {
    if (token == null || token.length() < 10) {
      return token;
    }
    return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
  }
}
