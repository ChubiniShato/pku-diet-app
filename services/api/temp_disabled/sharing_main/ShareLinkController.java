package com.chubini.pku.sharing;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.chubini.pku.sharing.dto.ShareLinkRequest;
import com.chubini.pku.sharing.dto.ShareLinkResponse;

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

/** REST controller for share link management */
@RestController
@RequestMapping("/api/v1/share-links")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Share Link Management",
    description = "APIs for managing secure data sharing links with doctors")
public class ShareLinkController {

  private final ShareLinkService shareLinkService;

  @Operation(
      summary = "Create share link",
      description = "Create a new share link for secure doctor access to patient data")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Share link created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ShareLinkResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient consent"),
        @ApiResponse(responseCode = "404", description = "Patient not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<ShareLinkResponse> createShareLink(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId,
      @Parameter(description = "Share link creation request", required = true) @Valid @RequestBody
          ShareLinkRequest request) {

    log.info("Creating share link for patient {} with scopes: {}", patientId, request.scopes());

    try {
      // Map request scopes to service scopes
      Set<ShareLink.ShareScope> scopes =
          request.scopes().stream().map(this::mapRequestScope).collect(Collectors.toSet());

      ShareLink shareLink =
          shareLinkService.createShareLink(
              null, // TODO: Get patient from repository
              scopes,
              request.doctorEmail(),
              request.doctorName(),
              request.ttlHours(),
              request.oneTimeUse(),
              request.deviceBound(),
              request.createdBy());

      ShareLinkResponse response = mapToResponse(shareLink);
      return ResponseEntity.ok(response);

    } catch (ShareLinkService.InsufficientConsentException e) {
      log.warn("Insufficient consent for share link creation: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error creating share link", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @Operation(
      summary = "Revoke share link",
      description = "Revoke an existing share link to prevent further access")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Share link revoked successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ShareLinkResponse.class))),
        @ApiResponse(responseCode = "404", description = "Share link not found"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or share link already revoked")
      })
  @PostMapping("/{shareLinkId}/revoke")
  public ResponseEntity<ShareLinkResponse> revokeShareLink(
      @Parameter(description = "Share link unique identifier", required = true) @PathVariable
          UUID shareLinkId,
      @Parameter(description = "Reason for revocation") @RequestParam(required = false)
          String reason,
      @Parameter(description = "User/device performing revocation") @RequestParam(required = false)
          String performedBy) {

    log.info("Revoking share link {} with reason: {}", shareLinkId, reason);

    try {
      ShareLink shareLink = shareLinkService.revokeShareLink(shareLinkId, performedBy);
      ShareLinkResponse response = mapToResponse(shareLink);
      return ResponseEntity.ok(response);

    } catch (ShareLinkService.ShareLinkNotFoundException e) {
      log.warn("Share link not found: {}", shareLinkId);
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      log.warn("Invalid share link state: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(
      summary = "Get patient share links",
      description = "Retrieve all share links for a specific patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Share links retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ShareLinkResponse.class)))
      })
  @GetMapping
  public ResponseEntity<List<ShareLinkResponse>> getPatientShareLinks(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId) {

    log.debug("Retrieving share links for patient: {}", patientId);

    try {
      List<ShareLink> shareLinks =
          shareLinkService.getPatientShareLinks(null); // TODO: Get patient from repository
      List<ShareLinkResponse> responses =
          shareLinks.stream().map(this::mapToResponse).collect(Collectors.toList());

      return ResponseEntity.ok(responses);

    } catch (Exception e) {
      log.error("Error retrieving patient share links", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(
      summary = "Get active patient share links",
      description = "Retrieve all currently active share links for a specific patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active share links retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ShareLinkResponse.class)))
      })
  @GetMapping("/active")
  public ResponseEntity<List<ShareLinkResponse>> getActivePatientShareLinks(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId) {

    log.debug("Retrieving active share links for patient: {}", patientId);

    try {
      List<ShareLink> shareLinks =
          shareLinkService.getActivePatientShareLinks(null); // TODO: Get patient from repository
      List<ShareLinkResponse> responses =
          shareLinks.stream().map(this::mapToResponse).collect(Collectors.toList());

      return ResponseEntity.ok(responses);

    } catch (Exception e) {
      log.error("Error retrieving active patient share links", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(
      summary = "Get share link details",
      description = "Get detailed information about a specific share link")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Share link details retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ShareLinkResponse.class))),
        @ApiResponse(responseCode = "404", description = "Share link not found")
      })
  @GetMapping("/{shareLinkId}")
  public ResponseEntity<ShareLinkResponse> getShareLink(
      @Parameter(description = "Share link unique identifier", required = true) @PathVariable
          UUID shareLinkId) {

    log.debug("Retrieving share link details: {}", shareLinkId);

    try {
      // This would need a method to get share link by ID
      // For now, return not found
      return ResponseEntity.notFound().build();

    } catch (Exception e) {
      log.error("Error retrieving share link details", e);
      return ResponseEntity.badRequest().build();
    }
  }

  // Mapping methods
  private ShareLink.ShareScope mapRequestScope(ShareLinkRequest.ShareScope requestScope) {
    return switch (requestScope) {
      case CRITICAL_FACTS -> ShareLink.ShareScope.CRITICAL_FACTS;
      case DAY -> ShareLink.ShareScope.DAY;
      case WEEK -> ShareLink.ShareScope.WEEK;
      case RANGE -> ShareLink.ShareScope.RANGE;
      case NUTRITION_SUMMARY -> ShareLink.ShareScope.NUTRITION_SUMMARY;
    };
  }

  private ShareLinkResponse mapToResponse(ShareLink shareLink) {
    return new ShareLinkResponse(
        shareLink.getId(),
        shareLink.getPatient().getId(),
        maskToken(shareLink.getToken()),
        shareLink.getDoctorEmail(),
        shareLink.getDoctorName(),
        shareLink.getScopes().stream().map(Enum::toString).collect(Collectors.toSet()),
        shareLink.getOneTimeUse(),
        shareLink.getDeviceBound(),
        shareLink.getTtlHours(),
        shareLink.getStatus().toString(),
        shareLink.getExpiresAt(),
        shareLink.isUsable(),
        shareLink.getUsageCount(),
        shareLink.getFirstUsedAt(),
        shareLink.getLastUsedAt(),
        shareLink.getRevokedAt(),
        shareLink.getNotes(),
        shareLink.getCreatedBy(),
        shareLink.getRevokedBy(),
        shareLink.getCreatedAt(),
        shareLink.getUpdatedAt());
  }

  private String maskToken(String token) {
    if (token == null || token.length() < 10) {
      return token;
    }
    return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
  }
}
