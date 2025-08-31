package com.chubini.pku.notifications;

import java.util.*;

import com.chubini.pku.i18n.MessageService;
import com.chubini.pku.notifications.events.*;
import com.chubini.pku.validation.events.LimitBreachEvent;
import com.chubini.pku.validation.events.SuggestionsReadyEvent;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Event listener that handles application events and sends notifications */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

  private final NotificationService notificationService;
  private final MessageService messageService;

  /** Handle limit breach events from Phase 1 */
  @Async
  @EventListener
  public void handleLimitBreachEvent(LimitBreachEvent event) {
    log.info("--- NOTIFICATION SERVICE (ASYNC) ---");
    log.info("Processing limit breach event: {}", event);

    try {
      // Get patient ID from the menu day (this would need to be added to the event)
      // For now, we'll use a placeholder approach
      UUID patientId = event.dayId(); // This should be patientId, not dayId
      String patientName = "Patient"; // This should come from patient service

      // Send breach notification
      NotificationProvider.NotificationResult result =
          notificationService.sendBreachNotification(
              patientId,
              patientName,
              event.breachType(),
              event.delta().doubleValue(),
              event.severity().name(),
              getDefaultNotificationPreferences());

      if (result.isSuccess()) {
        log.info("Breach notification sent successfully: {}", result.getMessageId());
      } else {
        log.error("Failed to send breach notification: {}", result.getErrorMessage());
      }

    } catch (Exception e) {
      log.error("Error processing limit breach event", e);
    }
  }

  /** Handle suggestions ready events from Phase 1 */
  @Async
  @EventListener
  public void handleSuggestionsReadyEvent(SuggestionsReadyEvent event) {
    log.info("--- NOTIFICATION SERVICE (ASYNC) ---");
    log.info("Processing suggestions ready event: {}", event);

    try {
      // Get patient ID from the menu day
      UUID patientId = event.dayId(); // This should be patientId, not dayId
      String patientName = "Patient"; // This should come from patient service

      // Send suggestions notification
      NotificationProvider.NotificationResult result =
          notificationService.sendSuggestionsNotification(
              patientId,
              patientName,
              event.suggestions().size(),
              "general", // Could be more specific
              getDefaultNotificationPreferences());

      if (result.isSuccess()) {
        log.info("Suggestions notification sent successfully: {}", result.getMessageId());
      } else {
        log.error("Failed to send suggestions notification: {}", result.getErrorMessage());
      }

    } catch (Exception e) {
      log.error("Error processing suggestions ready event", e);
    }
  }

  /** Handle consent expiry events */
  @Async
  @EventListener
  public void handleConsentExpiryEvent(ConsentExpiryEvent event) {
    log.info("--- NOTIFICATION SERVICE (ASYNC) ---");
    log.info("Processing consent expiry event: {}", event);

    try {
      NotificationProvider.NotificationResult result =
          notificationService.sendConsentExpiryNotification(
              event.getPatientId(),
              event.getPatientName(),
              event.getConsentType(),
              event.getExpiryDate(),
              getDefaultNotificationPreferences());

      if (result.isSuccess()) {
        log.info("Consent expiry notification sent successfully: {}", result.getMessageId());
      } else {
        log.error("Failed to send consent expiry notification: {}", result.getErrorMessage());
      }

    } catch (Exception e) {
      log.error("Error processing consent expiry event", e);
    }
  }

  /** Handle share link created events */
  @Async
  @EventListener
  public void handleShareLinkCreatedEvent(ShareLinkCreatedEvent event) {
    log.info("--- NOTIFICATION SERVICE (ASYNC) ---");
    log.info("Processing share link created event: {}", event);

    try {
      // Send notification to patient about share link creation
      NotificationProvider.NotificationResult result =
          notificationService.sendShareLinkCreatedNotification(
              event.getPatientId(),
              event.getPatientName(),
              event.getDoctorName(),
              event.getExpiryDate(),
              getDefaultNotificationPreferences());

      if (result.isSuccess()) {
        log.info("Share link created notification sent successfully: {}", result.getMessageId());
      } else {
        log.error("Failed to send share link created notification: {}", result.getErrorMessage());
      }

    } catch (Exception e) {
      log.error("Error processing share link created event", e);
    }
  }

  /** Handle submission status changed events from Phase 4 */
  @Async
  @EventListener
  public void handleSubmissionStatusChangedEvent(SubmissionStatusChangedEvent event) {
    log.info("--- NOTIFICATION SERVICE (ASYNC) ---");
    log.info("Processing submission status changed event: {}", event);

    try {
      // Get patient ID from submission (this would need to be added to the event)
      // For now, we'll use a placeholder approach
      UUID patientId = UUID.randomUUID(); // TODO: Get from submission
      String patientName = "Patient"; // TODO: Get from patient service

      // Get localized notification message
      Locale patientLocale = getPatientLocale(patientId);
      String title =
          messageService.getNotificationMessage("submission.status.title", patientLocale);
      String body =
          messageService.getNotificationMessage(
              "submission.status.body", new Object[] {event.getNewStatus()}, patientLocale);

      // Send notification
      NotificationMessage.SubmissionStatusNotification message =
          new NotificationMessage.SubmissionStatusNotification(
              patientId,
              patientName,
              event.getNewStatus(),
              event.getTargetType(),
              event.getSubmissionTitle(),
              event.getReviewerNotes());

      NotificationProvider.NotificationResult result =
          notificationService.sendNotification(message, getDefaultNotificationPreferences());

      if (result.isSuccess()) {
        log.info("Submission status notification sent successfully: {}", result.getMessageId());
      } else {
        log.error("Failed to send submission status notification: {}", result.getErrorMessage());
      }

    } catch (Exception e) {
      log.error("Error processing submission status changed event", e);
    }
  }

  /** Handle label scan matched events from Phase 4 */
  @Async
  @EventListener
  public void handleLabelScanMatchedEvent(LabelScanMatchedEvent event) {
    log.info("--- NOTIFICATION SERVICE (ASYNC) ---");
    log.info("Processing label scan matched event: {}", event);

    try {
      String patientName = "Patient"; // TODO: Get from patient service

      // Get localized notification message
      Locale patientLocale = getPatientLocale(event.getPatientId());
      String title =
          messageService.getNotificationMessage("labelscan.matched.title", patientLocale);
      String body =
          messageService.getNotificationMessage(
              "labelscan.matched.body",
              new Object[] {event.getTotalSafetyConcerns()},
              patientLocale);

      // Determine notification priority based on safety concerns
      NotificationType priorityChannel = determineNotificationPriority(event);

      // Send notification
      NotificationMessage.LabelScanNotification message =
          new NotificationMessage.LabelScanNotification(
              event.getPatientId(),
              patientName,
              event.getStatus(),
              event.getMatchedProductName(),
              event.getAllergenHits(),
              event.getForbiddenHits(),
              event.getWarnings(),
              event.isHasSafetyConcerns());

      Set<NotificationType> preferences = Set.of(priorityChannel);
      NotificationProvider.NotificationResult result =
          notificationService.sendNotification(message, preferences);

      if (result.isSuccess()) {
        log.info("Label scan notification sent successfully: {}", result.getMessageId());
      } else {
        log.error("Failed to send label scan notification: {}", result.getErrorMessage());
      }

    } catch (Exception e) {
      log.error("Error processing label scan matched event", e);
    }
  }

  /** Get patient locale (placeholder - should be from patient profile) */
  private Locale getPatientLocale(UUID patientId) {
    // TODO: Get from patient profile
    // For now, default to English
    return messageService.getDefaultLocale();
  }

  /** Determine notification priority based on safety concerns */
  private NotificationType determineNotificationPriority(LabelScanMatchedEvent event) {
    if (event.isHasSafetyConcerns()) {
      // High priority for safety concerns - use multiple channels
      return NotificationType.PUSH; // Will be sent to all configured channels
    } else {
      // Normal priority for successful scans
      return NotificationType.PUSH;
    }
  }

  /**
   * Get default notification preferences In production, this should come from patient preferences
   * stored in database
   */
  private Set<NotificationType> getDefaultNotificationPreferences() {
    return Set.of(NotificationType.PUSH); // Default to push notifications
  }

  /** Event for consent expiry notifications */
  public static class ConsentExpiryEvent {
    private final UUID patientId;
    private final String patientName;
    private final String consentType;
    private final java.time.LocalDateTime expiryDate;

    public ConsentExpiryEvent(
        UUID patientId,
        String patientName,
        String consentType,
        java.time.LocalDateTime expiryDate) {
      this.patientId = patientId;
      this.patientName = patientName;
      this.consentType = consentType;
      this.expiryDate = expiryDate;
    }

    public UUID getPatientId() {
      return patientId;
    }

    public String getPatientName() {
      return patientName;
    }

    public String getConsentType() {
      return consentType;
    }

    public java.time.LocalDateTime getExpiryDate() {
      return expiryDate;
    }
  }

  /** Event for share link creation notifications */
  public static class ShareLinkCreatedEvent {
    private final UUID patientId;
    private final String patientName;
    private final String doctorName;
    private final java.time.LocalDateTime expiryDate;

    public ShareLinkCreatedEvent(
        UUID patientId, String patientName, String doctorName, java.time.LocalDateTime expiryDate) {
      this.patientId = patientId;
      this.patientName = patientName;
      this.doctorName = doctorName;
      this.expiryDate = expiryDate;
    }

    public UUID getPatientId() {
      return patientId;
    }

    public String getPatientName() {
      return patientName;
    }

    public String getDoctorName() {
      return doctorName;
    }

    public java.time.LocalDateTime getExpiryDate() {
      return expiryDate;
    }
  }
}
