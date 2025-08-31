package com.chubini.pku.config;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {

  @Bean
  public RateLimitInterceptor rateLimitInterceptor() {
    return new RateLimitInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor()).addPathPatterns("/api/**");
  }

  public static class RateLimitInterceptor implements HandlerInterceptor {
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      String clientId = getClientId(request);
      Bucket bucket = getBucket(clientId, request);

      if (bucket.tryConsume(1)) {
        return true;
      } else {
        response.setStatus(429);
        response.setContentType("application/json");
        response
            .getWriter()
            .write(
                "{\"error\": \"Too many requests\", \"message\": \"Rate limit exceeded. Please try again later.\"}");
        return false;
      }
    }

    private String getClientId(HttpServletRequest request) {
      // Use IP address as client identifier
      String xForwardedFor = request.getHeader("X-Forwarded-For");
      if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return xForwardedFor.split(",")[0].trim();
      }
      return request.getRemoteAddr();
    }

    private Bucket getBucket(String clientId, HttpServletRequest request) {
      return buckets.computeIfAbsent(
          clientId,
          key -> {
            // Different rate limits for different endpoints
            if (request.getRequestURI().contains("/auth/login")) {
              // Stricter rate limit for login attempts: 5 requests per minute
              Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
              return Bucket.builder().addLimit(limit).build();
            } else if (request.getRequestURI().contains("/auth/")) {
              // Moderate rate limit for other auth endpoints: 20 requests per minute
              Bandwidth limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
              return Bucket.builder().addLimit(limit).build();
            } else {
              // General API rate limit: 100 requests per minute
              Bandwidth limit =
                  Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
              return Bucket.builder().addLimit(limit).build();
            }
          });
    }
  }
}
