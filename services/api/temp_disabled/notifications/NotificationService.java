package com.chubini.pku.notifications;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.chubini.pku.notifications.providers.FcmPushProvider;
import com.chubini.pku.notifications.providers.SendGridEmailProvider;
import com.chubini.pku.notifications.providers.TwilioSmsProvider;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Main notification service that coordinates all notification providers */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final FcmPushProvider pushProvider;
  private final SendGridEmailProvider emailProvider;
  private final TwilioSmsProvider smsProvider;

  // Default notification preferences (can be made configurable per patient)
  private static final Set<NotificationType> DEFAULT_PATIENT_PREFERENCES =
      Set.of(NotificationType.PUSH);

  /** Send notification using all available providers based on patient preferences */
  public NotificationResult sendNotification(
      NotificationMessage message, Set<NotificationType> preferences) {
    if (preferences == null || preferences.isEmpty()) {
      preferences = DEFAULT_PATIENT_PREFERENCES;
    }

    log.info(
        "Sending notification to patient {} via channels: {}", message.getPatientId(), preferences);

    List<CompletableFuture<NotificationResult>> futures = new ArrayList<>();

    // Send via configured providers
    if (preferences.contains(NotificationType.PUSH) && pushProvider.isAvailable()) {
      futures.add(
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return pushProvider.send(message);
                } catch (Exception e) {
                  log.error("Failed to send push notification", e);
                  return NotificationProvider.NotificationResult.failure(e.getMessage());
                }
              }));
    }

    if (preferences.contains(NotificationType.EMAIL) && emailProvider.isAvailable()) {
      futures.add(
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return emailProvider.send(message);
                } catch (Exception e) {
                  log.error("Failed to send email notification", e);
                  return NotificationProvider.NotificationResult.failure(e.getMessage());
                }
              }));
    }

    if (preferences.contains(NotificationType.SMS) && smsProvider.isAvailable()) {
      futures.add(
          CompletableFuture.supplyAsync(
              () -> {
                try {
                  return smsProvider.send(message);
                } catch (Exception e) {
                  log.error("Failed to send SMS notification", e);
                  return NotificationProvider.NotificationResult.failure(e.getMessage());
                }
              }));
    }

    // Wait for all notifications to complete
    if (futures.isEmpty()) {
      log.warn("No notification providers available for channels: {}", preferences);
      return NotificationProvider.NotificationResult.failure("No providers available");
    }

    // For now, return success if at least one provider succeeds
    // In production, you might want more sophisticated aggregation
    try {
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

      // Check results - if any succeeded, consider it successful
      boolean anySuccess =
          futures.stream().map(CompletableFuture::join).anyMatch(NotificationResult::isSuccess);

      if (anySuccess) {
        return NotificationProvider.NotificationResult.success(
            "MULTI-" + System.currentTimeMillis());
      } else {
        return NotificationProvider.NotificationResult.failure("All providers failed");
      }

    } catch (Exception e) {
      log.error("Error sending notifications", e);
      return NotificationProvider.NotificationResult.failure(e.getMessage());
    }
  }

  /** Send breach notification */
  public NotificationResult sendBreachNotification(
      UUID patientId,
      String patientName,
      String breachType,
      double deltaValue,
      String severity,
      Set<NotificationType> preferences) {

    NotificationMessage.BreachNotification message =
        new NotificationMessage.BreachNotification(
            patientId, patientName, breachType, deltaValue, severity);

    log.info(
        "Sending breach notification: {} breach of {} with severity {}",
        breachType,
        deltaValue,
        severity);

    return sendNotification(message, preferences);
  }

  /** Send suggestions notification */
  public NotificationResult sendSuggestionsNotification(
      UUID patientId,
      String patientName,
      int suggestionCount,
      String suggestionType,
      Set<NotificationType> preferences) {

    NotificationMessage.SuggestionsNotification message =
        new NotificationMessage.SuggestionsNotification(
            patientId, patientName, suggestionCount, suggestionType);

    log.info(
        "Sending suggestions notification: {} {} suggestions available",
        suggestionCount,
        suggestionType);

    return sendNotification(message, preferences);
  }

  /** Send consent expiry notification */
  public NotificationResult sendConsentExpiryNotification(
      UUID patientId,
      String patientName,
      String consentType,
      java.time.LocalDateTime expiryDate,
      Set<NotificationType> preferences) {

    NotificationMessage.ConsentExpiryNotification message =
        new NotificationMessage.ConsentExpiryNotification(
            patientId, patientName, consentType, expiryDate);

    log.info("Sending consent expiry notification: {} expires on {}", consentType, expiryDate);

    return sendNotification(message, preferences);
  }

  /** Send share link created notification */
  public NotificationResult sendShareLinkCreatedNotification(
      UUID patientId,
      String patientName,
      String doctorName,
      java.time.LocalDateTime expiryDate,
      Set<NotificationType> preferences) {

    NotificationMessage.ShareLinkCreatedNotification message =
        new NotificationMessage.ShareLinkCreatedNotification(
            patientId, patientName, doctorName, expiryDate);

    log.info(
        "Sending share link created notification: shared with {} expires on {}",
        doctorName,
        expiryDate);

    return sendNotification(message, preferences);
  }

  /** Send secure invitation email to doctor */
  public NotificationResult sendDoctorInvitationEmail(
      String doctorEmail,
      String doctorName,
      String patientName,
      String shareLinkUrl,
      String expiryInfo) {

    log.info("Sending secure invitation email to doctor: {}", maskEmail(doctorEmail));

    if (!emailProvider.isAvailable()) {
      log.warn("Email provider not available, logging invitation instead");
      logInvitation(doctorEmail, doctorName, patientName, shareLinkUrl, expiryInfo);
      return NotificationProvider.NotificationResult.success(
          "LOGGED-" + System.currentTimeMillis());
    }

    try {
      return emailProvider.sendSecureInvitationEmail(
          doctorEmail, doctorName, patientName, shareLinkUrl, expiryInfo);
    } catch (Exception e) {
      log.error("Failed to send doctor invitation email", e);
      return NotificationProvider.NotificationResult.failure(e.getMessage());
    }
  }

  /** Get available notification providers */
  public Map<NotificationType, Boolean> getProviderStatus() {
    return Map.of(
        NotificationType.PUSH, pushProvider.isAvailable(),
        NotificationType.EMAIL, emailProvider.isAvailable(),
        NotificationType.SMS, smsProvider.isAvailable());
  }

  /** Send test notification to verify provider configuration */
  public Map<NotificationType, NotificationResult> sendTestNotifications(
      UUID patientId, String patientName) {
    Map<NotificationType, NotificationResult> results = new HashMap<>();

    NotificationMessage testMessage =
        new NotificationMessage.BreachNotification(patientId, patientName, "TEST", 0.0, "LOW");

    // Test each provider individually
    if (pushProvider.isAvailable()) {
      results.put(NotificationType.PUSH, pushProvider.send(testMessage));
    }

    if (emailProvider.isAvailable()) {
      results.put(NotificationType.EMAIL, emailProvider.send(testMessage));
    }

    if (smsProvider.isAvailable()) {
      results.put(NotificationType.SMS, smsProvider.send(testMessage));
    }

    log.info("Test notification results: {}", results);
    return results;
  }

  private void logInvitation(
      String doctorEmail,
      String doctorName,
      String patientName,
      String shareLinkUrl,
      String expiryInfo) {
    log.info("--- SECURE DOCTOR INVITATION (LOGGED) ---");
    log.info("Doctor Email: {}", maskEmail(doctorEmail));
    log.info("Doctor Name: {}", doctorName);
    log.info("Patient Name: {}", patientName);
    log.info("Share Link: {}", shareLinkUrl);
    log.info("Expires: {}", expiryInfo);
    log.info("---------------------------------------");
  }

  private String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
      return email;
    }
    String[] parts = email.split("@");
    if (parts[0].length() <= 2) {
      return email;
    }
    String username = parts[0].substring(0, 2) + "***";
    return username + "@" + parts[1];
  }
}
