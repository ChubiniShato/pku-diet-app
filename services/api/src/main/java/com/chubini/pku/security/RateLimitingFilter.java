package com.chubini.pku.security;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

  @Value("${app.security.rate-limit.requests-per-minute:60}")
  private int requestsPerMinute;

  @Value("${app.security.rate-limit.requests-per-hour:1000}")
  private int requestsPerHour;

  // In-memory cache for rate limiting buckets
  private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Skip rate limiting for health checks and static resources
    String requestURI = request.getRequestURI();
    if (shouldSkipRateLimit(requestURI)) {
      filterChain.doFilter(request, response);
      return;
    }

    String clientIp = getClientIP(request);
    Bucket tokenBucket = getBucket(clientIp);

    if (tokenBucket.tryConsume(1)) {
      // Add rate limit headers
      addRateLimitHeaders(response, tokenBucket);
      filterChain.doFilter(request, response);
    } else {
      // Rate limit exceeded
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType("application/json");
      response.getWriter().write(createRateLimitErrorResponse());
      addRateLimitHeaders(response, tokenBucket);
    }
  }

  private boolean shouldSkipRateLimit(String requestURI) {
    return requestURI.startsWith("/actuator/health")
        || requestURI.startsWith("/actuator/info")
        || requestURI.startsWith("/swagger-ui")
        || requestURI.startsWith("/v3/api-docs")
        || requestURI.endsWith(".css")
        || requestURI.endsWith(".js")
        || requestURI.endsWith(".ico");
  }

  private String getClientIP(HttpServletRequest request) {
    // Check for X-Forwarded-For header (proxy/load balancer)
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    // Check for X-Real-IP header
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    // Fallback to remote address
    return request.getRemoteAddr();
  }

  private Bucket getBucket(String clientIp) {
    return cache.computeIfAbsent(clientIp, this::createNewBucket);
  }

  private Bucket createNewBucket(String clientIp) {
    // Create bucket with two bandwidth limits:
    // 1. Per-minute limit (burst protection)
    // 2. Per-hour limit (sustained usage protection)
    Bandwidth perMinuteLimit =
        Bandwidth.classic(
            requestsPerMinute, Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));

    Bandwidth perHourLimit =
        Bandwidth.classic(requestsPerHour, Refill.intervally(requestsPerHour, Duration.ofHours(1)));

    return Bucket.builder().addLimit(perMinuteLimit).addLimit(perHourLimit).build();
  }

  private void addRateLimitHeaders(HttpServletResponse response, Bucket bucket) {
    // Add standard rate limiting headers
    response.setHeader("X-RateLimit-Limit-Minute", String.valueOf(requestsPerMinute));
    response.setHeader("X-RateLimit-Limit-Hour", String.valueOf(requestsPerHour));
    response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));

    // Add retry-after header for rate limited requests
    if (bucket.getAvailableTokens() == 0) {
      response.setHeader("Retry-After", "60"); // Retry after 1 minute
    }
  }

  private String createRateLimitErrorResponse() {
    return "{"
        + "\"timestamp\":\""
        + java.time.LocalDateTime.now()
        + "\","
        + "\"status\":429,"
        + "\"error\":\"Too Many Requests\","
        + "\"message\":\"Rate limit exceeded. Please try again later.\","
        + "\"details\":{"
        + "\"limitPerMinute\":"
        + requestsPerMinute
        + ","
        + "\"limitPerHour\":"
        + requestsPerHour
        + "}"
        + "}";
  }
}
