package com.chubini.pku.validation.events;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Event listener for handling notification events This is a stub implementation that logs events -
 * in production would integrate with push notification services, email services, etc.
 */
@Component
@Slf4j
public class ValidationNotificationEventListener {

  /** Handle limit breach events */
  @EventListener
  @Async
  public void handleLimitBreach(LimitBreachEvent event) {
    log.info(
        "🚨 LIMIT BREACH DETECTED - Day: {}, Type: {}, Severity: {}, Delta: {}, Context: {}",
        event.dayId(),
        event.breachType(),
        event.severity(),
        event.delta(),
        event.contextType());

    // Stub implementations for different notification channels
    sendPushNotification(event);
    sendEmailNotification(event);

    // Log the event details for debugging
    log.debug("Breach details: {}", event.description());
  }

  /** Handle suggestions ready events */
  @EventListener
  @Async
  public void handleSuggestionsReady(SuggestionsReadyEvent event) {
    log.info(
        "💡 SUGGESTIONS READY - Patient: {}, Day: {}, Type: {}, Count: {}",
        event.patientId(),
        event.dayId(),
        event.suggestionType(),
        event.suggestions().size());

    // Stub implementations for different notification channels
    sendSuggestionsNotification(event);

    // Log suggestions for debugging
    event.suggestions().forEach(suggestion -> log.debug("Suggestion: {}", suggestion));
  }

  /** Stub: Send push notification for limit breach */
  private void sendPushNotification(LimitBreachEvent event) {
    log.info(
        "📱 PUSH NOTIFICATION STUB: Would send breach alert for {} with severity {}",
        event.breachType(),
        event.severity());

    // TODO: Integrate with Firebase Cloud Messaging, Apple Push Notification Service, etc.
    // Example payload:
    // {
    //   "title": "Dietary Limit Exceeded",
    //   "body": event.description(),
    //   "data": {
    //     "dayId": event.dayId(),
    //     "breachType": event.breachType(),
    //     "severity": event.severity()
    //   }
    // }
  }

  /** Stub: Send email notification for limit breach */
  private void sendEmailNotification(LimitBreachEvent event) {
    log.info(
        "📧 EMAIL NOTIFICATION STUB: Would send breach email for {} with severity {}",
        event.breachType(),
        event.severity());

    // TODO: Integrate with SendGrid, AWS SES, or similar email service
    // Example:
    // - Subject: "PKU Dietary Alert - " + event.breachType()
    // - Body: Formatted HTML with breach details and recommendations
    // - Priority based on severity level
  }

  /** Stub: Send notification for dietary suggestions */
  private void sendSuggestionsNotification(SuggestionsReadyEvent event) {
    log.info(
        "🔔 SUGGESTIONS NOTIFICATION STUB: Would send {} suggestions for patient {}",
        event.suggestions().size(),
        event.patientId());

    // TODO: Send in-app notification or email with suggestions
    // Could be lower priority than breach notifications
  }

  /** Future enhancement: Send SMS notifications for critical breaches */
  private void sendSmsNotification(LimitBreachEvent event) {
    if (event.severity() == com.chubini.pku.validation.CriticalFact.Severity.CRITICAL) {
      log.info(
          "📱 SMS NOTIFICATION STUB: Would send critical breach SMS for {}", event.breachType());

      // TODO: Integrate with Twilio, AWS SNS, or similar SMS service
      // Only for critical severity breaches to avoid SMS spam
    }
  }
}
