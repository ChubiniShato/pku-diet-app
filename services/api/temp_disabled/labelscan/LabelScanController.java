package com.chubini.pku.labelscan;

import java.util.List;
import java.util.UUID;

import com.chubini.pku.labelscan.dto.LabelScanRequest;
import com.chubini.pku.labelscan.dto.LabelScanResponse;
import com.chubini.pku.labelscan.dto.LabelScanStatusResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** REST controller for label scanning functionality */
@RestController
@RequestMapping("/api/v1/label-scan")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Label Scan",
    description = "APIs for scanning food labels using OCR and barcode lookup")
public class LabelScanController {

  private final LabelScanService labelScanService;

  @Operation(
      summary = "Upload images for label scanning",
      description = "Upload one or more food label images for OCR processing and safety analysis")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Images uploaded successfully and processing started",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LabelScanResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or file validation failed"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "415", description = "Unsupported file format"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
      })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<LabelScanResponse> uploadImages(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId,
      @Parameter(description = "Food label images (max 5 files)", required = true)
          @RequestParam("images")
          MultipartFile[] images,
      @Parameter(description = "Region code (ISO 3166-1 alpha-2)", example = "US")
          @RequestParam(required = false, defaultValue = "US")
          String region,
      @Parameter(description = "Barcode if visible (optional)", example = "123456789012")
          @RequestParam(required = false)
          String barcode) {

    log.info("Received label scan request for patient {} with {} images", patientId, images.length);

    // Validate request
    if (images == null || images.length == 0) {
      log.warn("No images provided in request");
      return ResponseEntity.badRequest().build();
    }

    if (images.length > 5) {
      log.warn("Too many images provided: {}", images.length);
      return ResponseEntity.badRequest().build();
    }

    try {
      LabelScanRequest request =
          new LabelScanRequest(
              patientId,
              List.of(images),
              region,
              barcode,
              null // createdBy - could be extracted from security context
              );

      LabelScanResponse response = labelScanService.processLabelScan(request);
      log.info("Label scan processing started for submission: {}", response.submissionId());

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.warn("Invalid request parameters: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error processing label scan request", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Get label scan status",
      description = "Retrieve the current status and results of a label scan submission")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LabelScanStatusResponse.class))),
        @ApiResponse(responseCode = "404", description = "Submission not found"),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - submission belongs to different patient")
      })
  @GetMapping("/{submissionId}")
  public ResponseEntity<LabelScanStatusResponse> getScanStatus(
      @Parameter(description = "Submission unique identifier", required = true) @PathVariable
          UUID submissionId,
      @Parameter(description = "Patient unique identifier (for access control)", required = true)
          @RequestParam
          UUID patientId) {

    log.debug("Retrieving status for submission {} by patient {}", submissionId, patientId);

    try {
      LabelScanStatusResponse response = labelScanService.getScanStatus(submissionId, patientId);

      if (response == null) {
        log.warn("Submission not found: {}", submissionId);
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok(response);

    } catch (SecurityException e) {
      log.warn("Access denied for submission {} by patient {}", submissionId, patientId);
      return ResponseEntity.status(403).build();
    } catch (Exception e) {
      log.error("Error retrieving scan status", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Get patient's scan history",
      description = "Retrieve paginated list of patient's label scan submissions")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "History retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScanHistoryResponse.class)))
      })
  @GetMapping("/history")
  public ResponseEntity<ScanHistoryResponse> getScanHistory(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId,
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Filter by status", example = "MATCHED")
          @RequestParam(required = false)
          LabelScanSubmission.ScanStatus status) {

    log.debug("Retrieving scan history for patient {} (page {} size {})", patientId, page, size);

    try {
      ScanHistoryResponse response = labelScanService.getScanHistory(patientId, page, size, status);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error retrieving scan history", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Retry failed scan",
      description = "Retry processing a failed label scan submission")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Retry initiated successfully"),
        @ApiResponse(responseCode = "400", description = "Submission is not in failed state"),
        @ApiResponse(responseCode = "404", description = "Submission not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
      })
  @PostMapping("/{submissionId}/retry")
  public ResponseEntity<Void> retryScan(
      @Parameter(description = "Submission unique identifier", required = true) @PathVariable
          UUID submissionId,
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId) {

    log.info("Retrying scan for submission {} by patient {}", submissionId, patientId);

    try {
      boolean retried = labelScanService.retryScan(submissionId, patientId);

      if (retried) {
        return ResponseEntity.ok().build();
      } else {
        return ResponseEntity.badRequest().build();
      }

    } catch (SecurityException e) {
      log.warn("Access denied for retry: {}", e.getMessage());
      return ResponseEntity.status(403).build();
    } catch (Exception e) {
      log.error("Error retrying scan", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Get supported regions",
      description = "Get list of supported regions for label scanning")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Regions retrieved successfully")})
  @GetMapping("/regions")
  public ResponseEntity<List<String>> getSupportedRegions() {
    try {
      List<String> regions = labelScanService.getSupportedRegions();
      return ResponseEntity.ok(regions);
    } catch (Exception e) {
      log.error("Error retrieving supported regions", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Get system status",
      description = "Get the status of label scanning services")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "System status retrieved successfully")
      })
  @GetMapping("/status")
  public ResponseEntity<SystemStatusResponse> getSystemStatus() {
    try {
      SystemStatusResponse status = labelScanService.getSystemStatus();
      return ResponseEntity.ok(status);
    } catch (Exception e) {
      log.error("Error retrieving system status", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /** Response DTOs (can be moved to separate files) */
  public record ScanHistoryResponse(
      List<LabelScanStatusResponse> submissions,
      int currentPage,
      int totalPages,
      long totalElements,
      boolean hasNext,
      boolean hasPrevious) {}

  public record SystemStatusResponse(
      boolean ocrAvailable,
      boolean barcodeLookupAvailable,
      List<String> supportedRegions,
      String systemVersion) {}
}
