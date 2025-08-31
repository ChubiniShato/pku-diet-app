package com.chubini.pku.labelscan.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for label scan status and results */
@Schema(description = "Response containing label scan status and processing results")
public record LabelScanStatusResponse(
    @Schema(
            description = "Submission unique identifier",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID submissionId,
    @Schema(
            description = "Patient unique identifier",
            example = "550e8400-e29b-41d4-a716-446655440000")
        UUID patientId,
    @Schema(description = "Current processing status", example = "MATCHED") String status,
    @Schema(description = "Progress percentage (0-100)", example = "100") int progressPercentage,
    @Schema(
            description = "List of uploaded image paths",
            example = "[\"images/2024/01/15/patient_123/image1.jpg\"]")
        List<String> imagePaths,
    @Schema(description = "Barcode found on label", example = "123456789012") String barcode,
    @Schema(description = "Region used for processing", example = "US") String region,
    @Schema(
            description = "Extracted OCR text (redacted for privacy)",
            example = "INGREDIENTS: Water, Sugar...")
        String ocrText,
    @Schema(description = "OCR confidence score (0.0-1.0)", example = "0.85") Double ocrConfidence,
    @Schema(
            description = "Extracted fields from OCR",
            example =
                "{\"ingredients\": \"Water, Sugar, Artificial Flavors\", \"nutrition\": \"Calories: 120\"}")
        Map<String, String> extractedFields,
    @Schema(description = "Matched product information") MatchedProductInfo matchedProduct,
    @Schema(description = "Safety analysis results") SafetyAnalysisResult safetyAnalysis,
    @Schema(
            description = "General warnings from processing",
            example = "[\"Low OCR confidence\", \"Unclear text\"]")
        List<String> warnings,
    @Schema(description = "Error message if processing failed", example = "OCR service unavailable")
        String errorMessage,
    @Schema(
            description = "Review notes from manual review",
            example = "Verified ingredients manually")
        String reviewNotes,
    @Schema(description = "When processing was completed", example = "2024-01-15T10:31:30")
        LocalDateTime processedAt,
    @Schema(description = "When submission was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
    @Schema(description = "When submission was last updated", example = "2024-01-15T10:31:30")
        LocalDateTime updatedAt) {

  @Schema(description = "Information about matched product")
  public record MatchedProductInfo(
      @Schema(
              description = "Product unique identifier",
              example = "550e8400-e29b-41d4-a716-446655440001")
          UUID productId,
      @Schema(description = "Product name", example = "Test Product") String productName,
      @Schema(description = "Product brand", example = "Test Brand") String brand,
      @Schema(description = "Product category", example = "snacks") String category,
      @Schema(description = "Match confidence score (0.0-1.0)", example = "0.95")
          Double matchConfidence,
      @Schema(description = "Match source", example = "BARCODE") String matchSource) {}

  @Schema(description = "Safety analysis results")
  public record SafetyAnalysisResult(
      @Schema(description = "Overall safety level", example = "SAFE") String overallLevel,
      @Schema(description = "Detected allergens", example = "[\"nuts\", \"dairy\"]")
          List<String> allergenHits,
      @Schema(
              description = "Detected forbidden ingredients",
              example = "[\"artificial_sweeteners\"]")
          List<String> forbiddenHits,
      @Schema(description = "General safety warnings", example = "[\"Contains artificial colors\"]")
          List<String> warnings,
      @Schema(description = "Analysis confidence score (0.0-1.0)", example = "0.90")
          Double confidence,
      @Schema(description = "Whether there are any safety concerns", example = "false")
          boolean hasSafetyConcerns,
      @Schema(description = "Total number of safety concerns", example = "0")
          int totalConcernCount) {}
}
