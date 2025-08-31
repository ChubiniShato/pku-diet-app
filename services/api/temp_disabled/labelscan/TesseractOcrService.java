package com.chubini.pku.labelscan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/** Tesseract OCR implementation */
@Service
@Slf4j
public class TesseractOcrService implements OcrService {

  @Value("${ocr.tesseract.enabled:true}")
  private boolean enabled;

  @Value("${ocr.tesseract.path:tesseract}")
  private String tesseractPath;

  @Value("${ocr.tesseract.language:eng}")
  private String defaultLanguage;

  @Value("${ocr.tesseract.config:--psm 3}")
  private String tesseractConfig;

  // Common OCR quality indicators
  private static final Pattern LOW_CONFIDENCE_PATTERN =
      Pattern.compile("\\b(low|unclear|blurry|dark)\\b", Pattern.CASE_INSENSITIVE);
  private static final Pattern QUALITY_KEYWORDS =
      Pattern.compile("\\b(ingredients|nutrition|allergens|contains)\\b", Pattern.CASE_INSENSITIVE);

  @Override
  public OcrResult extractText(Path imagePath) {
    return extractTextFromMultipleImages(List.of(imagePath));
  }

  @Override
  public OcrResult extractTextFromMultipleImages(List<Path> imagePaths) {
    if (!enabled) {
      log.warn("Tesseract OCR is disabled, returning mock result");
      return createMockResult();
    }

    if (!isTesseractAvailable()) {
      log.warn("Tesseract is not available, returning mock result");
      return createUnavailableResult();
    }

    if (imagePaths == null || imagePaths.isEmpty()) {
      return OcrResult.builder().success(false).errorMessage("No image paths provided").build();
    }

    List<String> allExtractedText = new ArrayList<>();
    List<String> allWarnings = new ArrayList<>();
    double totalConfidence = 0.0;
    int processedImages = 0;

    for (Path imagePath : imagePaths) {
      if (!Files.exists(imagePath)) {
        allWarnings.add("Image file not found: " + imagePath.getFileName());
        continue;
      }

      try {
        TesseractResult result = runTesseract(imagePath);
        if (result.success) {
          allExtractedText.add(result.text);
          totalConfidence += result.confidence;
          processedImages++;

          if (result.confidence < 70.0) {
            allWarnings.add(
                String.format(
                    "Low OCR confidence (%.1f%%) for image: %s",
                    result.confidence, imagePath.getFileName()));
          }
        } else {
          allWarnings.add("Failed to process image: " + imagePath.getFileName());
        }
      } catch (Exception e) {
        log.error("Error processing image {}: {}", imagePath, e.getMessage());
        allWarnings.add("Error processing image: " + imagePath.getFileName());
      }
    }

    if (processedImages == 0) {
      return OcrResult.builder()
          .success(false)
          .errorMessage("No images could be processed")
          .warnings(allWarnings)
          .build();
    }

    String combinedText = String.join("\n\n", allExtractedText);
    double averageConfidence = totalConfidence / processedImages;

    // Add quality warnings
    addQualityWarnings(combinedText, allWarnings);

    Map<String, Object> metadata =
        Map.of(
            "processedImages",
            processedImages,
            "averageConfidence",
            averageConfidence,
            "totalCharacters",
            combinedText.length(),
            "hasIngredients",
            QUALITY_KEYWORDS.matcher(combinedText).find());

    return OcrResult.builder()
        .success(true)
        .extractedText(combinedText)
        .confidence(averageConfidence)
        .metadata(metadata)
        .warnings(allWarnings.isEmpty() ? null : allWarnings)
        .build();
  }

  @Override
  public boolean isAvailable() {
    return enabled && isTesseractAvailable();
  }

  @Override
  public List<String> getSupportedLanguages() {
    // In a real implementation, this would query tesseract for supported languages
    return List.of("eng", "rus", "kat"); // English, Russian, Georgian
  }

  /** Check if Tesseract is available on the system */
  private boolean isTesseractAvailable() {
    try {
      ProcessBuilder pb = new ProcessBuilder(tesseractPath, "--version");
      Process process = pb.start();
      int exitCode = process.waitFor();
      return exitCode == 0;
    } catch (Exception e) {
      log.debug("Tesseract not available: {}", e.getMessage());
      return false;
    }
  }

