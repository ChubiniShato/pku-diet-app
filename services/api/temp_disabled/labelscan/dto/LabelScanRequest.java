package com.chubini.pku.labelscan.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Request DTO for label scan processing */
@Schema(description = "Request for processing food label images")
public record LabelScanRequest(
    @Schema(
            description = "Patient unique identifier",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true)
        @NotNull(message = "Patient ID is required")
        UUID patientId,
    @Schema(description = "Food label images to process", required = true)
        @NotEmpty(message = "At least one image is required")
        List<MultipartFile> images,
    @Schema(description = "Region code for barcode lookup (ISO 3166-1 alpha-2)", example = "US")
        String region,
    @Schema(description = "Barcode if visible on the label", example = "123456789012")
        String barcode,
    @Schema(
            description = "User/device identifier who initiated the scan",
            example = "patient-app-v1.2")
        String createdBy) {}
