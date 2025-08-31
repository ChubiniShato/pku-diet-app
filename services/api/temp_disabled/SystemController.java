package com.chubini.pku.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.chubini.pku.i18n.MessageService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** System configuration and status endpoints */
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "System", description = "System configuration and status endpoints")
public class SystemController {

  private final MessageService messageService;
  private final PhiProtectionConfig.PhiMasker phiMasker;

  @Operation(
      summary = "Get system configuration",
      description = "Returns current system configuration and supported features")
  @GetMapping("/config")
  public ResponseEntity<Map<String, Object>> getSystemConfig() {
    Map<String, Object> config = new HashMap<>();

    config.put("supportedLocales", messageService.getSupportedLocales());
    config.put("defaultLocale", messageService.getDefaultLocale());
    config.put("phiProtectionEnabled", !phiMasker.isSensitiveLoggingAllowed());
    config.put("version", "1.0.0");
    config.put(
        "features",
        Map.of(
            "emergencyMode", false,
            "pantryUsage", true,
            "notifications", true,
            "sharing", true,
            "labelScan", true));

    return ResponseEntity.ok(config);
  }

  @Operation(
      summary = "Test internationalization",
      description = "Test localized messages based on Accept-Language header")
  @GetMapping("/i18n-test")
  public ResponseEntity<Map<String, String>> testI18n(
      @Parameter(description = "Accept-Language header for locale resolution")
          @RequestHeader(value = "Accept-Language", required = false)
          String acceptLanguage) {

    Locale locale = messageService.resolveLocale(acceptLanguage);
    Map<String, String> messages = new HashMap<>();

    // Test validation messages
    messages.put(
        "patientNotFound",
        messageService.getValidationMessage("patient.not.found", new Object[] {"123"}, locale));
    messages.put("normsNotFound", messageService.getValidationMessage("norms.not.found", locale));

    // Test general messages
    messages.put("uploadSuccess", messageService.getLabelScanMessage("upload.success", locale));
    messages.put(
        "processingStatus", messageService.getLabelScanMessage("status.processing", locale));

    // Add locale information
    messages.put("detectedLocale", locale.toString());
    messages.put("localeSupported", String.valueOf(messageService.isSupportedLocale(locale)));

    return ResponseEntity.ok(messages);
  }

  @Operation(
      summary = "Test PHI masking",
      description = "Demonstrates PHI data masking (for testing purposes only)")
  @GetMapping("/phi-test")
  public ResponseEntity<Map<String, String>> testPhiMasking() {
    Map<String, String> examples = new HashMap<>();

    // Only show examples if sensitive logging is enabled
    if (phiMasker.isSensitiveLoggingAllowed()) {
      examples.put("originalEmail", "patient@example.com");
      examples.put("originalPhone", "+1234567890");
      examples.put("originalName", "John Doe");
      examples.put("originalDob", "1990-01-15");
      examples.put("originalPatientId", "PATIENT-12345");
    }

    // Always show masked versions
    examples.put("maskedEmail", phiMasker.maskEmail("patient@example.com"));
    examples.put("maskedPhone", phiMasker.maskPhone("+1234567890"));
    examples.put("maskedName", phiMasker.maskName("John Doe"));
    examples.put("maskedDob", phiMasker.maskDob("1990-01-15"));
    examples.put("maskedPatientId", phiMasker.maskPatientId("PATIENT-12345"));

    examples.put("phiProtectionEnabled", String.valueOf(!phiMasker.isSensitiveLoggingAllowed()));

    return ResponseEntity.ok(examples);
  }
}
