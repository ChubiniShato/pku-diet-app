package com.chubini.pku.consents;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.chubini.pku.consents.dto.ConsentRequest;
import com.chubini.pku.consents.dto.ConsentResponse;

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

/** REST controller for patient consent management */
@RestController
@RequestMapping("/api/v1/consents")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Consent Management",
    description = "APIs for managing patient consents for data sharing and submissions")
public class ConsentController {

  private final PatientConsentService consentService;

  @Operation(
      summary = "Grant or revoke patient consent",
      description = "Grant a new consent or revoke an existing one for a patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Consent processed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ConsentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid consent request"),
        @ApiResponse(responseCode = "404", description = "Patient not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<ConsentResponse> manageConsent(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId,
      @Parameter(description = "Consent management request", required = true) @Valid @RequestBody
          ConsentRequest request) {

    log.info(
        "Processing consent request for patient {}: {} {}",
        patientId,
        request.action(),
        request.consentType());

    try {
      PatientConsent consent;

      if (request.action() == ConsentRequest.ConsentAction.GRANT) {
        // Map request enums to service enums
        PatientConsent.ConsentType consentType = mapConsentType(request.consentType());

        consent =
            consentService.grantConsent(
                null, // TODO: Get patient from repository or service
                consentType,
                request.reason(),
                request.expiresAt(),
                request.performedBy());
      } else {
        // For revoke, we need to find the active consent first
        // This is a simplified implementation - in practice you'd need the consent ID
        throw new UnsupportedOperationException(
            "Revoke by patient ID not implemented - use consent ID");
      }

      ConsentResponse response = mapToResponse(consent);
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error processing consent request", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(
      summary = "Revoke consent by ID",
      description = "Revoke a specific consent by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Consent revoked successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ConsentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Consent not found"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or consent already revoked")
      })
  @PostMapping("/{consentId}/revoke")
  public ResponseEntity<ConsentResponse> revokeConsent(
      @Parameter(description = "Consent unique identifier", required = true) @PathVariable
          UUID consentId,
      @Parameter(description = "Reason for revocation") @RequestParam(required = false)
          String reason,
      @Parameter(description = "User/device performing revocation") @RequestParam(required = false)
          String performedBy) {

    log.info("Revoking consent {} with reason: {}", consentId, reason);

    try {
      PatientConsent consent = consentService.revokeConsent(consentId, reason, performedBy);
      ConsentResponse response = mapToResponse(consent);
      return ResponseEntity.ok(response);

    } catch (PatientConsentService.ConsentNotFoundException e) {
      log.warn("Consent not found: {}", consentId);
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      log.warn("Invalid consent state: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(
      summary = "Get patient consents",
      description = "Retrieve all consents for a specific patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Consents retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ConsentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Patient not found")
      })
  @GetMapping
  public ResponseEntity<List<ConsentResponse>> getPatientConsents(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId) {

    log.debug("Retrieving consents for patient: {}", patientId);

    try {
      List<PatientConsent> consents =
          consentService.getPatientConsents(null); // TODO: Get patient from repository
      List<ConsentResponse> responses =
          consents.stream().map(this::mapToResponse).collect(Collectors.toList());

      return ResponseEntity.ok(responses);

    } catch (Exception e) {
      log.error("Error retrieving patient consents", e);
      return ResponseEntity.notFound().build();
    }
  }

  @Operation(
      summary = "Get active patient consents",
      description = "Retrieve all currently active consents for a specific patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active consents retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ConsentResponse.class)))
      })
  @GetMapping("/active")
  public ResponseEntity<List<ConsentResponse>> getActivePatientConsents(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId) {

    log.debug("Retrieving active consents for patient: {}", patientId);

    try {
      List<PatientConsent> consents =
          consentService.getActivePatientConsents(null); // TODO: Get patient from repository
      List<ConsentResponse> responses =
          consents.stream().map(this::mapToResponse).collect(Collectors.toList());

      return ResponseEntity.ok(responses);

    } catch (Exception e) {
      log.error("Error retrieving active patient consents", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @Operation(
      summary = "Check consent status",
      description = "Check if a patient has active consent for a specific type")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Consent status checked successfully")
      })
  @GetMapping("/check")
  public ResponseEntity<ConsentStatusResponse> checkConsentStatus(
      @Parameter(description = "Patient unique identifier", required = true) @RequestParam
          UUID patientId,
      @Parameter(description = "Consent type to check", required = true) @RequestParam
          ConsentRequest.ConsentType consentType) {

    log.debug("Checking consent status for patient {} and type {}", patientId, consentType);

    try {
      PatientConsent.ConsentType serviceConsentType = mapConsentType(consentType);
      boolean hasConsent =
          consentService.hasActiveConsent(null, serviceConsentType); // TODO: Get patient

      ConsentStatusResponse response =
          new ConsentStatusResponse(hasConsent, consentType.toString());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error checking consent status", e);
      return ResponseEntity.badRequest().build();
    }
  }

  // Mapping methods
  private PatientConsent.ConsentType mapConsentType(ConsentRequest.ConsentType requestType) {
    return switch (requestType) {
      case GLOBAL_SUBMISSION_OPTIN -> PatientConsent.ConsentType.GLOBAL_SUBMISSION_OPTIN;
      case SHARE_WITH_DOCTOR -> PatientConsent.ConsentType.SHARE_WITH_DOCTOR;
      case EMERGENCY_ACCESS -> PatientConsent.ConsentType.EMERGENCY_ACCESS;
      case RESEARCH_OPTIN -> PatientConsent.ConsentType.RESEARCH_OPTIN;
    };
  }

  private ConsentResponse mapToResponse(PatientConsent consent) {
    return new ConsentResponse(
        consent.getId(),
        consent.getPatient().getId(),
        consent.getConsentType().toString(),
        consent.getStatus().toString(),
        consent.getVersion(),
        consent.getGrantedReason(),
        consent.getRevokedReason(),
        consent.getGrantedAt(),
        consent.getRevokedAt(),
        consent.getExpiresAt(),
        consent.isActive(),
        consent.isExpired(),
        consent.getGrantedBy(),
        consent.getRevokedBy(),
        consent.getCreatedAt(),
        consent.getUpdatedAt());
  }

  /** Response for consent status check */
  public record ConsentStatusResponse(boolean hasActiveConsent, String consentType) {}
}
