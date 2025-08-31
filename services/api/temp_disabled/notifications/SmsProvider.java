package com.chubini.pku.notifications;

/** SMS notification provider interface */
public interface SmsProvider extends NotificationProvider {

  @Override
  default NotificationType getType() {
    return NotificationType.SMS;
  }

  /** Send SMS message */
  NotificationResult sendSms(String to, String message);

  /** Send OTP SMS */
  NotificationResult sendOtpSms(String to, String otpCode, int expiryMinutes);

  /** Send secure alert SMS (contains only links, no PHI) */
  NotificationResult sendSecureAlertSms(String to, String alertType, String secureLink);

  /** SMS message structure */
  class SmsMessage {
    private final String to;
    private final String body;
    private final String from; // Sender ID or phone number

    public SmsMessage(String to, String body, String from) {
      this.to = to;
      this.body = body;
      this.from = from;
    }

    // Getters
    public String getTo() {
      return to;
    }

    public String getBody() {
      return body;
    }

    public String getFrom() {
      return from;
    }
  }
}
