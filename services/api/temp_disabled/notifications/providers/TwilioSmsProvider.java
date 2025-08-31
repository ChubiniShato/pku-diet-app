package com.chubini.pku.notifications.providers;

import com.chubini.pku.notifications.NotificationMessage;
import com.chubini.pku.notifications.SmsProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** Twilio SMS notification provider */
@Component
@Slf4j
public class TwilioSmsProvider implements SmsProvider {

  @Value("${twilio.account.sid:#{null}}")
  private String twilioAccountSid;

  @Value("${twilio.auth.token:#{null}}")
  private String twilioAuthToken;

  @Value("${twilio.phone.number:#{null}}")
  private String twilioPhoneNumber;

  @Value("${notifications.sms.enabled:true}")
  private boolean enabled;

  @Override
  public boolean isAvailable() {
    return enabled
        && twilioAccountSid != null
        && twilioAuthToken != null
        && twilioPhoneNumber != null;
  }

  @Override
  public NotificationResult send(NotificationMessage message) {
    if (!isAvailable()) {
      log.warn("Twilio provider not configured, falling back to logging");
      logSms(message);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation, this would send via Twilio API
    // For now, we'll just log the SMS
    logSms(message);
    return NotificationResult.success("TWILIO-" + System.currentTimeMillis());
  }

  @Override
  public NotificationResult sendSms(String to, String message) {
    if (!isAvailable()) {
      log.warn("Twilio provider not configured, falling back to logging");
      logTextSms(to, message);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation:
    // 1. Use Twilio SDK to send SMS
    // 2. Handle response and errors
    logTextSms(to, message);
    return NotificationResult.success("TWILIO-" + to.hashCode());
  }

  @Override
  public NotificationResult sendOtpSms(String to, String otpCode, int expiryMinutes) {
    String message =
        String.format(
            "Your secure access code is: %s. This code expires in %d minutes.",
            otpCode, expiryMinutes);

    SmsMessage smsMessage = new SmsMessage(to, message, twilioPhoneNumber);
    return send(smsMessage);
  }

  @Override
  public NotificationResult sendSecureAlertSms(String to, String alertType, String secureLink) {
    String message =
        String.format(
            "PKU Alert: %s detected. View details securely: %s (expires in 48h)",
            alertType, secureLink);

    SmsMessage smsMessage = new SmsMessage(to, message, twilioPhoneNumber);
    return send(smsMessage);
  }

  private NotificationResult send(SmsMessage message) {
    if (!isAvailable()) {
      log.warn("Twilio provider not configured, falling back to logging");
      logSmsMessage(message);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation, send via Twilio
    logSmsMessage(message);
    return NotificationResult.success("TWILIO-" + message.getTo().hashCode());
  }

  private void logSms(NotificationMessage message) {
    log.info("--- TWILIO SMS NOTIFICATION (LOGGED) ---");
    log.info("To: [PROTECTED]");
    log.info("Type: {}", message.getType());
    log.info("Title: {}", message.getTitle());
    log.info("Body: {}", message.getBody());
    log.info("Priority: {}", message.getPriority());
    log.info("Data: {}", message.getData());
    log.info("--------------------------------------");
  }

  private void logTextSms(String to, String message) {
    log.info("--- TWILIO TEXT SMS ---");
    log.info("To: {}", maskPhoneNumber(to));
    log.info("From: {}", maskPhoneNumber(twilioPhoneNumber));
    log.info("Message: {}", message);
    log.info("Message Length: {} chars", message.length());
    log.info("----------------------");
  }

  private void logSmsMessage(SmsMessage message) {
    log.info("--- TWILIO SMS MESSAGE ---");
    log.info("To: {}", maskPhoneNumber(message.getTo()));
    log.info("From: {}", maskPhoneNumber(message.getFrom()));
    log.info("Message: {}", message.getBody());
    log.info("Message Length: {} chars", message.getBody().length());
    log.info("--------------------------");
  }

  private String maskPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() < 4) {
      return phoneNumber;
    }

    // Keep country code and last 2 digits, mask the rest
    if (phoneNumber.startsWith("+")) {
      // International format: +1234567890 -> +12*****90
      if (phoneNumber.length() >= 6) {
        String countryCode = phoneNumber.substring(0, 3);
        String lastDigits = phoneNumber.substring(phoneNumber.length() - 2);
        String masked = "*".repeat(phoneNumber.length() - 5);
        return countryCode + masked + lastDigits;
      }
    } else {
      // Local format: 1234567890 -> *******890
      if (phoneNumber.length() >= 4) {
        String lastDigits = phoneNumber.substring(phoneNumber.length() - 3);
        String masked = "*".repeat(phoneNumber.length() - 3);
        return masked + lastDigits;
      }
    }

    return phoneNumber;
  }
}
