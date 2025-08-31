package com.chubini.pku.consents.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Request DTO for granting or revoking patient consent */
@Schema(description = "Request for granting or revoking patient consent")
public record ConsentRequest(
    @Schema(description = "Consent type", example = "SHARE_WITH_DOCTOR", required = true)
        @NotNull(message = "Consent type is required")
        ConsentType consentType,
    @Schema(description = "Consent action", example = "GRANT", required = true)
        @NotNull(message = "Action is required")
        ConsentAction action,
    @Schema(description = "Reason for the consent action", example = "Doctor consultation")
        String reason,
    @Schema(description = "Consent expiry date (optional)", example = "2024-12-31T23:59:59")
        LocalDateTime expiresAt,
    @Schema(
            description = "User/device identifier performing the action",
            example = "patient-app-v1.2")
        String performedBy) {

  public enum ConsentAction {
    GRANT,
    REVOKE
  }

  public enum ConsentType {
    GLOBAL_SUBMISSION_OPTIN,
    SHARE_WITH_DOCTOR,
    EMERGENCY_ACCESS,
    RESEARCH_OPTIN
  }
}
