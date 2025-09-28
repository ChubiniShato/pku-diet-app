# PR#3 - Production Profile Hardening - Enhancement Recommendations

## 🔧 დამატებითი გაუმჯობესებები

### **1. Application Properties Structure**

```yaml
# application-prod.yaml-ში დავამატებდი:
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
      idle-timeout: 300000
  jpa:
    open-in-view: false  # Performance improvement
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

### **2. Observability Enhancement**

```yaml
# Metrics შიდა ქსელისთვის მხოლოდ (IP allowlist/mTLS უკან):
management:
  server:
    port: 8081  # შიდა management port
  endpoints:
    web:
      exposure:
        include: "health,info"  # public endpoints
      # metrics მხოლოდ შიდა ქსელიდან
  metrics:
    tags:
      application: pku-diet-api
      environment: production
    export:
      prometheus:
        enabled: true  # შიდა collector-ისთვის
        
# OpenTelemetry მხოლოდ collector მზადყოფნისას:
# management:
#   tracing:
#     sampling:
#       probability: 0.1
```

### **3. Rate Limiting Prod Tuning**

```yaml
# application-prod.yaml-ში (restart-on-change მხოლოდ):
app:
  rate-limiting:
    strict-tier:
      capacity: 5      # უფრო მკაცრი prod-ში
      refill-period: 300s
    moderate-tier:
      capacity: 15
      refill-period: 60s
    standard-tier:
      capacity: 50     # შემცირებული prod-ისთვის
      refill-period: 60s
    # OPTIONS CORS preflight გამორიცხვა:
    exclude-methods: ["OPTIONS"]
```

**⚠️ შენიშვნა:** Bucket4j buckets immutable არის - hot-reload ნაცვლად restart საჭიროა.

### **4. Error Handling Enhancement**

```java
// SecurityConfig-ში დავამატებდი:
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        // ... existing config ...
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((req, res, authEx) -> {
                res.setStatus(HttpStatus.UNAUTHORIZED.value());
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.setHeader("X-Content-Type-Options", "nosniff");
                
                // Prod-ში დეტალები არ გამოვიტანოთ:
                String errorResponse = """
                    {
                        "error": "UNAUTHORIZED",
                        "message": "Authentication required",
                        "timestamp": "%s"
                    }
                    """.formatted(Instant.now().toString());
                
                res.getWriter().write(errorResponse);
            })
            .accessDeniedHandler((req, res, accessEx) -> {
                res.setStatus(HttpStatus.FORBIDDEN.value());
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.setHeader("X-Content-Type-Options", "nosniff");
                
                String errorResponse = """
                    {
                        "error": "FORBIDDEN",
                        "message": "Access denied",
                        "timestamp": "%s",
                        "path": "%s"
                    }
                    """.formatted(Instant.now().toString(), req.getRequestURI());
                
                res.getWriter().write(errorResponse);
            })
        )
        .build();
}
```

### **5. Docker Health Check**

```dockerfile
# Dockerfile-ში (lightweight health check):
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

# Multi-stage build optimization
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
USER appuser
COPY --chown=appuser:appuser target/api-*.jar app.jar
EXPOSE 8080 8081
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app.jar"]
```

### **6. Smoke Tests გაფართოება**

```bash
#!/bin/bash
# smoke-test-prod.sh

set -e

