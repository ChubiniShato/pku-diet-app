package com.chubini.pku.patients;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, UUID> {

  /** Find patients by name containing (case-insensitive) */
  @Query("SELECT p FROM PatientProfile p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
  Page<PatientProfile> findByNameContainingIgnoreCase(
      @Param("name") String name, Pageable pageable);

  /** Find patients by birth date range */
  @Query("SELECT p FROM PatientProfile p WHERE p.birthDate BETWEEN :startDate AND :endDate")
  List<PatientProfile> findByBirthDateBetween(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  /** Find patients by activity level */
  List<PatientProfile> findByActivityLevel(PatientProfile.ActivityLevel activityLevel);

  /** Find patients by region */
  List<PatientProfile> findByRegion(String region);

  /** Find patients with active status (if you have an isActive field) */
  @Query("SELECT p FROM PatientProfile p WHERE p.createdAt IS NOT NULL ORDER BY p.createdAt DESC")
  List<PatientProfile> findAllOrderByCreatedAtDesc();

  /** Search patients by multiple criteria */
  @Query(
      "SELECT p FROM PatientProfile p WHERE "
          + "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
          + "(:region IS NULL OR p.region = :region)")
  Page<PatientProfile> findByMultipleCriteria(
      @Param("name") String name, @Param("region") String region, Pageable pageable);
}
