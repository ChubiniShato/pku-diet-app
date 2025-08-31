package com.chubini.pku.sharing;

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
public interface ShareLinkRepository extends JpaRepository<ShareLink, UUID> {

  /** Find share link by token */
  Optional<ShareLink> findByToken(String token);

  /** Find all share links for a patient */
  List<ShareLink> findByPatientOrderByCreatedAtDesc(PatientProfile patient);

  /** Find active share links for a patient */
  @Query(
      "SELECT sl FROM ShareLink sl WHERE sl.patient = :patient "
          + "AND sl.status = 'ACTIVE' AND sl.expiresAt > :now "
          + "ORDER BY sl.createdAt DESC")
  List<ShareLink> findActiveShareLinksByPatient(
      @Param("patient") PatientProfile patient, @Param("now") LocalDateTime now);

  /** Find expired share links */
  @Query("SELECT sl FROM ShareLink sl WHERE sl.expiresAt <= :now " + "AND sl.status = 'ACTIVE'")
  List<ShareLink> findExpiredShareLinks(@Param("now") LocalDateTime now);

  /** Find share links by doctor email */
  List<ShareLink> findByDoctorEmailOrderByCreatedAtDesc(String doctorEmail);

  /** Find share links expiring soon (within specified hours) */
  @Query(
      "SELECT sl FROM ShareLink sl WHERE sl.status = 'ACTIVE' "
          + "AND sl.expiresAt BETWEEN :now AND :futureTime "
          + "ORDER BY sl.expiresAt ASC")
  List<ShareLink> findShareLinksExpiringSoon(
      @Param("now") LocalDateTime now, @Param("futureTime") LocalDateTime futureTime);

  /** Count active share links for a patient */
  @Query(
      "SELECT COUNT(sl) FROM ShareLink sl WHERE sl.patient = :patient "
          + "AND sl.status = 'ACTIVE' AND sl.expiresAt > :now")
  long countActiveShareLinksByPatient(
      @Param("patient") PatientProfile patient, @Param("now") LocalDateTime now);

  /** Find share links by status */
  List<ShareLink> findByStatus(ShareLink.ShareLinkStatus status);

  /** Find share links created after a specific date */
  List<ShareLink> findByCreatedAtAfter(LocalDateTime createdAt);

  /** Find one-time share links that have been used */
  @Query("SELECT sl FROM ShareLink sl WHERE sl.oneTimeUse = true " + "AND sl.usageCount > 0")
  List<ShareLink> findUsedOneTimeLinks();

  /** Find share links with specific scope */
  @Query(
      "SELECT sl FROM ShareLink sl WHERE :scope MEMBER OF sl.scopes "
          + "AND sl.status = 'ACTIVE' AND sl.expiresAt > :now")
  List<ShareLink> findShareLinksByScope(
      @Param("scope") ShareLink.ShareScope scope, @Param("now") LocalDateTime now);
}
