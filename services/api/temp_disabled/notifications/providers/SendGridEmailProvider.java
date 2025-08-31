package com.chubini.pku.notifications.providers;

import com.chubini.pku.notifications.EmailProvider;
import com.chubini.pku.notifications.NotificationMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/** SendGrid email notification provider */
@Component
@Slf4j
public class SendGridEmailProvider implements EmailProvider {

  @Value("${sendgrid.api.key:#{null}}")
  private String sendGridApiKey;

  @Value("${sendgrid.from.email:#{null}}")
  private String fromEmail;

  @Value("${sendgrid.from.name:PKU Diet App}")
  private String fromName;

  @Value("${notifications.email.enabled:true}")
  private boolean enabled;

  @Override
  public boolean isAvailable() {
    return enabled && sendGridApiKey != null && fromEmail != null;
  }

  @Override
  public NotificationResult send(NotificationMessage message) {
    if (!isAvailable()) {
      log.warn("SendGrid provider not configured, falling back to logging");
      logEmail(message);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation, this would send via SendGrid API
    // For now, we'll just log the email
    logEmail(message);
    return NotificationResult.success("SG-" + System.currentTimeMillis());
  }

  @Override
  public NotificationResult sendTextEmail(String to, String subject, String body) {
    if (!isAvailable()) {
      log.warn("SendGrid provider not configured, falling back to logging");
      logTextEmail(to, subject, body);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation:
    // 1. Build SendGrid mail object
    // 2. Send via SendGrid API
    // 3. Handle response
    logTextEmail(to, subject, body);
    return NotificationResult.success("SG-" + to.hashCode());
  }

  @Override
  public NotificationResult sendHtmlEmail(String to, String subject, String htmlBody) {
    if (!isAvailable()) {
      log.warn("SendGrid provider not configured, falling back to logging");
      logHtmlEmail(to, subject, htmlBody);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation:
    // 1. Build SendGrid mail object with HTML content
    // 2. Send via SendGrid API
    // 3. Handle response
    logHtmlEmail(to, subject, htmlBody);
    return NotificationResult.success("SG-HTML-" + to.hashCode());
  }

  @Override
  public NotificationResult sendTemplatedEmail(String to, String templateId, Object templateData) {
    if (!isAvailable()) {
      log.warn("SendGrid provider not configured, falling back to logging");
      logTemplatedEmail(to, templateId, templateData);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation:
    // 1. Use SendGrid dynamic templates
    // 2. Send via SendGrid API
    // 3. Handle response
    logTemplatedEmail(to, templateId, templateData);
    return NotificationResult.success("SG-TEMPLATE-" + templateId.hashCode());
  }

  @Override
  public NotificationResult sendSecureInvitationEmail(
      String to, String doctorName, String patientName, String shareLinkUrl, String expiryInfo) {

    String subject = "Secure Medical Data Access Invitation";
    String htmlBody = buildSecureInvitationHtml(doctorName, patientName, shareLinkUrl, expiryInfo);
    String textBody = buildSecureInvitationText(doctorName, patientName, shareLinkUrl, expiryInfo);

    EmailMessage message = new EmailMessage(to, subject, textBody, htmlBody, fromEmail, null);

    return send(message);
  }

  private NotificationResult send(EmailMessage message) {
    if (!isAvailable()) {
      log.warn("SendGrid provider not configured, falling back to logging");
      logEmailMessage(message);
      return NotificationResult.success("LOGGED-" + System.currentTimeMillis());
    }

    // In a real implementation, send via SendGrid
    logEmailMessage(message);
    return NotificationResult.success("SG-" + message.getTo().hashCode());
  }

  private String buildSecureInvitationHtml(
      String doctorName, String patientName, String shareLinkUrl, String expiryInfo) {
    return String.format(
        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Medical Data Access Invitation</title>
            </head>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                    <h2 style="color: #333; margin-top: 0;">Secure Medical Data Access Invitation</h2>

                    <p>Dear %s,</p>

                    <p>You have been granted secure access to view medical data for <strong>%s</strong>.</p>

                    <div style="background-color: #fff; padding: 15px; border: 1px solid #dee2e6; border-radius: 4px; margin: 20px 0;">
                        <p><strong>Important Security Notice:</strong></p>
                        <ul>
                            <li>This link provides secure, read-only access to medical data</li>
                            <li>No sensitive information is included in this email</li>
                            <li>The link will expire: <strong>%s</strong></li>
                            <li>Access is logged for security purposes</li>
                        </ul>
                    </div>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; display: inline-block;">Access Medical Data</a>
                    </div>

                    <p style="color: #6c757d; font-size: 14px;">
                        If you did not expect this invitation, please disregard this email.
                    </p>

                    <hr style="border: none; border-top: 1px solid #dee2e6; margin: 20px 0;">
                    <p style="color: #6c757d; font-size: 12px;">
                        This is an automated message from the PKU Diet Management System.
                        For support, please contact your healthcare provider.
                    </p>
                </div>
            </body>
            </html>
            """,
        doctorName, patientName, expiryInfo, shareLinkUrl);
  }

  private String buildSecureInvitationText(
      String doctorName, String patientName, String shareLinkUrl, String expiryInfo) {
    return String.format(
        """
            SECURE MEDICAL DATA ACCESS INVITATION

            Dear %s,

            You have been granted secure access to view medical data for %s.

            IMPORTANT SECURITY NOTICE:
            - This link provides secure, read-only access to medical data
            - No sensitive information is included in this email
            - The link will expire: %s
            - Access is logged for security purposes

            Access the data here: %s

            If you did not expect this invitation, please disregard this email.

            This is an automated message from the PKU Diet Management System.
            For support, please contact your healthcare provider.
            """,
        doctorName, patientName, expiryInfo, shareLinkUrl);
  }

  private void logEmail(NotificationMessage message) {
    log.info("--- SENDGRID EMAIL NOTIFICATION (LOGGED) ---");
    log.info("To: [PROTECTED]");
    log.info("Subject: {}", message.getTitle());
    log.info("Body: {}", message.getBody());
    log.info("Priority: {}", message.getPriority());
    log.info("Data: {}", message.getData());
    log.info("-----------------------------------------");
  }

  private void logTextEmail(String to, String subject, String body) {
    log.info("--- SENDGRID TEXT EMAIL ---");
    log.info("To: {}", maskEmail(to));
    log.info("Subject: {}", subject);
    log.info("Body: {}", body);
    log.info("---------------------------");
  }

  private void logHtmlEmail(String to, String subject, String htmlBody) {
    log.info("--- SENDGRID HTML EMAIL ---");
    log.info("To: {}", maskEmail(to));
    log.info("Subject: {}", subject);
    log.info("HTML Body Length: {} chars", htmlBody.length());
    log.info("---------------------------");
  }

  private void logTemplatedEmail(String to, String templateId, Object templateData) {
    log.info("--- SENDGRID TEMPLATED EMAIL ---");
    log.info("To: {}", maskEmail(to));
    log.info("Template ID: {}", templateId);
    log.info("Template Data: {}", templateData);
    log.info("--------------------------------");
  }

  private void logEmailMessage(EmailMessage message) {
    log.info("--- SENDGRID EMAIL MESSAGE ---");
    log.info("To: {}", maskEmail(message.getTo()));
    log.info("Subject: {}", message.getSubject());
    log.info("From: {}", message.getFrom());
    log.info("Has HTML: {}", message.getHtmlBody() != null);
    log.info(
        "Text Body Length: {} chars",
        message.getTextBody() != null ? message.getTextBody().length() : 0);
    log.info("------------------------------");
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
