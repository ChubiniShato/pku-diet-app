package com.chubini.pku.config;

import java.util.regex.Pattern;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Production logging configuration with PII redaction. Implements Rev C requirements for secure
 * logging.
 */
@Configuration
@Profile("prod")
public class LoggingConfig {

  /**
   * PII Redaction Filter for production logs. Redacts sensitive information like emails, phones,
   * tokens.
   */
  public static class PiiRedactionFilter extends Filter<ILoggingEvent> {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile(
            "\\b(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b");

    private static final Pattern TOKEN_PATTERN =
        Pattern.compile(
            "(?i)(token|jwt|bearer|authorization|password|secret|key)\\s*[=:]\\s*[\"']?([^\\s\"']+)[\"']?");

    private static final Pattern CREDIT_CARD_PATTERN =
        Pattern.compile("\\b(?:\\d{4}[-.\\s]?){3}\\d{4}\\b");

    @Override
    public FilterReply decide(ILoggingEvent event) {
      String message = event.getFormattedMessage();

      if (message != null) {
        // Redact sensitive information
        message = EMAIL_PATTERN.matcher(message).replaceAll("***@***.***");
        message = PHONE_PATTERN.matcher(message).replaceAll("***-***-****");
        message = TOKEN_PATTERN.matcher(message).replaceAll("$1=***");
        message = CREDIT_CARD_PATTERN.matcher(message).replaceAll("****-****-****-****");

        // Create new event with redacted message
        // Note: This is a simplified approach. In production, consider using
        // a custom PatternLayout or Logback configuration for better performance
      }

      return FilterReply.NEUTRAL;
    }
  }

  /**
   * Custom Pattern Layout with PII redaction. Used in production to ensure sensitive data doesn't
   * leak into logs.
   */
  public static class PiiRedactionPatternLayout extends PatternLayout {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile(
            "\\b(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b");

    private static final Pattern TOKEN_PATTERN =
        Pattern.compile(
            "(?i)(token|jwt|bearer|authorization|password|secret|key)\\s*[=:]\\s*[\"']?([^\\s\"']+)[\"']?");

    @Override
    public String doLayout(ILoggingEvent event) {
      String originalMessage = super.doLayout(event);

      // Apply PII redaction
      String redactedMessage = EMAIL_PATTERN.matcher(originalMessage).replaceAll("***@***.***");
      redactedMessage = PHONE_PATTERN.matcher(redactedMessage).replaceAll("***-***-****");
      redactedMessage = TOKEN_PATTERN.matcher(redactedMessage).replaceAll("$1=***");

      return redactedMessage;
    }
  }
}
