package com.chubini.pku.norms;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chubini.pku.norms.dto.CreateNormPrescriptionRequest;
import com.chubini.pku.norms.dto.NormPrescriptionDto;

import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api/v1/norms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Norm Prescriptions", description = "APIs for managing nutritional norm prescriptions")
public class NormController {

  private final NormService normService;

  @Operation(
      summary = "Get all norms for a patient",
      description = "Retrieve all norm prescriptions for a specific patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved norm prescriptions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NormPrescriptionDto.class)))
      })
  @GetMapping("/patient/{patientId}")
  public ResponseEntity<List<NormPrescriptionDto>> getNormsByPatient(
      @Parameter(description = "Patient unique identifier", required = true) @PathVariable
          UUID patientId) {

    log.info("Getting norms for patient: {}", patientId);
    List<NormPrescriptionDto> norms = normService.getNormsByPatient(patientId);
    return ResponseEntity.ok(norms);
  }

  @Operation(
      summary = "Get active norms for a patient",
      description = "Retrieve all active norm prescriptions for a specific patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved active norm prescriptions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NormPrescriptionDto.class)))
      })
  @GetMapping("/patient/{patientId}/active")
  public ResponseEntity<List<NormPrescriptionDto>> getActiveNormsByPatient(
      @Parameter(description = "Patient unique identifier", required = true) @PathVariable
          UUID patientId) {

    log.info("Getting active norms for patient: {}", patientId);
    List<NormPrescriptionDto> activeNorms = normService.getActiveNormsByPatient(patientId);
    return ResponseEntity.ok(activeNorms);
  }

  @Operation(
      summary = "Get current norm for a patient",
      description = "Retrieve the current active norm prescription for a specific patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved current norm prescription",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NormPrescriptionDto.class))),
        @ApiResponse(responseCode = "404", description = "No active norm found for patient")
      })
  @GetMapping("/patient/{patientId}/current")
  public ResponseEntity<NormPrescriptionDto> getCurrentNormForPatient(
      @Parameter(description = "Patient unique identifier", required = true) @PathVariable
          UUID patientId) {

    log.info("Getting current norm for patient: {}", patientId);
    Optional<NormPrescriptionDto> currentNorm = normService.getCurrentNormForPatient(patientId);

    return currentNorm.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Get norm prescription by ID",
      description = "Retrieve a specific norm prescription by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved norm prescription",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NormPrescriptionDto.class))),
        @ApiResponse(responseCode = "404", description = "Norm prescription not found")
      })
  @GetMapping("/{normId}")
  public ResponseEntity<NormPrescriptionDto> getNormById(
      @Parameter(description = "Norm prescription unique identifier", required = true) @PathVariable
          UUID normId) {

    log.info("Getting norm by ID: {}", normId);
    NormPrescriptionDto norm = normService.getNormById(normId);
    return ResponseEntity.ok(norm);
  }

  @Operation(
      summary = "Create new norm prescription",
      description = "Create a new norm prescription for a patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Norm prescription created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NormPrescriptionDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
      })
  @PostMapping
  public ResponseEntity<NormPrescriptionDto> createNorm(
      @Parameter(description = "Norm prescription creation request", required = true)
          @Valid
          @RequestBody
          CreateNormPrescriptionRequest request) {

    log.info("Creating new norm prescription for patient: {}", request.patientId());
    NormPrescriptionDto createdNorm = normService.createNorm(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdNorm);
  }

  @Operation(
      summary = "Update norm prescription",
      description = "Update an existing norm prescription")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Norm prescription updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NormPrescriptionDto.class))),
        @ApiResponse(responseCode = "404", description = "Norm prescription not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
      })
  @PutMapping("/{normId}")
  public ResponseEntity<NormPrescriptionDto> updateNorm(
      @Parameter(description = "Norm prescription unique identifier", required = true) @PathVariable
          UUID normId,
      @Parameter(description = "Norm prescription update request", required = true)
          @Valid
          @RequestBody
          CreateNormPrescriptionRequest request) {

    log.info("Updating norm prescription: {}", normId);
    NormPrescriptionDto updatedNorm = normService.updateNorm(normId, request);
    return ResponseEntity.ok(updatedNorm);
  }

  @Operation(
      summary = "Activate norm prescription",
      description = "Activate a norm prescription and deactivate others for the same patient")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Norm prescription activated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NormPrescriptionDto.class))),
        @ApiResponse(responseCode = "404", description = "Norm prescription not found")
      })
  @PatchMapping("/{normId}/activate")
  public ResponseEntity<NormPrescriptionDto> activateNorm(
      @Parameter(description = "Norm prescription unique identifier", required = true) @PathVariable
          UUID normId) {

    log.info("Activating norm prescription: {}", normId);
    NormPrescriptionDto activatedNorm = normService.activateNorm(normId);
    return ResponseEntity.ok(activatedNorm);
  }

  @Operation(
      summary = "Deactivate norm prescription",
      description = "Deactivate a norm prescription")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Norm prescription deactivated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NormPrescriptionDto.class))),
        @ApiResponse(responseCode = "404", description = "Norm prescription not found")
      })
  @PatchMapping("/{normId}/deactivate")
  public ResponseEntity<NormPrescriptionDto> deactivateNorm(
      @Parameter(description = "Norm prescription unique identifier", required = true) @PathVariable
          UUID normId) {

    log.info("Deactivating norm prescription: {}", normId);
    NormPrescriptionDto deactivatedNorm = normService.deactivateNorm(normId);
    return ResponseEntity.ok(deactivatedNorm);
  }

  @Operation(summary = "Delete norm prescription", description = "Delete a norm prescription")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Norm prescription deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Norm prescription not found")
      })
  @DeleteMapping("/{normId}")
  public ResponseEntity<Void> deleteNorm(
      @Parameter(description = "Norm prescription unique identifier", required = true) @PathVariable
          UUID normId) {

    log.info("Deleting norm prescription: {}", normId);
    normService.deleteNorm(normId);
    return ResponseEntity.noContent().build();
  }
}
