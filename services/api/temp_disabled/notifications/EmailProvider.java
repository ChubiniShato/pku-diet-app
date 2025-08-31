package com.chubini.pku.notifications;

/** Email notification provider interface */
public interface EmailProvider extends NotificationProvider {

  @Override
  default NotificationType getType() {
    return NotificationType.EMAIL;
  }

  /** Send plain text email */
  NotificationResult sendTextEmail(String to, String subject, String body);

  /** Send HTML email */
  NotificationResult sendHtmlEmail(String to, String subject, String htmlBody);

  /** Send email with template */
  NotificationResult sendTemplatedEmail(String to, String templateId, Object templateData);

  /** Send secure invitation email (contains only links, no PHI) */
  NotificationResult sendSecureInvitationEmail(
      String to, String doctorName, String patientName, String shareLinkUrl, String expiryInfo);

  /** Email message structure */
  class EmailMessage {
    private final String to;
    private final String subject;
    private final String textBody;
    private final String htmlBody;
    private final String from;
    private final String replyTo;

    public EmailMessage(
        String to, String subject, String textBody, String htmlBody, String from, String replyTo) {
      this.to = to;
      this.subject = subject;
      this.textBody = textBody;
      this.htmlBody = htmlBody;
      this.from = from;
      this.replyTo = replyTo;
    }

    // Getters
    public String getTo() {
      return to;
    }

    public String getSubject() {
      return subject;
    }

    public String getTextBody() {
      return textBody;
    }

    public String getHtmlBody() {
      return htmlBody;
    }

    public String getFrom() {
      return from;
    }

    public String getReplyTo() {
      return replyTo;
    }
  }
}
