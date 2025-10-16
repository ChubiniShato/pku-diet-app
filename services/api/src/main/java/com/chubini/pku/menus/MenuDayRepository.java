package com.chubini.pku.menus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chubini.pku.patients.PatientProfile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuDayRepository extends JpaRepository<MenuDay, UUID> {

  Optional<MenuDay> findByPatientIdAndDate(UUID patientId, LocalDate date);

  Page<MenuDay> findByPatientIdOrderByDateDesc(UUID patientId, Pageable pageable);

  List<MenuDay> findByPatientIdAndStatus(UUID patientId, MenuWeek.MenuStatus status);

  @Query(
      "SELECT md FROM MenuDay md WHERE md.patient.id = :patientId AND md.date BETWEEN :startDate AND :endDate ORDER BY md.date")
  List<MenuDay> findByPatientIdAndDateRange(
      @Param("patientId") UUID patientId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  List<MenuDay> findByMenuWeekIdOrderByDate(UUID menuWeekId);

  @Query(
      "SELECT md FROM MenuDay md WHERE md.patient.id = :patientId AND md.date >= :fromDate ORDER BY md.date")
  List<MenuDay> findUpcomingMenuDays(
      @Param("patientId") UUID patientId, @Param("fromDate") LocalDate fromDate);

  @Query(
      "SELECT md FROM MenuDay md WHERE md.patient.id = :patientId AND md.totalDayPheMg > :pheLimit")
  List<MenuDay> findByPatientIdAndPheExceeded(
      @Param("patientId") UUID patientId, @Param("pheLimit") Double pheLimit);

  long countByPatientIdAndStatus(UUID patientId, MenuWeek.MenuStatus status);

  // Method for variety engine
  List<MenuDay> findByPatientAndDateBetweenOrderByDateDesc(
      PatientProfile patient, LocalDate startDate, LocalDate endDate);
}
