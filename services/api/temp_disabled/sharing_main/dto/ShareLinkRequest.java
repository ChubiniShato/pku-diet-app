package com.chubini.pku.sharing.dto;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

/** Request DTO for creating share links */
@Schema(description = "Request for creating a share link")
public record ShareLinkRequest(
    @Schema(
            description = "Scopes to share",
            example = "[\"CRITICAL_FACTS\", \"DAY\"]",
            required = true)
        @NotEmpty(message = "At least one scope must be specified")
        Set<ShareScope> scopes,
    @Schema(description = "Doctor email address", example = "doctor@example.com")
        String doctorEmail,
    @Schema(description = "Doctor name", example = "Dr. Smith") String doctorName,
    @Schema(description = "Time to live in hours", example = "48") Integer ttlHours,
    @Schema(description = "Whether link is one-time use only", example = "true") Boolean oneTimeUse,
    @Schema(description = "Device fingerprint for security", example = "device-123-abc")
        String deviceBound,
    @Schema(description = "Reason for creating the link", example = "Weekly checkup") String notes,
    @Schema(description = "User/device identifier creating the link", example = "patient-app-v1.2")
        String createdBy) {

  public enum ShareScope {
    CRITICAL_FACTS,
    DAY,
    WEEK,
    RANGE,
    NUTRITION_SUMMARY
  }
}