  /** Run Tesseract on a single image */
  private TesseractResult runTesseract(Path imagePath) throws IOException, InterruptedException {
    // Create temporary file for output
    Path tempOutput = Files.createTempFile("tesseract_output", ".txt");

    try {
      ProcessBuilder pb =
          new ProcessBuilder(
              tesseractPath,
              imagePath.toString(),
              tempOutput.toString().replace(".txt", ""), // Tesseract adds .txt
              "-l",
              defaultLanguage,
              tesseractConfig);

      Process process = pb.start();
      int exitCode = process.waitFor();

      if (exitCode != 0) {
        // Try to read error output
        String errorOutput = readProcessError(process);
        return new TesseractResult(false, null, 0.0, errorOutput);
      }

      // Read the extracted text
      String extractedText = Files.readString(tempOutput);

      // Estimate confidence (in a real implementation, you'd parse tesseract's confidence output)
      double confidence = estimateConfidence(extractedText);

      return new TesseractResult(true, extractedText, confidence, null);

    } finally {
      // Clean up temp file
      try {
        Files.deleteIfExists(tempOutput);
      } catch (IOException e) {
        log.warn("Failed to delete temp file: {}", tempOutput);
      }
    }
  }

  /** Estimate OCR confidence based on text characteristics */
  private double estimateConfidence(String text) {
    if (text == null || text.trim().isEmpty()) {
      return 0.0;
    }

    double confidence = 80.0; // Base confidence

    // Reduce confidence for short text
    if (text.length() < 50) {
      confidence -= 20;
    }

    // Reduce confidence for text with many special characters (likely OCR errors)
    long specialCharCount =
        text.chars()
            .filter(ch -> !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch))
            .count();
    double specialCharRatio = (double) specialCharCount / text.length();
    if (specialCharRatio > 0.1) {
      confidence -= 15;
    }

    // Reduce confidence if text contains common OCR error indicators
    if (LOW_CONFIDENCE_PATTERN.matcher(text).find()) {
      confidence -= 10;
    }

    // Increase confidence if text contains expected keywords
    if (QUALITY_KEYWORDS.matcher(text).find()) {
      confidence += 5;
    }

    return Math.max(0.0, Math.min(100.0, confidence));
  }

  /** Add quality warnings based on extracted text */
  private void addQualityWarnings(String text, List<String> warnings) {
    if (text.length() < 100) {
      warnings.add("Extracted text is very short - may indicate poor image quality");
    }

    if (!QUALITY_KEYWORDS.matcher(text).find()) {
      warnings.add("Extracted text does not contain expected food label keywords");
    }

    long uppercaseRatio = text.chars().filter(Character::isUpperCase).count();
    double upperPercent =
        (double) uppercaseRatio / text.chars().filter(Character::isLetter).count();
    if (upperPercent > 0.8) {
      warnings.add("High proportion of uppercase text - may indicate OCR errors");
    }
  }

  /** Read process error output */
  private String readProcessError(Process process) {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      StringBuilder error = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        error.append(line).append("\n");
      }
      return error.toString().trim();
    } catch (IOException e) {
      return "Failed to read error output";
    }
  }

  /** Create mock result for testing when OCR is disabled */
  private OcrResult createMockResult() {
    return OcrResult.builder()
        .success(true)
        .extractedText(
            "MOCK OCR RESULT - Tesseract not configured\n\nINGREDIENTS: Water, Sugar, Artificial Flavors\n\nNUTRITION FACTS\nServing Size: 1 cup\nCalories: 120\n\nALLERGENS: May contain traces of nuts")
        .confidence(85.0)
        .metadata(Map.of("mock", true, "source", "TesseractOcrService"))
        .addWarning("Using mock OCR result - Tesseract not configured")
        .build();
  }

  /** Create unavailable result */
  private OcrResult createUnavailableResult() {
    return OcrResult.builder()
        .success(false)
        .errorMessage("Tesseract OCR is not available")
        .build();
  }

  /** Internal result class for Tesseract operations */
  private static class TesseractResult {
    final boolean success;
    final String text;
    final double confidence;
    final String error;

    TesseractResult(boolean success, String text, double confidence, String error) {
      this.success = success;
      this.text = text;
      this.confidence = confidence;
      this.error = error;
    }
  }
}
