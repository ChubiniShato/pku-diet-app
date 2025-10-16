package com.chubini.pku.patients;

import java.util.List;
import java.util.UUID;

import com.chubini.pku.patients.dto.CreatePatientProfileRequest;
import com.chubini.pku.patients.dto.PatientProfileDto;
import com.chubini.pku.patients.dto.UpdatePatientProfileRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patient Management", description = "APIs for managing patient profiles")
public class PatientController {

  private final PatientService patientService;

  @Operation(
      summary = "Get all patient profiles",
      description = "Retrieve all patient profiles with optional pagination")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved patient profiles",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PatientProfileDto.class)))
      })
  @GetMapping
  public ResponseEntity<?> getAllPatients(
      @Parameter(description = "Enable pagination", example = "true")
          @RequestParam(defaultValue = "false")
          boolean paginated,
      Pageable pageable) {

    log.info("Getting all patients, paginated: {}", paginated);

    if (paginated) {
      Page<PatientProfileDto> patients = patientService.getAllPatients(pageable);
      return ResponseEntity.ok(patients);
    } else {
      List<PatientProfileDto> patients = patientService.getAllPatients();
      return ResponseEntity.ok(patients);
    }
  }

  @Operation(
      summary = "Get patient profile by ID",
      description = "Retrieve a specific patient profile by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved patient profile",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PatientProfileDto.class))),
        @ApiResponse(responseCode = "404", description = "Patient not found")
      })
  @GetMapping("/{patientId}")
  public ResponseEntity<PatientProfileDto> getPatientById(
      @Parameter(description = "Patient unique identifier", required = true) @PathVariable
          UUID patientId) {

    log.info("Getting patient by ID: {}", patientId);
    PatientProfileDto patient = patientService.getPatientById(patientId);
    return ResponseEntity.ok(patient);
  }

  @Operation(
      summary = "Create new patient profile",
      description = "Create a new patient profile with the provided information")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Patient profile created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PatientProfileDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
      })
  @PostMapping
  public ResponseEntity<PatientProfileDto> createPatient(
      @Parameter(description = "Patient profile creation request", required = true)
          @Valid
          @RequestBody
          CreatePatientProfileRequest request) {

    log.info("Creating new patient profile for: {}", request.name());
    PatientProfileDto createdPatient = patientService.createPatient(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdPatient);
  }

  @Operation(
      summary = "Update patient profile",
      description = "Update an existing patient profile with new information")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Patient profile updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PatientProfileDto.class))),
        @ApiResponse(responseCode = "404", description = "Patient not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
      })
  @PutMapping("/{patientId}")
  public ResponseEntity<PatientProfileDto> updatePatient(
      @Parameter(description = "Patient unique identifier", required = true) @PathVariable
          UUID patientId,
      @Parameter(description = "Patient profile update request", required = true)
          @Valid
          @RequestBody
          UpdatePatientProfileRequest request) {

    log.info("Updating patient profile: {}", patientId);
    PatientProfileDto updatedPatient = patientService.updatePatient(patientId, request);
    return ResponseEntity.ok(updatedPatient);
  }

  @Operation(
      summary = "Delete patient profile",
      description = "Delete a patient profile and all associated data")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Patient profile deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
      })
  @DeleteMapping("/{patientId}")
  public ResponseEntity<Void> deletePatient(
      @Parameter(description = "Patient unique identifier", required = true) @PathVariable
          UUID patientId) {

    log.info("Deleting patient profile: {}", patientId);
    patientService.deletePatient(patientId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Check if patient exists",
      description = "Check if a patient profile exists by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Patient existence check result"),
        @ApiResponse(responseCode = "404", description = "Patient not found")
      })
  @GetMapping("/{patientId}/exists")
  public ResponseEntity<Boolean> patientExists(
      @Parameter(description = "Patient unique identifier", required = true) @PathVariable
          UUID patientId) {

    boolean exists = patientService.patientExists(patientId);
    return ResponseEntity.ok(exists);
  }
}
