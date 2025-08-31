package com.chubini.pku.patients.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request to update patient profile")
public record UpdatePatientProfileRequest(
    @Schema(description = "Patient's full name", example = "John Doe") String name,
    @Schema(description = "Patient's email address", example = "john.doe@example.com")
        @Email(message = "Valid email is required")
        String email,
    @Schema(description = "Date of birth", example = "1990-05-15") LocalDate dateOfBirth,
    @Schema(
            description = "Gender",
            allowableValues = {"MALE", "FEMALE", "OTHER"})
        String gender,
    @Schema(description = "Current weight in kg", example = "70.5")
        @Positive(message = "Weight must be positive")
        BigDecimal weightKg,
    @Schema(description = "Height in cm", example = "175.5")
        @Positive(message = "Height must be positive")
        BigDecimal heightCm,
    @Schema(
            description = "Activity level",
            allowableValues = {"LOW", "MODERATE", "HIGH"})
        String activityLevel,
    @Schema(
            description = "Dietary preferences (comma-separated)",
            example = "vegetarian,low-sodium")
        String dietaryPreferences,
    @Schema(description = "Medical notes", example = "Classical PKU, diagnosed at birth")
        String medicalNotes) {}
