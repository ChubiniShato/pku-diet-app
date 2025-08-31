package com.chubini.pku.sharing;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

/** Audit log for share link access and usage */
@Entity
@Table(
    name = "share_link_access_log",
    indexes = {
      @Index(name = "idx_access_log_share_link", columnList = "share_link_id"),
      @Index(name = "idx_access_log_timestamp", columnList = "accessed_at"),
      @Index(name = "idx_access_log_ip", columnList = "client_ip"),
      @Index(name = "idx_access_log_user_agent", columnList = "user_agent")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareLinkAccessLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "share_link_id", nullable = false)
  private ShareLink shareLink;

  @Column(name = "access_type", nullable = false)
  private String accessType; // VIEW, DOWNLOAD, API_ACCESS, etc.

  @Column(name = "resource_accessed")
  private String resourceAccessed; // e.g., "CRITICAL_FACTS", "DAY:2024-01-15"

  @Column(name = "client_ip")
  private String clientIp;

  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "device_fingerprint")
  private String deviceFingerprint;

  @Column(name = "geolocation")
  private String geolocation; // Optional: country/city info

  @Column(name = "success", nullable = false)
  private Boolean success;

  @Column(name = "error_message")
  private String errorMessage;

  @Column(name = "response_time_ms")
  private Long responseTimeMs;

  @Column(name = "accessed_at", nullable = false)
  private LocalDateTime accessedAt;

  @Column(name = "additional_data", columnDefinition = "TEXT")
  private String additionalData; // JSON string for extra context

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Create access log entry for successful access */
  public static ShareLinkAccessLog success(
      ShareLink shareLink,
      String accessType,
      String resourceAccessed,
      String clientIp,
      String userAgent,
      Long responseTimeMs) {
    return ShareLinkAccessLog.builder()
        .shareLink(shareLink)
        .accessType(accessType)
        .resourceAccessed(resourceAccessed)
        .clientIp(clientIp)
        .userAgent(userAgent)
        .success(true)
        .responseTimeMs(responseTimeMs)
        .accessedAt(LocalDateTime.now())
        .build();
  }

  /** Create access log entry for failed access */
  public static ShareLinkAccessLog failure(
      ShareLink shareLink,
      String accessType,
      String errorMessage,
      String clientIp,
      String userAgent) {
    return ShareLinkAccessLog.builder()
        .shareLink(shareLink)
        .accessType(accessType)
        .clientIp(clientIp)
        .userAgent(userAgent)
        .success(false)
        .errorMessage(errorMessage)
        .accessedAt(LocalDateTime.now())
        .build();
  }

  /** Get user agent summary (browser/OS info) */
  public String getUserAgentSummary() {
    if (userAgent == null) return "Unknown";

    // Simple user agent parsing - could be enhanced
    if (userAgent.contains("Chrome")) return "Chrome";
    if (userAgent.contains("Firefox")) return "Firefox";
    if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
    if (userAgent.contains("Edge")) return "Edge";
    if (userAgent.contains("Mobile")) return "Mobile Browser";

    return "Other";
  }
}
