package com.chubini.pku.consents.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for consent operations */
@Schema(description = "Response containing consent information")
public record ConsentResponse(
    @Schema(
            description = "Consent unique identifier",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID consentId,
    @Schema(description = "Patient identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID patientId,
    @Schema(description = "Consent type", example = "SHARE_WITH_DOCTOR") String consentType,
    @Schema(description = "Consent status", example = "GRANTED") String status,
    @Schema(description = "Consent version number", example = "1") Integer version,
    @Schema(description = "Reason for granting", example = "Doctor consultation")
        String grantedReason,
    @Schema(description = "Reason for revoking", example = "Consultation completed")
        String revokedReason,
    @Schema(description = "When consent was granted", example = "2024-01-15T10:30:00")
        LocalDateTime grantedAt,
    @Schema(description = "When consent was revoked", example = "2024-02-15T14:20:00")
        LocalDateTime revokedAt,
    @Schema(description = "When consent expires", example = "2024-12-31T23:59:59")
        LocalDateTime expiresAt,
    @Schema(description = "Whether consent is currently active", example = "true") boolean isActive,
    @Schema(description = "Whether consent has expired", example = "false") boolean isExpired,
    @Schema(description = "Who granted the consent", example = "patient-app-v1.2") String grantedBy,
    @Schema(description = "Who revoked the consent", example = "patient-app-v1.2") String revokedBy,
    @Schema(description = "When record was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
    @Schema(description = "When record was last updated", example = "2024-01-15T10:30:00")
        LocalDateTime updatedAt) {}
