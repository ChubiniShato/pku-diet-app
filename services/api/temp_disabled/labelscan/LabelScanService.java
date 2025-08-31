package com.chubini.pku.labelscan;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.chubini.pku.labelscan.dto.LabelScanRequest;
import com.chubini.pku.labelscan.dto.LabelScanResponse;
import com.chubini.pku.labelscan.dto.LabelScanStatusResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Main service for orchestrating the label scan pipeline */
@Service
@RequiredArgsConstructor
@Slf4j
public class LabelScanService {

  private final LabelScanSubmissionRepository submissionRepository;
  private final ImageStorageService imageStorageService;
  private final OcrService ocrService;
  private final BarcodeLookupService barcodeLookupService;
  private final SafetyFlaggingService safetyFlaggingService;

  // TODO: Inject repositories for patients, products, etc.

  /** Process a label scan request */
  @Transactional
  public LabelScanResponse processLabelScan(LabelScanRequest request) {
    log.info("Processing label scan request for patient: {}", request.patientId());

    // Create initial submission
    LabelScanSubmission submission =
        LabelScanSubmission.builder()
            .patient(null) // TODO: Get patient from repository
            .region(request.region())
            .barcode(request.barcode())
            .status(LabelScanSubmission.ScanStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

    submission = submissionRepository.save(submission);

    // Store images asynchronously
    List<String> imagePaths = new ArrayList<>();
    for (var image : request.images()) {
      try {
        var storageResult = imageStorageService.storeImage(image, request.patientId());
        if (storageResult.isSuccess()) {
          imagePaths.add(storageResult.getStoredPath());
        } else {
          log.error("Failed to store image: {}", storageResult.getErrorMessage());
        }
      } catch (Exception e) {
        log.error("Error storing image", e);
      }
    }

    // Update submission with image paths
    submission.setImagePaths(imagePaths);
    submission = submissionRepository.save(submission);

    // Start async processing
    processSubmissionAsync(submission.getId());

    return new LabelScanResponse(
        submission.getId(),
        submission.getStatus().toString(),
        imagePaths.size(),
        estimateProcessingTime(imagePaths.size()),
        "Images uploaded successfully. Processing will complete shortly.",
        submission.getCreatedAt(),
        false);
  }

  /** Get scan status and results */
  public LabelScanStatusResponse getScanStatus(UUID submissionId, UUID patientId) {
    Optional<LabelScanSubmission> submissionOpt = submissionRepository.findById(submissionId);

    if (submissionOpt.isEmpty()) {
      return null;
    }

    LabelScanSubmission submission = submissionOpt.get();

    // TODO: Check if submission belongs to patient
    // if (!submission.getPatient().getId().equals(patientId)) {
    //     throw new SecurityException("Access denied");
    // }

    return mapToStatusResponse(submission);
  }

  /** Get scan history for a patient */
  public LabelScanController.ScanHistoryResponse getScanHistory(
      UUID patientId, int page, int size, LabelScanSubmission.ScanStatus status) {
    // TODO: Implement proper pagination
    // For now, return empty response
    return new LabelScanController.ScanHistoryResponse(
        Collections.emptyList(), page, 0, 0, false, page > 0);
  }

  /** Retry a failed scan */
  @Transactional
  public boolean retryScan(UUID submissionId, UUID patientId) {
    Optional<LabelScanSubmission> submissionOpt = submissionRepository.findById(submissionId);

    if (submissionOpt.isEmpty()) {
      return false;
    }

    LabelScanSubmission submission = submissionOpt.get();

    // TODO: Check patient access

    if (submission.getStatus() != LabelScanSubmission.ScanStatus.FAILED
        && submission.getStatus() != LabelScanSubmission.ScanStatus.REQUIRES_REVIEW) {
      return false;
    }

    // Reset status and retry processing
    submission.setStatus(LabelScanSubmission.ScanStatus.PENDING);
    submission.setErrorMessage(null);
    submissionRepository.save(submission);

    processSubmissionAsync(submissionId);
    return true;
  }

  /** Get supported regions */
  public List<String> getSupportedRegions() {
    // TODO: Aggregate from all barcode lookup services
    return Arrays.asList("US", "EU", "CA", "AU", "GB", "FR", "DE", "IT", "ES", "NL", "GE", "RU");
  }

  /** Get system status */
  public LabelScanController.SystemStatusResponse getSystemStatus() {
    return new LabelScanController.SystemStatusResponse(
        ocrService.isAvailable(),
        barcodeLookupService.isAvailable(),
        getSupportedRegions(),
        "1.0.0" // TODO: Get from configuration
        );
  }

  /** Process submission asynchronously */
  private void processSubmissionAsync(UUID submissionId) {
    CompletableFuture.runAsync(
        () -> {
          try {
            processSubmission(submissionId);
          } catch (Exception e) {
            log.error("Error processing submission {}", submissionId, e);
            markSubmissionFailed(submissionId, "Processing failed: " + e.getMessage());
          }
        });
  }

  /** Process a submission through the entire pipeline */
  private void processSubmission(UUID submissionId) {
    log.info("Starting processing for submission: {}", submissionId);

    Optional<LabelScanSubmission> submissionOpt = submissionRepository.findById(submissionId);
    if (submissionOpt.isEmpty()) {
      log.error("Submission not found: {}", submissionId);
      return;
    }

    LabelScanSubmission submission = submissionOpt.get();
    submission.setStatus(LabelScanSubmission.ScanStatus.PROCESSING);
    submission = submissionRepository.save(submission);

    try {
      // Step 1: OCR Processing
      List<Path> imagePaths =
          submission.getImagePaths().stream().map(path -> Paths.get(path)).toList();

      OcrService.OcrResult ocrResult = ocrService.extractTextFromMultipleImages(imagePaths);

      if (!ocrResult.isSuccess()) {
        throw new RuntimeException("OCR processing failed: " + ocrResult.getErrorMessage());
      }

      // Update submission with OCR results
      submission.setOcrText(ocrResult.getExtractedText());
      submission.setOcrConfidence(ocrResult.getConfidence());
      submission.setExtractedFields(extractFieldsFromText(ocrResult.getExtractedText()));

      // Step 2: Barcode Lookup
      BarcodeLookupService.BarcodeLookupResult barcodeResult = null;
      if (submission.getBarcode() != null) {
        barcodeResult =
            barcodeLookupService.lookupByBarcode(submission.getBarcode(), submission.getRegion());
      }

      // Step 3: Safety Analysis
      SafetyFlaggingService.SafetyAnalysis safetyAnalysis = null;
      if (ocrResult.getExtractedText() != null) {
        safetyAnalysis =
            safetyFlaggingService.analyzeText(
                ocrResult.getExtractedText(), submission.getPatient());
      }

      if (barcodeResult != null && barcodeResult.isFound()) {
        SafetyFlaggingService.SafetyAnalysis productSafety =
            safetyFlaggingService.analyzeProductInfo(
                barcodeResult.getProductInfo(), submission.getPatient());

        // Merge safety analyses
        if (safetyAnalysis != null) {
          safetyAnalysis = mergeSafetyAnalyses(safetyAnalysis, productSafety);
        } else {
          safetyAnalysis = productSafety;
        }

        // Set matched product
        submission.setMatchedProduct(null); // TODO: Create/find product
        submission.setMatchConfidence(barcodeResult.getProductInfo().getConfidence());
        submission.setMatchSource("BARCODE");
      }

      // Update submission with safety results
      if (safetyAnalysis != null) {
        submission.setAllergenHits(safetyAnalysis.getAllergenHits());
        submission.setForbiddenHits(safetyAnalysis.getForbiddenHits());
        submission.setWarnings(safetyAnalysis.getWarnings());
      }

      // Determine final status
      LabelScanSubmission.ScanStatus finalStatus =
          determineFinalStatus(barcodeResult, safetyAnalysis, ocrResult);

      submission.setStatus(finalStatus);
      submission.setProcessedAt(LocalDateTime.now());

      submissionRepository.save(submission);

      log.info("Completed processing for submission {} with status: {}", submissionId, finalStatus);

      // TODO: Publish events for notifications
      // publishLabelScanCompletedEvent(submission);

    } catch (Exception e) {
      log.error("Error processing submission {}", submissionId, e);
      markSubmissionFailed(submissionId, e.getMessage());
    }
  }

  /** Mark submission as failed */
  private void markSubmissionFailed(UUID submissionId, String errorMessage) {
    Optional<LabelScanSubmission> submissionOpt = submissionRepository.findById(submissionId);
    if (submissionOpt.isPresent()) {
      LabelScanSubmission submission = submissionOpt.get();
      submission.markFailed(errorMessage);
      submissionRepository.save(submission);
    }
  }

  /** Extract structured fields from OCR text */
  private Map<String, String> extractFieldsFromText(String text) {
    Map<String, String> fields = new HashMap<>();

    if (text == null) return fields;

    // Simple extraction logic - could be enhanced
    String lowerText = text.toLowerCase();

    // Extract ingredients section
    int ingredientsStart = lowerText.indexOf("ingredients");
    if (ingredientsStart != -1) {
      String ingredients = extractSection(text, ingredientsStart, lowerText);
      fields.put("ingredients", ingredients);
    }

    // Extract nutrition section
    int nutritionStart = lowerText.indexOf("nutrition");
    if (nutritionStart != -1) {
      String nutrition = extractSection(text, nutritionStart, lowerText);
      fields.put("nutrition", nutrition);
    }

    // Extract allergens section
    int allergensStart = lowerText.indexOf("allergens");
    if (allergensStart != -1) {
      String allergens = extractSection(text, allergensStart, lowerText);
      fields.put("allergens", allergens);
    }

    return fields;
  }

  /** Extract a section of text starting from a position */
  private String extractSection(String text, int startPos, String lowerText) {
    // Find the end of the section (next section or end of text)
    String[] sectionHeaders = {"ingredients", "nutrition", "allergens", "serving", "directions"};
    int endPos = text.length();

    for (String header : sectionHeaders) {
      int headerPos = lowerText.indexOf(header, startPos + 1);
      if (headerPos != -1 && headerPos < endPos) {
        endPos = headerPos;
      }
    }

    return text.substring(startPos, Math.min(endPos, startPos + 500)).trim();
  }

  /** Merge two safety analyses */
  private SafetyFlaggingService.SafetyAnalysis mergeSafetyAnalyses(
      SafetyFlaggingService.SafetyAnalysis analysis1,
      SafetyFlaggingService.SafetyAnalysis analysis2) {

    List<String> combinedAllergens = new ArrayList<>(analysis1.getAllergenHits());
    combinedAllergens.addAll(analysis2.getAllergenHits());

    List<String> combinedForbidden = new ArrayList<>(analysis1.getForbiddenHits());
    combinedForbidden.addAll(analysis2.getForbiddenHits());

    List<String> combinedWarnings = new ArrayList<>(analysis1.getWarnings());
    combinedWarnings.addAll(analysis2.getWarnings());

    SafetyFlaggingService.SafetyLevel highestLevel =
        analysis1.getOverallLevel().ordinal() > analysis2.getOverallLevel().ordinal()
            ? analysis1.getOverallLevel()
            : analysis2.getOverallLevel();

    double avgConfidence = (analysis1.getConfidence() + analysis2.getConfidence()) / 2.0;

    return new SafetyFlaggingService.SafetyAnalysis(
        highestLevel, combinedAllergens, combinedForbidden, combinedWarnings, avgConfidence);
  }

  /** Determine final status based on processing results */
  private LabelScanSubmission.ScanStatus determineFinalStatus(
      BarcodeLookupService.BarcodeLookupResult barcodeResult,
      SafetyFlaggingService.SafetyAnalysis safetyAnalysis,
      OcrService.OcrResult ocrResult) {

    // If OCR failed, mark as failed
    if (!ocrResult.isSuccess()) {
      return LabelScanSubmission.ScanStatus.FAILED;
    }

    // If safety concerns found, requires review
    if (safetyAnalysis != null && safetyAnalysis.hasSafetyConcerns()) {
      return LabelScanSubmission.ScanStatus.REQUIRES_REVIEW;
    }

    // If barcode lookup succeeded, mark as matched
    if (barcodeResult != null && barcodeResult.isFound()) {
      return LabelScanSubmission.ScanStatus.MATCHED;
    }

    // If OCR succeeded but no match, requires review
    if (ocrResult.getConfidence() < 0.5) {
      return LabelScanSubmission.ScanStatus.REQUIRES_REVIEW;
    }

    return LabelScanSubmission.ScanStatus.MATCHED;
  }

  /** Estimate processing time based on image count */
  private int estimateProcessingTime(int imageCount) {
    // Base time: 10 seconds, plus 5 seconds per image
    return 10 + (imageCount * 5);
  }

  /** Map submission to status response */
  private LabelScanStatusResponse mapToStatusResponse(LabelScanSubmission submission) {
    LabelScanStatusResponse.MatchedProductInfo matchedProduct = null;
    if (submission.getMatchedProduct() != null) {
      matchedProduct =
          new LabelScanStatusResponse.MatchedProductInfo(
              submission.getMatchedProduct().getId(),
              submission.getMatchedProduct().getProductName(),
              "", // TODO: Add brand field to Product entity
              "", // TODO: Add category field to Product entity
              submission.getMatchConfidence(),
              submission.getMatchSource());
    }

    LabelScanStatusResponse.SafetyAnalysisResult safetyAnalysis = null;
    if (submission.getAllergenHits() != null || submission.getForbiddenHits() != null) {
      SafetyFlaggingService.SafetyLevel level = determineSafetyLevel(submission);
      safetyAnalysis =
          new LabelScanStatusResponse.SafetyAnalysisResult(
              level.toString(),
              submission.getAllergenHits(),
              submission.getForbiddenHits(),
              submission.getWarnings(),
              0.85, // TODO: Calculate actual confidence
              submission.hasSafetyConcerns(),
              submission.getSafetyFlagCount());
    }

    return new LabelScanStatusResponse(
        submission.getId(),
        submission.getPatient().getId(),
        submission.getStatus().toString(),
        calculateProgress(submission),
        submission.getImagePaths(),
        submission.getBarcode(),
        submission.getRegion(),
        submission.getOcrText(), // TODO: Consider redacting sensitive info
        submission.getOcrConfidence(),
        submission.getExtractedFields(),
        matchedProduct,
        safetyAnalysis,
        submission.getWarnings(),
        submission.getErrorMessage(),
        submission.getReviewNotes(),
        submission.getProcessedAt(),
        submission.getCreatedAt(),
        submission.getUpdatedAt());
  }

  /** Calculate progress percentage */
  private int calculateProgress(LabelScanSubmission submission) {
    switch (submission.getStatus()) {
      case PENDING:
        return 0;
      case PROCESSING:
        return 50;
      case MATCHED:
      case REQUIRES_REVIEW:
      case APPROVED:
      case REJECTED:
        return 100;
      case FAILED:
        return 0;
      default:
        return 0;
    }
  }

  /** Determine safety level from submission data */
  private SafetyFlaggingService.SafetyLevel determineSafetyLevel(LabelScanSubmission submission) {
    if (submission.getAllergenHits() != null && !submission.getAllergenHits().isEmpty()) {
      return SafetyFlaggingService.SafetyLevel.DANGER;
    }
    if (submission.getForbiddenHits() != null && !submission.getForbiddenHits().isEmpty()) {
      return SafetyFlaggingService.SafetyLevel.WARNING;
    }
    return SafetyFlaggingService.SafetyLevel.SAFE;
  }
}
