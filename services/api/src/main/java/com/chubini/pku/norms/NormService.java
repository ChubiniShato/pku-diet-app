package com.chubini.pku.norms;

import com.chubini.pku.norms.dto.CreateNormPrescriptionRequest;
import com.chubini.pku.norms.dto.NormPrescriptionDto;
import com.chubini.pku.norms.mapper.NormPrescriptionMapper;
import com.chubini.pku.patients.PatientService;
import com.chubini.pku.patients.PatientProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NormService {

    private final NormPrescriptionRepository normRepository;
    private final NormPrescriptionMapper normMapper;
    private final PatientService patientService;

    /**
     * Get all norm prescriptions for a patient
     */
    public List<NormPrescriptionDto> getNormsByPatient(UUID patientId) {
        log.debug("Getting norms for patient: {}", patientId);
        List<NormPrescription> norms = normRepository.findByPatientIdOrderByPrescribedDateDesc(patientId);
        return normMapper.toDto(norms);
    }

    /**
     * Get active norm prescriptions for a patient
     */
    public List<NormPrescriptionDto> getActiveNormsByPatient(UUID patientId) {
        log.debug("Getting active norms for patient: {}", patientId);
        List<NormPrescription> activeNorms = normRepository.findByPatientIdAndIsActiveTrue(patientId);
        return normMapper.toDto(activeNorms);
    }

    /**
     * Get the current active norm prescription for a patient
     */
    public Optional<NormPrescriptionDto> getCurrentNormForPatient(UUID patientId) {
        log.debug("Getting current norm for patient: {}", patientId);
        Optional<NormPrescription> currentNorm = normRepository.findTopByPatientIdAndIsActiveTrueOrderByPrescribedDateDesc(patientId);
        return currentNorm.map(normMapper::toDto);
    }

    /**
     * Get norm prescription by ID
     */
    public NormPrescriptionDto getNormById(UUID normId) {
        log.debug("Getting norm by ID: {}", normId);
        NormPrescription norm = normRepository.findById(normId)
                .orElseThrow(() -> new NormNotFoundException("Norm prescription not found with ID: " + normId));
        return normMapper.toDto(norm);
    }

    /**
     * Create a new norm prescription
     */
    @Transactional
    public NormPrescriptionDto createNorm(CreateNormPrescriptionRequest request) {
        log.info("Creating new norm prescription for patient: {}", request.patientId());
        
        // Validate patient exists
        PatientProfile patient = patientService.getPatientEntity(request.patientId());
        
        // Convert request to entity
        NormPrescription norm = normMapper.toEntity(request);
        norm.setPatient(patient);
        
        // If this is set as active, deactivate other active norms for this patient
        if (norm.getIsActive()) {
            deactivateOtherNormsForPatient(request.patientId());
        }
        
        // Save the norm
        NormPrescription savedNorm = normRepository.save(norm);
        
        log.info("Created norm prescription with ID: {}", savedNorm.getId());
        return normMapper.toDto(savedNorm);
    }

    /**
     * Update an existing norm prescription
     */
    @Transactional
    public NormPrescriptionDto updateNorm(UUID normId, CreateNormPrescriptionRequest request) {
        log.info("Updating norm prescription: {}", normId);
        
        // Find existing norm
        NormPrescription existingNorm = normRepository.findById(normId)
                .orElseThrow(() -> new NormNotFoundException("Norm prescription not found with ID: " + normId));
        
        // Update fields
        existingNorm.setPheLimitMgPerDay(request.dailyPheMgLimit());
        existingNorm.setProteinLimitGPerDay(request.dailyProteinGLimit());
        existingNorm.setKcalMinPerDay(request.dailyKcalMin());
        existingNorm.setFatLimitGPerDay(request.dailyFatGMax());
        existingNorm.setPrescribedDate(request.effectiveFrom());
        existingNorm.setNotes(request.clinicalNotes());
        
        // Save the updated norm
        NormPrescription updatedNorm = normRepository.save(existingNorm);
        
        log.info("Updated norm prescription: {}", normId);
        return normMapper.toDto(updatedNorm);
    }

    /**
     * Activate a norm prescription (and deactivate others for the same patient)
     */
    @Transactional
    public NormPrescriptionDto activateNorm(UUID normId) {
        log.info("Activating norm prescription: {}", normId);
        
        NormPrescription norm = normRepository.findById(normId)
                .orElseThrow(() -> new NormNotFoundException("Norm prescription not found with ID: " + normId));
        
        // Deactivate other norms for this patient
        deactivateOtherNormsForPatient(norm.getPatient().getId());
        
        // Activate this norm
        norm.setIsActive(true);
        NormPrescription activatedNorm = normRepository.save(norm);
        
        log.info("Activated norm prescription: {}", normId);
        return normMapper.toDto(activatedNorm);
    }

    /**
     * Deactivate a norm prescription
     */
    @Transactional
    public NormPrescriptionDto deactivateNorm(UUID normId) {
        log.info("Deactivating norm prescription: {}", normId);
        
        NormPrescription norm = normRepository.findById(normId)
                .orElseThrow(() -> new NormNotFoundException("Norm prescription not found with ID: " + normId));
        
        norm.setIsActive(false);
        NormPrescription deactivatedNorm = normRepository.save(norm);
        
        log.info("Deactivated norm prescription: {}", normId);
        return normMapper.toDto(deactivatedNorm);
    }

    /**
     * Delete a norm prescription
     */
    @Transactional
    public void deleteNorm(UUID normId) {
        log.info("Deleting norm prescription: {}", normId);
        
        if (!normRepository.existsById(normId)) {
            throw new NormNotFoundException("Norm prescription not found with ID: " + normId);
        }
        
        normRepository.deleteById(normId);
        log.info("Deleted norm prescription: {}", normId);
    }

    /**
     * Get norm entity by ID (for internal use by other services)
     */
    public NormPrescription getNormEntity(UUID normId) {
        return normRepository.findById(normId)
                .orElseThrow(() -> new NormNotFoundException("Norm prescription not found with ID: " + normId));
    }

    /**
     * Helper method to deactivate all other active norms for a patient
     */
    private void deactivateOtherNormsForPatient(UUID patientId) {
        List<NormPrescription> activeNorms = normRepository.findByPatientIdAndIsActiveTrue(patientId);
        for (NormPrescription norm : activeNorms) {
            norm.setIsActive(false);
        }
        if (!activeNorms.isEmpty()) {
            normRepository.saveAll(activeNorms);
            log.debug("Deactivated {} existing active norms for patient: {}", activeNorms.size(), patientId);
        }
    }
}
