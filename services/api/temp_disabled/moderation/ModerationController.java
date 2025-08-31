package com.chubini.pku.moderation;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.chubini.pku.moderation.dto.ModerationRequest;
import com.chubini.pku.moderation.dto.ModerationResponse;
import com.chubini.pku.moderation.dto.ReviewRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** REST controller for moderation workflow */
@RestController
@RequestMapping("/api/v1/moderation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Moderation Workflow", description = "APIs for global catalog moderation and review")
public class ModerationController {

  private final ModerationSubmissionService moderationService;

  // Patient endpoints

  @Operation(
      summary = "Submit item to global catalog",
      description =
          "Submit a custom product, dish, or label scan for inclusion in the global catalog")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Submission created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModerationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient consent"),
        @ApiResponse(responseCode = "404", description = "Referenced item not found")
      })
  @PostMapping("/submissions")
  public ResponseEntity<ModerationResponse> createSubmission(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId,
      @Parameter(description = "Moderation submission request", required = true) @Valid @RequestBody
          ModerationRequest request) {

    log.info(
        "Creating moderation submission for patient {}: {} {}",
        patientId,
        request.targetType(),
        request.payloadRef());

    try {
      ModerationSubmission.TargetType targetType =
          ModerationSubmission.TargetType.valueOf(request.targetType());

      ModerationSubmission submission =
          moderationService.createSubmission(
              patientId,
              targetType,
              request.payloadRef(),
              request.title(),
              request.description(),
              request.createdBy());

      ModerationResponse response = mapToResponse(submission);
      return ResponseEntity.ok(response);

    } catch (ModerationSubmissionService.InsufficientConsentException e) {
      log.warn("Insufficient consent for moderation submission: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (IllegalArgumentException e) {
      log.warn("Invalid submission request: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error creating moderation submission", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Get patient's submissions",
      description = "Retrieve all moderation submissions for a specific patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Submissions retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModerationResponse.class)))
      })
  @GetMapping("/submissions")
  public ResponseEntity<List<ModerationResponse>> getPatientSubmissions(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId) {

    log.debug("Retrieving submissions for patient: {}", patientId);

    try {
      List<ModerationSubmission> submissions = moderationService.getPatientSubmissions(patientId);
      List<ModerationResponse> responses =
          submissions.stream().map(this::mapToResponse).collect(Collectors.toList());

      return ResponseEntity.ok(responses);

    } catch (Exception e) {
      log.error("Error retrieving patient submissions", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  // Reviewer/Admin endpoints

  @Operation(
      summary = "Get moderation queue",
      description = "Retrieve submissions waiting for moderation review")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Moderation queue retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModerationResponse.class)))
      })
  @GetMapping("/queue")
  public ResponseEntity<ModerationQueueResponse> getModerationQueue(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "Filter by status", example = "PENDING")
          @RequestParam(required = false)
          String status,
      @Parameter(description = "Filter by target type", example = "CUSTOM_PRODUCT")
          @RequestParam(required = false)
          String targetType) {

    log.debug("Retrieving moderation queue (page {} size {})", page, size);

    try {
      List<ModerationSubmission> submissions = moderationService.getModerationQueue(page, size);

      // Apply filters if provided
      if (status != null) {
        ModerationSubmission.ModerationStatus statusFilter =
            ModerationSubmission.ModerationStatus.valueOf(status);
        submissions =
            submissions.stream()
                .filter(s -> s.getStatus() == statusFilter)
                .collect(Collectors.toList());
      }

      if (targetType != null) {
        ModerationSubmission.TargetType typeFilter =
            ModerationSubmission.TargetType.valueOf(targetType);
        submissions =
            submissions.stream()
                .filter(s -> s.getTargetType() == typeFilter)
                .collect(Collectors.toList());
      }

      List<ModerationResponse> responses =
          submissions.stream().map(this::mapToResponse).collect(Collectors.toList());

      ModerationQueueResponse queueResponse =
          new ModerationQueueResponse(
              responses,
              page,
              size,
              responses.size(), // TODO: Get actual total count
              page > 0,
              responses.size() >= size);

      return ResponseEntity.ok(queueResponse);

    } catch (Exception e) {
      log.error("Error retrieving moderation queue", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Mark submission under review",
      description = "Mark a submission as currently being reviewed")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Submission marked under review successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModerationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Submission not found"),
        @ApiResponse(responseCode = "400", description = "Submission not in reviewable state")
      })
  @PostMapping("/{submissionId}/review")
  public ResponseEntity<ModerationResponse> markUnderReview(
      @Parameter(description = "Submission unique identifier", required = true) @PathVariable
          UUID submissionId,
      @Parameter(description = "Reviewer identifier", required = true) @RequestParam
          String reviewerId) {

    log.info("Marking submission {} under review by {}", submissionId, reviewerId);

    try {
      ModerationSubmission submission = moderationService.markUnderReview(submissionId, reviewerId);
      ModerationResponse response = mapToResponse(submission);
      return ResponseEntity.ok(response);

    } catch (ModerationSubmissionService.ModerationSubmissionNotFoundException e) {
      log.warn("Submission not found: {}", submissionId);
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      log.warn("Invalid submission state: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(
      summary = "Approve submission",
      description = "Approve a submission and merge it into the global catalog")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Submission approved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModerationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Submission not found"),
        @ApiResponse(responseCode = "400", description = "Submission not in reviewable state")
      })
  @PostMapping("/{submissionId}/approve")
  public ResponseEntity<ModerationResponse> approveSubmission(
      @Parameter(description = "Submission unique identifier", required = true) @PathVariable
          UUID submissionId,
      @Parameter(description = "Review request with approval details", required = true)
          @Valid
          @RequestBody
          ReviewRequest request) {

    log.info("Approving submission {} by reviewer {}", submissionId, request.reviewerId());

    try {
      ModerationSubmission submission =
          moderationService.approveSubmission(
              submissionId, request.reviewerId(), request.reviewNotes(), request.mergeData());

      ModerationResponse response = mapToResponse(submission);
      return ResponseEntity.ok(response);

    } catch (ModerationSubmissionService.ModerationSubmissionNotFoundException e) {
      log.warn("Submission not found: {}", submissionId);
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      log.warn("Invalid submission state: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error approving submission", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(summary = "Reject submission", description = "Reject a submission with review notes")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Submission rejected successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ModerationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Submission not found"),
        @ApiResponse(responseCode = "400", description = "Submission not in reviewable state")
      })
  @PostMapping("/{submissionId}/reject")
  public ResponseEntity<ModerationResponse> rejectSubmission(
      @Parameter(description = "Submission unique identifier", required = true) @PathVariable
          UUID submissionId,
      @Parameter(description = "Review request with rejection details", required = true)
          @Valid
          @RequestBody
          ReviewRequest request) {

    log.info("Rejecting submission {} by reviewer {}", submissionId, request.reviewerId());

    try {
      ModerationSubmission submission =
          moderationService.rejectSubmission(
              submissionId, request.reviewerId(), request.reviewNotes());

      ModerationResponse response = mapToResponse(submission);
      return ResponseEntity.ok(response);

    } catch (ModerationSubmissionService.ModerationSubmissionNotFoundException e) {
      log.warn("Submission not found: {}", submissionId);
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      log.warn("Invalid submission state: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error rejecting submission", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Get moderation statistics",
      description = "Get statistics for moderation dashboard")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
      })
  @GetMapping("/statistics")
  public ResponseEntity<ModerationStatisticsResponse> getStatistics() {
    try {
      ModerationSubmissionService.ModerationStatistics stats = moderationService.getStatistics();
      ModerationStatisticsResponse response =
          new ModerationStatisticsResponse(
              stats.totalSubmissions(),
              stats.pendingSubmissions(),
              stats.approvedSubmissions(),
              stats.rejectedSubmissions(),
              LocalDateTime.now());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error retrieving moderation statistics", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /** Map ModerationSubmission to ModerationResponse */
  private ModerationResponse mapToResponse(ModerationSubmission submission) {
    return new ModerationResponse(
        submission.getId(),
        submission.getPatient() != null ? submission.getPatient().getId() : null,
        submission.getTargetType().toString(),
        submission.getPayloadRef(),
        submission.getSubmissionTitle(),
        submission.getSubmissionDescription(),
        submission.getStatus().toString(),
        submission.getConflicts(),
        submission.getReviewNotes(),
        submission.getReviewerId(),
        submission.getReviewedAt(),
        submission.getApprovedProductId(),
        submission.getCreatedBy(),
        submission.getCreatedAt(),
        submission.getUpdatedAt(),
        submission.getExpiresAt(),
        submission.isExpired());
  }

  /** Response DTOs */
  public record ModerationQueueResponse(
      List<ModerationResponse> submissions,
      int currentPage,
      int pageSize,
      long totalElements,
      boolean hasPrevious,
      boolean hasNext) {}

  public record ModerationStatisticsResponse(
      int totalSubmissions,
      int pendingSubmissions,
      int approvedSubmissions,
      int rejectedSubmissions,
      java.time.LocalDateTime generatedAt) {}
}
