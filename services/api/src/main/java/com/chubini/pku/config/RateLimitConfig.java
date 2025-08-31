package com.chubini.pku.config;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class RateLimitConfig {

  @Bean
  public RateLimitFilter rateLimitFilter() {
    return new RateLimitFilter();
  }

  public static class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, ClientRequestInfo> clientRequests =
        new ConcurrentHashMap<>();
    private final int maxRequestsPerMinute = 100; // Configurable rate limit
    private final long windowSizeMs = 60_000; // 1 minute window

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

      String clientId = getClientId(request);
      long currentTime = System.currentTimeMillis();

      ClientRequestInfo clientInfo =
          clientRequests.computeIfAbsent(clientId, k -> new ClientRequestInfo());

      // Reset window if expired
      if (currentTime - clientInfo.windowStart.get() > windowSizeMs) {
        clientInfo.requestCount.set(0);
        clientInfo.windowStart.set(currentTime);
      }

      int currentCount = clientInfo.requestCount.incrementAndGet();

      if (currentCount > maxRequestsPerMinute) {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.getWriter().write("Rate limit exceeded. Please try again later.");
        return;
      }

      filterChain.doFilter(request, response);
    }

    private String getClientId(HttpServletRequest request) {
      // Use IP address as client identifier
      // In production, you might want to use user ID or API key
      String xForwardedFor = request.getHeader("X-Forwarded-For");
      if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return xForwardedFor.split(",")[0].trim();
      }
      return request.getRemoteAddr();
    }

    private static class ClientRequestInfo {
      final AtomicInteger requestCount = new AtomicInteger(0);
      final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
    }
  }
}
