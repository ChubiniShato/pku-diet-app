package com.chubini.pku.config;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import com.chubini.pku.service.RateLimitPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rate limiting configuration using Bucket4j HandlerInterceptor approach. Implements Rev C rules:
 * single source of truth, route tiers, userId/IP+UA identity.
 */
@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {

  private final RateLimitPolicy rateLimitPolicy;
  private final ObjectMapper objectMapper;

  public RateLimitingConfig(RateLimitPolicy rateLimitPolicy, ObjectMapper objectMapper) {
    this.rateLimitPolicy = rateLimitPolicy;
    this.objectMapper = objectMapper;
  }

  @Bean
  public RateLimitInterceptor rateLimitInterceptor() {
    return new RateLimitInterceptor(rateLimitPolicy, objectMapper);
  }

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    registry
        .addInterceptor(rateLimitInterceptor())
        .addPathPatterns("/api/**")
        .excludePathPatterns("/actuator/**");
  }

  /** Bucket4j-based rate limiting interceptor implementing Rev C unified approach. */
  public static class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final RateLimitPolicy rateLimitPolicy;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(RateLimitPolicy rateLimitPolicy, ObjectMapper objectMapper) {
      this.rateLimitPolicy = rateLimitPolicy;
      this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler)
        throws Exception {

      // Check Idempotency-Key requirement first
      if (rateLimitPolicy.requiresIdempotencyKey(request)) {
        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
          writeErrorResponse(
              response,
              HttpStatus.BAD_REQUEST,
              "IDEMPOTENCY_KEY_REQUIRED",
              "Idempotency-Key header is required for this operation",
              null);
          return false;
        }
      }

      // Apply rate limiting
      String clientId = rateLimitPolicy.getClientIdentifier(request);
      Bucket bucket = getBucket(clientId, request);

      if (bucket.tryConsume(1)) {
        return true;
      } else {
        // Rate limit exceeded - return 429 with proper headers
        long waitTime = bucket.getAvailableTokens() == 0 ? estimateRefillTime(request) : 0;

        writeRateLimitResponse(response, waitTime);
        return false;
      }
    }

    private Bucket getBucket(String clientId, HttpServletRequest request) {
      return buckets.computeIfAbsent(
          clientId,
          key -> {
            return Bucket.builder()
                .addLimit(rateLimitPolicy.getBandwidthForRequest(request))
                .build();
          });
    }

    private long estimateRefillTime(HttpServletRequest request) {
      // Estimate based on route tier - simplified approach
      if (request.getRequestURI().contains("/menu/generate")) {
        return 300; // 5 minutes for strict tier
      } else if (request.getRequestURI().contains("/auth/")) {
        return 60; // 1 minute for auth tier
      } else {
        return 60; // 1 minute for standard tier
      }
    }

    private void writeRateLimitResponse(HttpServletResponse response, long retryAfterSeconds)
        throws IOException {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
      response.setHeader("X-RateLimit-Limit", "varies by endpoint");
      response.setHeader("X-RateLimit-Remaining", "0");
      response.setHeader(
          "X-RateLimit-Reset", String.valueOf(Instant.now().getEpochSecond() + retryAfterSeconds));

      RateLimitErrorResponse errorResponse =
          new RateLimitErrorResponse(
              "RATE_LIMIT_EXCEEDED",
              "Rate limit exceeded. Please try again later.",
              "Rate limiting protects our service. Please wait before retrying.",
              generateTraceId());

      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private void writeErrorResponse(
        HttpServletResponse response,
        HttpStatus status,
        String code,
        String message,
        String details)
        throws IOException {
      response.setStatus(status.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      RateLimitErrorResponse errorResponse =
          new RateLimitErrorResponse(code, message, details, generateTraceId());

      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String generateTraceId() {
      return "rl-" + System.currentTimeMillis() + "-" + Thread.currentThread().hashCode();
    }
  }

  /** Unified error response format for rate limiting errors. */
  public static class RateLimitErrorResponse {
    public final String code;
    public final String message;
    public final String details;
    public final String traceId;

    public RateLimitErrorResponse(String code, String message, String details, String traceId) {
      this.code = code;
      this.message = message;
      this.details = details;
      this.traceId = traceId;
    }
  }
}
