package com.chubini.pku.patients;

import java.util.List;
import java.util.UUID;

import com.chubini.pku.patients.dto.CreatePatientProfileRequest;
import com.chubini.pku.patients.dto.PatientProfileDto;
import com.chubini.pku.patients.dto.UpdatePatientProfileRequest;
import com.chubini.pku.patients.mapper.PatientProfileMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientService {

  private final PatientProfileRepository patientRepository;
  private final PatientProfileMapper patientMapper;

  /** Get all patient profiles with pagination */
  public Page<PatientProfileDto> getAllPatients(Pageable pageable) {
    log.debug("Getting all patients with pagination: {}", pageable);
    return patientRepository.findAll(pageable).map(patientMapper::toDto);
  }

  /** Get all patient profiles as a simple list */
  public List<PatientProfileDto> getAllPatients() {
    log.debug("Getting all patients");
    List<PatientProfile> patients = patientRepository.findAll();
    return patientMapper.toDto(patients);
  }

  /** Get a patient profile by ID */
  public PatientProfileDto getPatientById(UUID patientId) {
    log.debug("Getting patient by ID: {}", patientId);
    PatientProfile patient =
        patientRepository
            .findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
    return patientMapper.toDto(patient);
  }

  /** Create a new patient profile */
  @Transactional
  public PatientProfileDto createPatient(CreatePatientProfileRequest request) {
    log.info("Creating new patient profile for: {}", request.name());

    // Convert request to entity
    PatientProfile patient = patientMapper.toEntity(request);

    // Save the patient
    PatientProfile savedPatient = patientRepository.save(patient);

    log.info("Created patient profile with ID: {}", savedPatient.getId());
    return patientMapper.toDto(savedPatient);
  }

  /** Update an existing patient profile */
  @Transactional
  public PatientProfileDto updatePatient(UUID patientId, UpdatePatientProfileRequest request) {
    log.info("Updating patient profile: {}", patientId);

    // Find existing patient
    PatientProfile existingPatient =
        patientRepository
            .findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

    // Update the patient with new data
    patientMapper.updateEntityFromRequest(request, existingPatient);

    // Save the updated patient
    PatientProfile updatedPatient = patientRepository.save(existingPatient);

    log.info("Updated patient profile: {}", patientId);
    return patientMapper.toDto(updatedPatient);
  }

  /** Delete a patient profile */
  @Transactional
  public void deletePatient(UUID patientId) {
    log.info("Deleting patient profile: {}", patientId);

    if (!patientRepository.existsById(patientId)) {
      throw new RuntimeException("Patient not found with ID: " + patientId);
    }

    patientRepository.deleteById(patientId);
    log.info("Deleted patient profile: {}", patientId);
  }

  /** Check if a patient exists */
  public boolean patientExists(UUID patientId) {
    return patientRepository.existsById(patientId);
  }

  /** Get patient entity by ID (for internal use by other services) */
  public PatientProfile getPatientEntity(UUID patientId) {
    return patientRepository
        .findById(patientId)
        .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
  }
}
