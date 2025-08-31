package com.chubini.pku.moderation.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request DTO for moderation review actions */
@Schema(description = "Request for reviewing a moderation submission")
public record ReviewRequest(
    @Schema(description = "Reviewer identifier", example = "moderator-123", required = true)
        @NotBlank(message = "Reviewer ID is required")
        String reviewerId,
    @Schema(
            description = "Review notes or comments",
            example = "Approved with updated nutritional values")
        String reviewNotes,
    @Schema(
            description = "Field mappings for merge operations",
            example = "{\"name\": \"Updated Product Name\", \"calories\": \"120\"}")
        Map<String, String> mergeData) {}
