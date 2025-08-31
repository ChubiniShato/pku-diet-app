package com.chubini.pku.validation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.chubini.pku.menus.MenuDay;
import com.chubini.pku.patients.PatientProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CriticalFactRepository extends JpaRepository<CriticalFact, UUID> {

  /** Find all unresolved critical facts for a patient */
  List<CriticalFact> findByPatientAndResolvedFalseOrderByCreatedAtDesc(PatientProfile patient);

  /** Find all critical facts for a specific menu day */
  List<CriticalFact> findByMenuDayOrderByCreatedAtDesc(MenuDay menuDay);

  /** Find critical facts by patient and date range */
  @Query(
      "SELECT cf FROM CriticalFact cf WHERE cf.patient = :patient "
          + "AND cf.createdAt BETWEEN :startDate AND :endDate "
          + "ORDER BY cf.createdAt DESC")
  List<CriticalFact> findByPatientAndDateRange(
      @Param("patient") PatientProfile patient,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /** Find critical facts by breach type for a patient */
  List<CriticalFact> findByPatientAndBreachTypeOrderByCreatedAtDesc(
      PatientProfile patient, CriticalFact.BreachType breachType);

  /** Find critical facts by severity for a patient */
  List<CriticalFact> findByPatientAndSeverityOrderByCreatedAtDesc(
      PatientProfile patient, CriticalFact.Severity severity);

  /** Count unresolved critical facts for a patient */
  long countByPatientAndResolvedFalse(PatientProfile patient);

  /** Count critical facts by severity for a patient */
  long countByPatientAndSeverity(PatientProfile patient, CriticalFact.Severity severity);

  /** Find recent critical facts (last 30 days) for a patient */
  @Query(
      "SELECT cf FROM CriticalFact cf WHERE cf.patient = :patient "
          + "AND cf.createdAt >= :thirtyDaysAgo "
          + "ORDER BY cf.createdAt DESC")
  List<CriticalFact> findRecentByPatient(
      @Param("patient") PatientProfile patient,
      @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
}
