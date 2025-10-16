package com.chubini.pku.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chubini.pku.service.RateLimitPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.bucket4j.Bandwidth;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for rate limiting functionality without Spring context. Tests the core rate limiting
 * logic according to Rev C rules.
 */
class RateLimitingUnitTest {

  private RateLimitPolicy rateLimitPolicy;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    rateLimitPolicy = new RateLimitPolicy();
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldDetermineCorrectBandwidthForMenuGeneration() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/v1/menu/generate");

    Bandwidth bandwidth = rateLimitPolicy.getBandwidthForRequest(request);

    // Menu generation should have strict limits (10 requests per 5 minutes)
    assertEquals(10, bandwidth.getCapacity());
  }

  @Test
  void shouldDetermineCorrectBandwidthForAuthEndpoints() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

    Bandwidth bandwidth = rateLimitPolicy.getBandwidthForRequest(request);

    // Auth login should have strict limits (5 requests per minute)
    assertEquals(5, bandwidth.getCapacity());
  }

  @Test
  void shouldDetermineCorrectBandwidthForStandardAPI() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/api/v1/products");

    Bandwidth bandwidth = rateLimitPolicy.getBandwidthForRequest(request);

    // Standard API should have higher limits (100 requests per minute)
    assertEquals(100, bandwidth.getCapacity());
  }

  @Test
  void shouldRequireIdempotencyKeyForUploadEndpoints() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/api/v1/import/csv");

    boolean requiresKey = rateLimitPolicy.requiresIdempotencyKey(request);

    assertTrue(requiresKey, "POST import endpoints should require Idempotency-Key");
  }

  @Test
  void shouldNotRequireIdempotencyKeyForRegularEndpoints() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/v1/products");

    boolean requiresKey = rateLimitPolicy.requiresIdempotencyKey(request);

    assertFalse(requiresKey, "GET endpoints should not require Idempotency-Key");
  }

  @Test
  void shouldGenerateClientIdentifierForUnauthenticatedRequest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getHeader("X-Real-IP")).thenReturn(null);

    String clientId = rateLimitPolicy.getClientIdentifier(request);

    assertTrue(
        clientId.startsWith("ip:192.168.1.1:ua:"),
        "Unauthenticated requests should use IP+UA format");
  }
}
