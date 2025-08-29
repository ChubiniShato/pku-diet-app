package com.chubini.pku.menus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuWeekRepository extends JpaRepository<MenuWeek, UUID> {

    Page<MenuWeek> findByPatientIdOrderByWeekStartDateDesc(UUID patientId, Pageable pageable);

    List<MenuWeek> findByPatientIdAndStatus(UUID patientId, MenuWeek.MenuStatus status);

    @Query("SELECT mw FROM MenuWeek mw WHERE mw.patient.id = :patientId AND mw.weekStartDate <= :date AND mw.weekEndDate >= :date")
    Optional<MenuWeek> findByPatientIdAndDate(@Param("patientId") UUID patientId, @Param("date") LocalDate date);

    @Query("SELECT mw FROM MenuWeek mw WHERE mw.patient.id = :patientId AND mw.weekStartDate BETWEEN :startDate AND :endDate")
    List<MenuWeek> findByPatientIdAndDateRange(@Param("patientId") UUID patientId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(mw) FROM MenuWeek mw WHERE mw.patient.id = :patientId AND mw.status = :status")
    long countByPatientIdAndStatus(@Param("patientId") UUID patientId, @Param("status") MenuWeek.MenuStatus status);

    @Query("SELECT mw FROM MenuWeek mw WHERE mw.patient.id = :patientId AND mw.generationMethod = :method")
    List<MenuWeek> findByPatientIdAndGenerationMethod(@Param("patientId") UUID patientId, 
                                                      @Param("method") MenuWeek.GenerationMethod method);

    List<MenuWeek> findByPatientIdOrderByWeekStartDateDesc(UUID patientId);
}

