package com.chubini.pku.labelscan.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for label scan submission */
@Schema(description = "Response containing label scan submission details")
public record LabelScanResponse(
    @Schema(
            description = "Submission unique identifier",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID submissionId,
    @Schema(description = "Current processing status", example = "PENDING") String status,
    @Schema(description = "Number of images uploaded", example = "2") int imageCount,
    @Schema(description = "Estimated processing time in seconds", example = "30")
        int estimatedProcessingTimeSeconds,
    @Schema(
            description = "Message for the user",
            example =
                "Images uploaded successfully. Processing will take approximately 30 seconds.")
        String message,
    @Schema(description = "When the submission was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
    @Schema(description = "Whether the submission requires review", example = "false")
        boolean requiresReview) {}
