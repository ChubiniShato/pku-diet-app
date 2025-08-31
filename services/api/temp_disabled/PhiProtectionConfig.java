package com.chubini.pku.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * PHI (Protected Health Information) protection configuration Ensures sensitive patient data is
 * properly masked in logs and responses
 */
@Configuration
@Slf4j
public class PhiProtectionConfig {

  @Value("${app.security.log-sensitive-data:false}")
  private boolean logSensitiveData;

  /** PHI data masker utility */
  @Bean
  public PhiMasker phiMasker() {
    return new PhiMasker(logSensitiveData);
  }

  /** Utility class for masking PHI data */
  public static class PhiMasker {

    private final boolean allowSensitiveLogging;
    private static final String MASK = "***MASKED***";

    public PhiMasker(boolean allowSensitiveLogging) {
      this.allowSensitiveLogging = allowSensitiveLogging;
    }

    /** Mask email addresses */
    public String maskEmail(String email) {
      if (email == null || email.isEmpty()) return email;
      if (allowSensitiveLogging) return email;

      int atIndex = email.indexOf('@');
      if (atIndex <= 1) return MASK;

      return email.charAt(0) + "***@" + email.substring(atIndex + 1);
    }

    /** Mask phone numbers */
    public String maskPhone(String phone) {
      if (phone == null || phone.isEmpty()) return phone;
      if (allowSensitiveLogging) return phone;

      // Keep country code and last 2 digits
      if (phone.length() <= 4) return MASK;
      return phone.substring(0, phone.length() - 4) + "****";
    }

    /** Mask names (keep first letter) */
    public String maskName(String name) {
      if (name == null || name.isEmpty()) return name;
      if (allowSensitiveLogging) return name;

      if (name.length() <= 1) return "*";
      return name.charAt(0) + "***";
    }

    /** Mask date of birth (keep year) */
    public String maskDob(String dob) {
      if (dob == null || dob.isEmpty()) return dob;
      if (allowSensitiveLogging) return dob;

      // Assume format YYYY-MM-DD, keep only year
      if (dob.length() >= 4) {
        return dob.substring(0, 4) + "-**-**";
      }
      return MASK;
    }

    /** Mask patient ID (keep first and last characters) */
    public String maskPatientId(String patientId) {
      if (patientId == null || patientId.isEmpty()) return patientId;
      if (allowSensitiveLogging) return patientId;

      if (patientId.length() <= 4) return MASK;
      return patientId.charAt(0) + "***" + patientId.charAt(patientId.length() - 1);
    }

    /** Mask generic sensitive field */
    public String maskSensitive(String value) {
      if (value == null || value.isEmpty()) return value;
      if (allowSensitiveLogging) return value;

      return MASK;
    }

    /** Check if sensitive data logging is allowed */
    public boolean isSensitiveLoggingAllowed() {
      return allowSensitiveLogging;
    }
  }
}
