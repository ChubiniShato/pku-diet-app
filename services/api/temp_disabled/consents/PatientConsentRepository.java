package com.chubini.pku.consents;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientConsentRepository extends JpaRepository<PatientConsent, UUID> {

  /** Find all consents for a specific patient */
  List<PatientConsent> findByPatientOrderByCreatedAtDesc(PatientProfile patient);

  /** Find active consents for a specific patient and consent type */
  @Query(
      "SELECT pc FROM PatientConsent pc WHERE pc.patient = :patient "
          + "AND pc.consentType = :consentType AND pc.status = 'GRANTED' "
          + "AND (pc.expiresAt IS NULL OR pc.expiresAt > :now) "
          + "ORDER BY pc.version DESC")
  Optional<PatientConsent> findActiveConsentByPatientAndType(
      @Param("patient") PatientProfile patient,
      @Param("consentType") PatientConsent.ConsentType consentType,
      @Param("now") LocalDateTime now);

  /** Find latest consent version for a patient and type */
  @Query(
      "SELECT pc FROM PatientConsent pc WHERE pc.patient = :patient "
          + "AND pc.consentType = :consentType ORDER BY pc.version DESC LIMIT 1")
  Optional<PatientConsent> findLatestConsentByPatientAndType(
      @Param("patient") PatientProfile patient,
      @Param("consentType") PatientConsent.ConsentType consentType);

  /** Find all active consents for a patient */
  @Query(
      "SELECT pc FROM PatientConsent pc WHERE pc.patient = :patient "
          + "AND pc.status = 'GRANTED' "
          + "AND (pc.expiresAt IS NULL OR pc.expiresAt > :now) "
          + "ORDER BY pc.createdAt DESC")
  List<PatientConsent> findActiveConsentsByPatient(
      @Param("patient") PatientProfile patient, @Param("now") LocalDateTime now);

  /** Find expired consents */
  @Query(
      "SELECT pc FROM PatientConsent pc WHERE pc.status = 'GRANTED' "
          + "AND pc.expiresAt IS NOT NULL AND pc.expiresAt <= :now")
  List<PatientConsent> findExpiredConsents(@Param("now") LocalDateTime now);

  /** Count active consents by type */
  @Query(
      "SELECT COUNT(pc) FROM PatientConsent pc WHERE pc.consentType = :consentType "
          + "AND pc.status = 'GRANTED' "
          + "AND (pc.expiresAt IS NULL OR pc.expiresAt > :now)")
  long countActiveConsentsByType(
      @Param("consentType") PatientConsent.ConsentType consentType,
      @Param("now") LocalDateTime now);

  /** Find consents by status */
  List<PatientConsent> findByStatus(PatientConsent.ConsentStatus status);

  /** Find consents expiring soon (within specified hours) */
  @Query(
      "SELECT pc FROM PatientConsent pc WHERE pc.status = 'GRANTED' "
          + "AND pc.expiresAt IS NOT NULL "
          + "AND pc.expiresAt BETWEEN :now AND :futureTime "
          + "ORDER BY pc.expiresAt ASC")
  List<PatientConsent> findConsentsExpiringSoon(
      @Param("now") LocalDateTime now, @Param("futureTime") LocalDateTime futureTime);
}
