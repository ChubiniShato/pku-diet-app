package com.chubini.pku.sharing.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for share link operations */
@Schema(description = "Response containing share link information")
public record ShareLinkResponse(
    @Schema(
            description = "Share link unique identifier",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID shareLinkId,
    @Schema(description = "Patient identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID patientId,
    @Schema(description = "Masked token (for security)", example = "abcd1234...wxyz9876")
        String maskedToken,
    @Schema(description = "Doctor email address", example = "doctor@example.com")
        String doctorEmail,
    @Schema(description = "Doctor name", example = "Dr. Smith") String doctorName,
    @Schema(description = "Shared scopes", example = "[\"CRITICAL_FACTS\", \"DAY\"]")
        Set<String> scopes,
    @Schema(description = "Whether link is one-time use only", example = "true") boolean oneTimeUse,
    @Schema(description = "Device bound identifier", example = "device-123-abc") String deviceBound,
    @Schema(description = "Time to live in hours", example = "48") Integer ttlHours,
    @Schema(description = "Link status", example = "ACTIVE") String status,
    @Schema(description = "When link expires", example = "2024-01-17T10:30:00")
        LocalDateTime expiresAt,
    @Schema(description = "Whether link is currently usable", example = "true") boolean isUsable,
    @Schema(description = "Number of times link has been used", example = "0") Integer usageCount,
    @Schema(description = "When link was first used", example = "2024-01-15T14:20:00")
        LocalDateTime firstUsedAt,
    @Schema(description = "When link was last used", example = "2024-01-15T16:45:00")
        LocalDateTime lastUsedAt,
    @Schema(description = "When link was revoked", example = "2024-01-15T12:00:00")
        LocalDateTime revokedAt,
    @Schema(description = "Notes about the share link", example = "Weekly checkup") String notes,
    @Schema(description = "Who created the link", example = "patient-app-v1.2") String createdBy,
    @Schema(description = "Who revoked the link", example = "patient-app-v1.2") String revokedBy,
    @Schema(description = "When record was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
    @Schema(description = "When record was last updated", example = "2024-01-15T10:30:00")
        LocalDateTime updatedAt) {}
