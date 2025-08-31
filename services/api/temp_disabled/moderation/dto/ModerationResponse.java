package com.chubini.pku.moderation.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for moderation submission */
@Schema(description = "Response containing moderation submission details")
public record ModerationResponse(
    @Schema(
            description = "Submission unique identifier",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID submissionId,
    @Schema(description = "Patient identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID patientId,
    @Schema(description = "Target type", example = "CUSTOM_PRODUCT") String targetType,
    @Schema(
            description = "Payload reference",
            example = "custom_product:550e8400-e29b-41d4-a716-446655440000")
        String payloadRef,
    @Schema(description = "Submission title", example = "Organic Apple Sauce")
        String submissionTitle,
    @Schema(description = "Submission description", example = "New organic apple sauce product")
        String submissionDescription,
    @Schema(description = "Submission status", example = "PENDING") String status,
    @Schema(
            description = "Detected conflicts",
            example = "[\"Potential duplicate: Similar product exists\"]")
        List<String> conflicts,
    @Schema(
            description = "Review notes from moderator",
            example = "Approved with minor corrections")
        String reviewNotes,
    @Schema(description = "Reviewer identifier", example = "moderator-123") String reviewerId,
    @Schema(description = "When review was completed", example = "2024-01-15T14:30:00")
        LocalDateTime reviewedAt,
    @Schema(
            description = "ID of approved product in global catalog",
            example = "550e8400-e29b-41d4-a716-446655440001")
        UUID approvedProductId,
    @Schema(description = "Who created the submission", example = "patient-app-v1.2")
        String createdBy,
    @Schema(description = "When submission was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
    @Schema(description = "When submission was last updated", example = "2024-01-15T14:30:00")
        LocalDateTime updatedAt,
    @Schema(description = "When submission expires", example = "2024-02-14T10:30:00")
        LocalDateTime expiresAt,
    @Schema(description = "Whether submission has expired", example = "false") boolean isExpired) {}
