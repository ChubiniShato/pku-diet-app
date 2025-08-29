package com.chubini.pku.norms;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NormPrescriptionRepository extends JpaRepository<NormPrescription, UUID> {

    @Query("SELECT n FROM NormPrescription n WHERE n.patient.id = :patientId AND n.isActive = true")
    Optional<NormPrescription> findActiveByPatientId(@Param("patientId") UUID patientId);

    List<NormPrescription> findByPatientIdOrderByPrescribedDateDesc(UUID patientId);

    @Query("SELECT n FROM NormPrescription n WHERE n.patient.id = :patientId ORDER BY n.prescribedDate DESC")
    List<NormPrescription> findByPatientIdOrderByDate(@Param("patientId") UUID patientId);

    @Query("SELECT COUNT(n) FROM NormPrescription n WHERE n.patient.id = :patientId AND n.isActive = true")
    long countActiveByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT n FROM NormPrescription n WHERE n.isActive = true AND n.pheLimitMgPerDay BETWEEN :minPhe AND :maxPhe")
    List<NormPrescription> findActiveByPheLimitRange(@Param("minPhe") Double minPhe, @Param("maxPhe") Double maxPhe);

    List<NormPrescription> findByPatientIdAndIsActiveTrue(UUID patientId);

    Optional<NormPrescription> findTopByPatientIdAndIsActiveTrueOrderByPrescribedDateDesc(UUID patientId);
}

