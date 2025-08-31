package com.chubini.pku.moderation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request DTO for moderation submission */
@Schema(description = "Request for submitting an item to global catalog moderation")
public record ModerationRequest(
    @Schema(description = "Target type for moderation", example = "CUSTOM_PRODUCT", required = true)
        @NotBlank(message = "Target type is required")
        String targetType,
    @Schema(
            description = "Reference to the item being submitted (format: 'type:id')",
            example = "custom_product:550e8400-e29b-41d4-a716-446655440000",
            required = true)
        @NotBlank(message = "Payload reference is required")
        String payloadRef,
    @Schema(description = "Title for the submission", example = "Organic Apple Sauce") String title,
    @Schema(
            description = "Description of what is being submitted",
            example = "New organic apple sauce product with detailed nutritional information")
        String description,
    @Schema(
            description = "User/device identifier who created the submission",
            example = "patient-app-v1.2")
        String createdBy) {}
