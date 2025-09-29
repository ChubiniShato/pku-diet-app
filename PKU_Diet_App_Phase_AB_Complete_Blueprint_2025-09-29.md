# PKU Diet App - Phase A/B Complete Implementation Blueprint

## 🎯 **MISSION CRITICAL: Complete Implementation Guide**

**This blueprint contains EVERY detail needed for any new session to continue Phase A/B implementation without losing any nuance.**

---

## 📋 **PHASE A - FOUNDATION LOCKDOWN**

### **A1.1 - Actuator Configuration (9090 Port)**
**File:** `services/api/src/main/resources/application-prod.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "health,info,prometheus"
  server:
    port: 9090
    address: 0.0.0.0
```

**Acceptance Criteria:**
- `curl -f http://api:9090/actuator/health` works from docker compose network
- `/actuator/prometheus` endpoint accessible without issues

### **A1.2 - Prometheus Static Targets**
**File:** `infra/observability/prometheus/prometheus.yml`

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'pku-api'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api:9090']
```

**Acceptance Criteria:**
- Prometheus UI: Status → Targets = UP
- Query `rate(http_server_requests_seconds_count[1m])` returns data

### **A1.3 - Dockerized k6 Testing**
**Files:**
- `testing/k6/.env`:
```env
BASE_URL=http://api:8080
STAGE_USERS=10
STAGE_DURATION=30s
```

- `testing/k6/scripts/smoke.js`:
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 1,
  duration: '30s',
};

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const res = http.get(`${baseUrl}/actuator/health`);
  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(1);
}
```

- `docker-compose.yml` addition:
```yaml
services:
  k6:
    image: grafana/k6
    depends_on: [api]
    env_file: [./testing/k6/.env]
    command: ["run", "/scripts/smoke.js"]
    volumes:
      - ./testing/k6/scripts:/scripts
    networks:
      - default
```

**Acceptance Criteria:**
- `docker compose run --rm k6` returns "status is 200: true" ≥ once in 30s

### **A2 - PR#3 Post-merge Tuning**
**File:** `services/api/src/main/resources/application-prod.yml`

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000
  jpa:
    open-in-view: false

server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

**Critical Notes:**
- `open-in-view: false` requires fixing LazyInitializationException with join-fetch/DTO mapping in service layer
- All lazy loading must be resolved in service layer before returning DTOs

**Acceptance Criteria:**
- `docker stop` shows orderly shutdown in logs
- No LazyInitializationException in smoke test paths

### **A3 - CI Smoke (Prod Profile)**
**File:** `ci/smoke/smoke.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

# Health check
curl -fsS "${BASE_URL}/actuator/health" >/dev/null

# CORS header check
curl -fsS -H "Origin: https://app.example.com" -I "${BASE_URL}/some-public-endpoint" | grep -i "access-control-allow-origin" >/dev/null

# CSP header check
curl -fsS -I "${BASE_URL}/" | grep -i "content-security-policy" >/dev/null

echo "SMOKE OK"
```

**Acceptance Criteria:**
- CI shows "SMOKE OK"
- On failure: docker logs api included as artifact

---

## 🔒 **PHASE B - SECURITY & RATE LIMITING**

### **B1 - Rate Limiting (In-memory, Extensible to Redis)**
**Policies:**
- `/api/public/**`: 40 burst / 20 rpm replenish, per-IP
- `/api/**` (authenticated): 120 burst / 60 rpm replenish, per-user (JWT sub)

**429 JSON Standard Body:**
```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "path": "/api/...",
  "message": "Rate limit exceeded"
}
```