BASE_URL=${BASE_URL:-http://localhost:8080}
EXPECTED_ORIGINS=${EXPECTED_ORIGINS:-"https://app.pku.example"}

echo "🔍 Starting production smoke tests..."

# 1. Health check
echo "✅ Testing health endpoint..."
curl -sSf "$BASE_URL/actuator/health" | jq -e '.status == "UP"' > /dev/null
echo "   Health check passed"

# 2. Basic API connectivity (არა მძიმე endpoints)
echo "✅ Testing API connectivity..."
HTTP_CODE=$(curl -s -w "%{http_code}" -o /dev/null "$BASE_URL/api/v1/ping" || echo "000")
if [[ "$HTTP_CODE" == "200" || "$HTTP_CODE" == "401" ]]; then
    echo "   API endpoint accessible"
else
    echo "   ❌ Unexpected status code: $HTTP_CODE"
    exit 1
fi

# 3. CORS headers
echo "✅ Testing CORS headers..."
CORS_HEADER=$(curl -s -D- -o /dev/null -H "Origin: $EXPECTED_ORIGINS" "$BASE_URL/api/v1/ping" | grep -i "access-control-allow-origin" || true)
if [[ -n "$CORS_HEADER" ]]; then
    echo "   CORS headers present"
else
    echo "   ⚠️  CORS headers not found (may be expected for non-matching origins)"
fi

# 4. Security headers comprehensive check
echo "✅ Testing security headers..."
SECURITY_HEADERS=$(curl -s -D- -o /dev/null "$BASE_URL/api/v1/ping" | \
  grep -E "(X-Content-Type-Options|X-Frame-Options|Content-Security-Policy|Referrer-Policy|Permissions-Policy)" | \
  wc -l)

if [[ "$SECURITY_HEADERS" -ge 4 ]]; then
    echo "   Security headers present ($SECURITY_HEADERS/5)"
else
    echo "   ❌ Missing security headers (found: $SECURITY_HEADERS/5)"
    exit 1
fi

# 5. SQL logging check (should not be visible in logs)
echo "✅ Testing SQL logging is disabled..."
LOG_FILE=${LOG_FILE:-/tmp/app.log}
if [[ -f "$LOG_FILE" ]]; then
    SQL_LOGS=$(grep -i "select\|insert\|update\|delete" "$LOG_FILE" | wc -l || echo "0")
    if [[ "$SQL_LOGS" -eq 0 ]]; then
        echo "   SQL logging properly disabled"
    else
        echo "   ⚠️  Found $SQL_LOGS SQL statements in logs"
    fi
else
    echo "   Log file not found, skipping SQL log check"
fi

# 6. Actuator endpoints restriction
echo "✅ Testing actuator endpoints restriction..."
METRICS_CODE=$(curl -s -w "%{http_code}" -o /dev/null "$BASE_URL/actuator/metrics" || echo "000")
if [[ "$METRICS_CODE" == "404" ]]; then
    echo "   Metrics endpoint properly restricted"
else
    echo "   ⚠️  Metrics endpoint accessible (code: $METRICS_CODE)"
fi

echo "🎉 Production smoke tests completed successfully!"
```

### **7. Configuration Validation**

```java
// AppProperties.java
package com.chubini.pku.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app")
@Validated
public class AppProperties {

    @Valid
    private Security security = new Security();
    
    @Valid
    private Cors cors = new Cors();
    
    @Valid
    private RateLimiting rateLimiting = new RateLimiting();

    // Getters and setters...

    public static class Security {
        @NotEmpty
        private String jwtSecret;
        
        private int jwtExpirationHours = 24;
        
        // Getters and setters...
    }

    public static class Cors {
        @NotEmpty
        private List<@Pattern(regexp = "https://.*", message = "Prod origins must use HTTPS") String> allowedOrigins;
        
        // Getters and setters...
    }
    
    public static class RateLimiting {
        @Valid
        private Tier strictTier = new Tier();
        
        @Valid
        private Tier moderateTier = new Tier();
        
        @Valid
        private Tier standardTier = new Tier();
        
        public static class Tier {
            private int capacity = 100;
            private int refillPeriodSeconds = 60;
            
            // Getters and setters...
        }
        
        // Getters and setters...
    }
}
```

### **8. Flyway Prod Safety**

```yaml
# application-prod.yaml
spring:
  flyway:
    validate-on-migrate: true
    clean-disabled: true  # prod-ში clean აკრძალული
    out-of-order: false
    baseline-on-migrate: false
    locations: classpath:db/migration
    sql-migration-suffixes: .sql
    repeatable-sql-migration-prefix: R
    target: latest
```

### **9. Jackson Security**

```yaml
# application-prod.yaml
spring:
  jackson:
    deserialization:
      # Public APIs → false (forward compatibility)
      # External ingestion → true + whitelist deserializers
      fail-on-unknown-properties: false  # API evolution-ისთვის
      fail-on-null-for-primitives: true
      fail-on-numbers-for-enums: true
    default-property-inclusion: NON_NULL
    serialization:
      write-dates-as-timestamps: false
      fail-on-empty-beans: false
    mapper:
      accept-case-insensitive-enums: false
```

### **10. Graceful Shutdown**

```yaml
# application-prod.yaml
server:
  shutdown: graceful
  tomcat:
    connection-timeout: 20000
    keep-alive-timeout: 15000
    max-connections: 200
    threads:
      max: 50
      min-spare: 10

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
  task:
    execution:
      shutdown:
        await-termination: true
        await-termination-period: 30s
```

### **11. Monitoring & Alerting Configuration**

```yaml
# application-prod.yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
      group:
        liveness:
          include: livenessState,diskSpace
        readiness:
          include: readinessState,db,redis
      show-details: when-authorized
      show-components: when-authorized
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true
```

### **12. Enhanced Security Configuration**

```java
// Enhanced SecurityConfig.java additions
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    @Profile("prod")
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                       .maximumSessions(1)
                       .maxSessionsPreventsLogin(false))
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                    .preload(true))
                .contentSecurityPolicy(csp -> csp
                    // UI/CDN მოთხოვნილებების გათვალისწინებით:
                    .policyDirectives("default-src 'self'; " +
                                    "img-src 'self' data: https: blob:; " +
                                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                                    "script-src 'self' 'unsafe-eval'; " +  // SPA-ისთვის
                                    "connect-src 'self' https://api.pku.example; " +
                                    "font-src 'self' https://fonts.gstatic.com; " +
                                    "frame-ancestors 'none'; " +
                                    "base-uri 'self'; " +
                                    "form-action 'self'"))
                .and())
            .build();
    }
}
```

## 📋 **PR Template-ში დავამატებდი:**

```markdown
### Production Readiness Checklist
- [ ] Database connection pool configured for prod load (max-pool-size: 10)
- [ ] OpenTelemetry sampling rate set to 0.1 (10%)
- [ ] Graceful shutdown configured (30s timeout)
- [ ] Flyway clean disabled in prod
- [ ] Jackson security settings applied (fail-on-unknown-properties: true)
- [ ] Docker health check implemented
- [ ] Rate limiting tuned for prod traffic (strict: 5/5min, moderate: 15/min)
- [ ] HSTS headers enabled with preload
- [ ] Enhanced CSP policy with base-uri and form-action
- [ ] Actuator endpoints restricted to health/info only
- [ ] Configuration validation with @Validated annotations
- [ ] Smoke tests pass with all security headers
- [ ] SQL logging completely disabled in prod logs
```

## 🚀 **Implementation Priority:**

1. **High Priority:** Application properties, Security headers, Graceful shutdown
2. **Medium Priority:** Enhanced error handling, Configuration validation, Smoke tests
3. **Low Priority:** Docker optimizations, Advanced monitoring configuration

## 📝 **Notes:**

- ყველა ცვლილება profile-specific არის და არ აზარალებს dev/test environments
- Smoke tests შეიძლება CI/CD pipeline-ში ინტეგრირდეს
- Configuration validation უზრუნველყოფს startup-time error detection
- Enhanced security headers მიყვება OWASP რეკომენდაციებს
