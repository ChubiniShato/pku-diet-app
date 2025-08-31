package com.chubini.pku.norms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Request to create a new norm prescription")
public record CreateNormPrescriptionRequest(
    @Schema(description = "Patient identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "Patient ID is required")
        UUID patientId,
    @Schema(description = "Daily phenylalanine limit in mg", example = "300")
        @PositiveOrZero(message = "Phenylalanine limit must be non-negative")
        BigDecimal dailyPheMgLimit,
    @Schema(description = "Daily protein limit in grams", example = "15.5")
        @PositiveOrZero(message = "Protein limit must be non-negative")
        BigDecimal dailyProteinGLimit,
    @Schema(description = "Daily minimum calories", example = "1800")
        @PositiveOrZero(message = "Calorie minimum must be non-negative")
        BigDecimal dailyKcalMin,
    @Schema(description = "Daily maximum calories", example = "2200")
        @PositiveOrZero(message = "Calorie maximum must be non-negative")
        BigDecimal dailyKcalMax,
    @Schema(description = "Daily maximum fat in grams", example = "65")
        @PositiveOrZero(message = "Fat maximum must be non-negative")
        BigDecimal dailyFatGMax,
    @Schema(description = "Prescription effective date", example = "2024-01-01")
        @NotNull(message = "Effective date is required")
        LocalDate effectiveFrom,
    @Schema(description = "Prescription expiry date", example = "2024-12-31")
        LocalDate effectiveUntil,
    @Schema(description = "Prescribing doctor name", example = "Dr. Smith") String prescribedBy,
    @Schema(description = "Additional clinical notes", example = "Adjusted for growth phase")
        String clinicalNotes) {}
