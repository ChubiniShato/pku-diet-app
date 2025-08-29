package com.chubini.pku.patients.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request to create a new patient profile")
public record CreatePatientProfileRequest(
    @Schema(description = "Patient's full name", example = "John Doe")
    @NotBlank(message = "Name is required")
    String name,

    @Schema(description = "Patient's email address", example = "john.doe@example.com")
    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    String email,

    @Schema(description = "Date of birth", example = "1990-05-15")
    @NotNull(message = "Date of birth is required")
    LocalDate dateOfBirth,

    @Schema(description = "Gender", allowableValues = {"MALE", "FEMALE", "OTHER"})
    String gender,

    @Schema(description = "Current weight in kg", example = "70.5")
    @Positive(message = "Weight must be positive")
    BigDecimal weightKg,

    @Schema(description = "Height in cm", example = "175.5")
    @Positive(message = "Height must be positive")
    BigDecimal heightCm,

    @Schema(description = "Activity level", allowableValues = {"LOW", "MODERATE", "HIGH"})
    String activityLevel,

    @Schema(description = "Dietary preferences (comma-separated)", example = "vegetarian,low-sodium")
    String dietaryPreferences,

    @Schema(description = "Medical notes", example = "Classical PKU, diagnosed at birth")
    String medicalNotes
) {
}
