package com.chubini.pku.sharing;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareLinkAccessLogRepository extends JpaRepository<ShareLinkAccessLog, UUID> {

  /** Find all access logs for a share link */
  List<ShareLinkAccessLog> findByShareLinkOrderByAccessedAtDesc(ShareLink shareLink);

  /** Find access logs within a date range */
  List<ShareLinkAccessLog> findByAccessedAtBetweenOrderByAccessedAtDesc(
      LocalDateTime startDate, LocalDateTime endDate);

  /** Find access logs for a specific share link within date range */
  List<ShareLinkAccessLog> findByShareLinkAndAccessedAtBetweenOrderByAccessedAtDesc(
      ShareLink shareLink, LocalDateTime startDate, LocalDateTime endDate);

  /** Count successful accesses for a share link */
  @Query(
      "SELECT COUNT(al) FROM ShareLinkAccessLog al WHERE al.shareLink = :shareLink "
          + "AND al.success = true")
  long countSuccessfulAccessesByShareLink(@Param("shareLink") ShareLink shareLink);

  /** Count failed accesses for a share link */
  @Query(
      "SELECT COUNT(al) FROM ShareLinkAccessLog al WHERE al.shareLink = :shareLink "
          + "AND al.success = false")
  long countFailedAccessesByShareLink(@Param("shareLink") ShareLink shareLink);

  /** Find recent failed access attempts (potential abuse detection) */
  @Query(
      "SELECT al FROM ShareLinkAccessLog al WHERE al.shareLink = :shareLink "
          + "AND al.success = false AND al.accessedAt > :since "
          + "ORDER BY al.accessedAt DESC")
  List<ShareLinkAccessLog> findRecentFailedAccesses(
      @Param("shareLink") ShareLink shareLink, @Param("since") LocalDateTime since);

  /** Find access logs by client IP (for security monitoring) */
  List<ShareLinkAccessLog> findByClientIpOrderByAccessedAtDesc(String clientIp);

  /** Find access logs with slow response times */
  @Query(
      "SELECT al FROM ShareLinkAccessLog al WHERE al.responseTimeMs > :threshold "
          + "ORDER BY al.responseTimeMs DESC")
  List<ShareLinkAccessLog> findSlowAccesses(@Param("threshold") Long threshold);

  /** Get access statistics for a share link */
  @Query(
      "SELECT new map("
          + "COUNT(al) as totalAccesses, "
          + "SUM(CASE WHEN al.success = true THEN 1 ELSE 0 END) as successfulAccesses, "
          + "AVG(al.responseTimeMs) as avgResponseTime, "
          + "MIN(al.accessedAt) as firstAccess, "
          + "MAX(al.accessedAt) as lastAccess) "
          + "FROM ShareLinkAccessLog al WHERE al.shareLink = :shareLink")
  Object getAccessStatistics(@Param("shareLink") ShareLink shareLink);
}
