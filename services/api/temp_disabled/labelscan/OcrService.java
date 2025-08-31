package com.chubini.pku.labelscan;

import java.nio.file.Path;
import java.util.Map;

/** OCR service interface for extracting text from label images */
public interface OcrService {

  /** Extract text from image file */
  OcrResult extractText(Path imagePath);

  /** Extract text from multiple images */
  OcrResult extractTextFromMultipleImages(java.util.List<Path> imagePaths);

  /** Check if OCR service is available */
  boolean isAvailable();

  /** Get supported languages */
  java.util.List<String> getSupportedLanguages();

  /** OCR result containing extracted text and metadata */
  class OcrResult {
    private final boolean success;
    private final String extractedText;
    private final double confidence;
    private final Map<String, Object> metadata;
    private final String errorMessage;
    private final java.util.List<String> warnings;

    public OcrResult(
        boolean success,
        String extractedText,
        double confidence,
        Map<String, Object> metadata,
        String errorMessage,
        java.util.List<String> warnings) {
      this.success = success;
      this.extractedText = extractedText;
      this.confidence = confidence;
      this.metadata = metadata;
      this.errorMessage = errorMessage;
      this.warnings = warnings;
    }

    public static Builder builder() {
      return new Builder();
    }

    // Getters
    public boolean isSuccess() {
      return success;
    }

    public String getExtractedText() {
      return extractedText;
    }

    public double getConfidence() {
      return confidence;
    }

    public Map<String, Object> getMetadata() {
      return metadata;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public java.util.List<String> getWarnings() {
      return warnings;
    }

    public static class Builder {
      private boolean success = true;
      private String extractedText;
      private double confidence;
      private Map<String, Object> metadata;
      private String errorMessage;
      private java.util.List<String> warnings;

      public Builder success(boolean success) {
        this.success = success;
        return this;
      }

      public Builder extractedText(String extractedText) {
        this.extractedText = extractedText;
        return this;
      }

      public Builder confidence(double confidence) {
        this.confidence = confidence;
        return this;
      }

      public Builder metadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
      }

      public Builder errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
        return this;
      }

      public Builder warnings(java.util.List<String> warnings) {
        this.warnings = warnings;
        return this;
      }

      public Builder addWarning(String warning) {
        if (this.warnings == null) {
          this.warnings = new java.util.ArrayList<>();
        }
        this.warnings.add(warning);
        return this;
      }

      public OcrResult build() {
        return new OcrResult(success, extractedText, confidence, metadata, errorMessage, warnings);
      }
    }
  }
}
