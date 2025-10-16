package com.chubini.pku.service;

import java.time.Duration;
import java.util.UUID;

import com.chubini.pku.security.User;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Service responsible for rate limiting policy decisions according to Rev C rules.
 *
 * <p>Rate limiting strategy:
 *
 * <ul>
 *   <li>Identity: per userId (authenticated) or per IP+UA (public)
 *   <li>Route tiers: /menu/generate strict, /auth/* moderate, general API standard
 *   <li>Single source of truth: Bucket4j only
 * </ul>
 */
@Service
public class RateLimitPolicy {

  /** Route tier definitions according to Rev C rules. */
  public enum RouteTier {
    STRICT_BURST, // /menu/generate - CPU intensive
    MODERATE_AUTH, // /auth/* - security sensitive
    STANDARD_API // General API endpoints
  }

  /**
   * Determines the appropriate rate limit bandwidth for a request.
   *
   * @param request the HTTP request
   * @return Bandwidth configuration for the request
   */
  public Bandwidth getBandwidthForRequest(HttpServletRequest request) {
    RouteTier tier = determineRouteTier(request);

    switch (tier) {
      case STRICT_BURST:
        // Menu generation: strict burst + long window
        // 10 requests per 5 minutes (CPU-intensive operations)
        return Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(5)));

      case MODERATE_AUTH:
        // Auth endpoints: moderate limits for security
        if (request.getRequestURI().contains("/auth/login")) {
          // Login attempts: stricter to prevent brute force
          return Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        } else {
          // Other auth endpoints: moderate
          return Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
        }

      case STANDARD_API:
      default:
        // General API: standard rate limit
        return Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
    }
  }

  /**
   * Generates a unique client identifier for rate limiting. Uses userId for authenticated requests,
   * IP+UA for public requests.
   *
   * @param request the HTTP request
   * @return unique client identifier
   */
  public String getClientIdentifier(HttpServletRequest request) {
    // Try to get authenticated user ID first (preferred)
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
      User user = (User) auth.getPrincipal();
      UUID userId = user.getId();
      if (userId != null) {
        return "user:" + userId.toString();
      }
    }

    // Fallback to IP + User-Agent for unauthenticated requests
    String ip = extractClientIp(request);
    String userAgent = request.getHeader("User-Agent");
    if (userAgent == null) {
      userAgent = "unknown";
    }

    // Truncate user agent to prevent excessive key length
    if (userAgent.length() > 100) {
      userAgent = userAgent.substring(0, 100);
    }

    return "ip:" + ip + ":ua:" + userAgent.hashCode();
  }

  /**
   * Determines if the request requires Idempotency-Key header.
   *
   * @param request the HTTP request
   * @return true if Idempotency-Key is required
   */
  public boolean requiresIdempotencyKey(HttpServletRequest request) {
    String method = request.getMethod();
    String uri = request.getRequestURI();

    // POST upload/import endpoints require Idempotency-Key
    return "POST".equals(method)
        && (uri.contains("/upload") || uri.contains("/import") || uri.contains("/csv"));
  }

  private RouteTier determineRouteTier(HttpServletRequest request) {
    String uri = request.getRequestURI();

    if (uri.contains("/menu/generate")) {
      return RouteTier.STRICT_BURST;
    } else if (uri.contains("/auth/")) {
      return RouteTier.MODERATE_AUTH;
    } else {
      return RouteTier.STANDARD_API;
    }
  }

  private String extractClientIp(HttpServletRequest request) {
    // Check X-Forwarded-For header first (proxy/load balancer)
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // Take the first IP in the chain
      return xForwardedFor.split(",")[0].trim();
    }

    // Check X-Real-IP header
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    // Fallback to remote address
    return request.getRemoteAddr();
  }
}