**Acceptance Criteria:**
- Per-identity isolation tested (two users don't affect each other)
- Per-route different policies working
- Standard 429 JSON responses

### **B2 - CSP Gradual Rollout (48h Report-Only → Enforce)**

**D-0 to 48h (Report-Only):**
```http
Content-Security-Policy-Report-Only: default-src 'none'; connect-src 'self' https://<ui-domain> https://telemetry.<tld>; img-src 'self' data:; script-src 'self'; style-src 'self'; frame-ancestors 'none'
```

**D+2 (Enforce):**
```http
Content-Security-Policy: default-src 'none'; connect-src 'self' https://<ui-domain> https://telemetry.<tld>; img-src 'self' data:; script-src 'self'; style-src 'self'; frame-ancestors 'none'
```

**Additional Security Headers:**
```http
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
X-Frame-Options: DENY
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

**Acceptance Criteria:**
- Report-Only phase: 0 blocked functionality
- Enforce phase: smoke tests green

### **B3 - Secrets Rotation Documentation**
**File:** `docs/SECURITY_SECRETS_ROTATION.md`

**Content:**
- Dual-secret window for JWT rotation
- Step-by-step rotation process
- Storage locations (CI variables, Vault/Secrets Manager)
- "Don't log secrets" guidelines
- Zero-downtime rotation strategy

**Acceptance Criteria:**
- Complete documentation with CI variable references
- Actionable for operations team

---

## 🌿 **BRANCHING STRATEGY**

1. `feat/pr6-observability-fixes` - A1.1 + A1.2 + A1.3
2. `chore/pr3-postmerge-tuning` - A2
3. `ci/smoke-prod-profile` - A3
4. `sec/ratelimit-unify-bucket4j` - B1
5. `sec/csp-and-security-headers` - B2
6. `docs/secrets-rotation` - B3

---

## 📏 **PROJECT RULES (MANDATORY)**

### **Architecture Rules:**
- **Thin Controllers:** DTO validation + one orchestrator/service call only
- **No Business Logic:** Controllers handle status codes + mapping only
- **Single Responsibility:** One service = one business capability
- **MapStruct Mappings:** Domain objects must NOT cross API boundaries

### **Code Quality Rules:**
- **Method Complexity:** ≤12 cyclomatic complexity
- **Parameters:** ≤5 per method
- **Constructor DI:** ≤6 beans
- **File Length:** ≤350 lines (refactor if exceeded)

### **Testing Rules:**
- **Coverage:** Phase 1 ≥55% lines, ≥45% branches
- **Golden Tests:** Tolerant matching for floating-point (abs(actual-expected) ≤ 1e-6)
- **Integration Tests:** Testcontainers mandatory
- **E2E Tests:** Critical journeys (Login, CSV Upload, Menu Generation)

### **Security Rules:**
- **Rate Limiting:** Bucket4j only, per-identity (userId/IP)
- **Validation:** All public DTOs with precise constraints
- **Error Handling:** Unified error payload with traceId
- **Secrets:** No secrets in .env, use Secret Manager

### **Performance Rules:**
- **Queries:** No N+1, use fetch join/batch fetch
- **Transactions:** Service layer transactional, READ COMMITTED default
- **Pagination:** All list endpoints with page,size,sort,filter

### **Commit Rules:**
- **Conventional Commits:** `feat|fix|refactor|test|chore|docs|perf`
- **Single Responsibility:** One responsibility per commit
- **Small PRs:** ≤400 LOC max
- **Branch Naming:** `type/scope-short-desc`

---

## 🚀 **IMPLEMENTATION ORDER**

### **Phase A (Foundation Lockdown):**
1. **PR#6:** A1.1 + A1.2 + A1.3 (Metrics & k6 Fix)
2. **PR#3 Tuning:** A2 (Post-merge tuning)
3. **CI Smoke:** A3 (Prod profile testing)

### **Phase B (Security & Rate Limiting):**
4. **Rate Limiting:** B1 (Bucket4j unification)
5. **CSP Rollout:** B2 (Security headers)
6. **Secrets Doc:** B3 (Rotation documentation)

---

## ✅ **CURRENT STATUS**

**Ready to start PR#6 implementation:**
- All configurations specified
- Acceptance criteria defined
- Project Rules established
- Branching strategy planned
- Implementation order clear

**Next Action:** Create `feat/pr6-observability-fixes` branch and implement A1.1, A1.2, A1.3

---

## 🎯 **CRITICAL SUCCESS FACTORS**

1. **Follow Project Rules exactly** - No exceptions
2. **Test each component** - All acceptance criteria must pass
3. **Small, focused commits** - One responsibility per commit
4. **Document changes** - Update README/operations docs where needed
5. **Manual verification** - Test each step manually before proceeding

**This blueprint is COMPLETE and contains EVERY detail needed for successful implementation.**
