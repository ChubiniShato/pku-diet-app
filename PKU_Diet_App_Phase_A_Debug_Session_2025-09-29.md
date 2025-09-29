# PKU Diet App - Phase A Debug Session Log
**Date:** 2025-09-29  
**Session Duration:** ~2 hours  
**Status:** BLOCKING - Actuator health endpoint returns 503 Service Unavailable

---

## 🎯 **SESSION OBJECTIVE**
Complete Phase A implementation from the PKU Diet App Phase A/B Blueprint:
- A1.1 - Actuator Configuration (port 8080)
- A1.2 - Prometheus Static Targets  
- A1.3 - Dockerized k6 Testing

---

## 🚨 **CURRENT BLOCKING ISSUE**
**Problem:** Actuator health endpoint returns 503 Service Unavailable
**Endpoint:** `http://localhost:8080/actuator/health`
**Status:** All configuration appears correct, but health check fails

---

## 📋 **ISSUES IDENTIFIED & RESOLVED**

### ✅ **Issue 1: Logback Configuration Error**
**Problem:** Container fails to start due to logback file logging error
```
ERROR in ch.qos.logback.core.rolling.RollingFileAppender[FILE] - Failed to create parent directories for [/app/logs/pku-api.log]
```

**Root Cause:** 
- Dockerfile uses `distroless/java21-debian12` (minimal image)
- User is `65534:65534` (non-root)
- `/app/logs` directory doesn't exist and can't be created

**Solution Applied:**
1. Modified `logback-spring.xml` to disable file logging in docker profile
2. Added profile-specific configuration:
```xml
<springProfile name="docker">
  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</springProfile>
```
3. Changed docker-compose.yml to use `docker` profile instead of `prod,docker`

**Status:** ✅ RESOLVED

### ✅ **Issue 2: Management Server Configuration Error**
**Problem:** 
```
Management-specific server address cannot be configured as the management server is not listening on a separate port
```

**Root Cause:** 
- Management server port was set to 8080 (same as main server)
- Address `0.0.0.0` was also configured
- Spring Boot doesn't allow address configuration when using same port

**Solution Applied:**
1. Removed management server configuration from `application-docker.yaml`
2. Let actuator use main server port (8080)
3. Removed `address: 0.0.0.0` configuration

**Status:** ✅ RESOLVED

### ✅ **Issue 3: Docker Restart Loop**
**Problem:** Container kept restarting after configuration fixes

**Root Cause:** Docker Desktop had memory/resource issues

**Solution Applied:**
1. Completely shut down Docker Desktop
2. Waited 10-15 seconds
3. Restarted Docker Desktop
4. Rebuilt and restarted containers

**Status:** ✅ RESOLVED

### ✅ **Issue 4: Health Check Configuration**
**Problem:** Health check using `wget` which doesn't exist in distroless image

**Solution Applied:**
1. Commented out health check in `docker-compose.yml`
2. Set health check details to `always` in `application-docker.yaml`

**Status:** ✅ RESOLVED

---

## 🔧 **CONFIGURATION CHANGES MADE**

### 1. **logback-spring.xml**
```xml
<!-- Docker profile - console only, no file logging -->
<springProfile name="docker">
  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</springProfile>
```

### 2. **docker-compose.yml**
```yaml
environment:
  SPRING_PROFILES_ACTIVE: docker  # Changed from prod,docker

# healthcheck:  # Commented out
#   test: ["CMD", "wget", "-qO-", "http://localhost:8080/actuator/health"]
```

### 3. **application-docker.yaml**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      show-details: always  # Changed from when_authorized
      show-components: always
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

# Security configuration for Docker
app:
  security:
    cors:
      allowed-origins: "http://localhost:3000,http://localhost:5173,http://localhost:8080"
```

---

## 📊 **CURRENT STATUS**

### ✅ **Working Components**
- **Application Startup:** ✅ Successfully starts
- **Database Connection:** ✅ PostgreSQL connection working
- **Redis Connection:** ✅ Redis connection working
- **Logging:** ✅ Console logging working
- **Port Binding:** ✅ Port 8080 accessible

### ❌ **Not Working**
- **Actuator Health Endpoint:** Returns 503 Service Unavailable
- **Actuator Info Endpoint:** Not tested (likely same issue)
- **Actuator Prometheus Endpoint:** Not tested (likely same issue)

---

## 🔍 **DEBUGGING ATTEMPTS**

### 1. **Security Configuration Check**
- Verified `SecurityConfig.java` has actuator endpoints in `permitAll()`
- Added CORS configuration for Docker profile
- **Result:** Still 503

### 2. **Health Check Details**
- Changed from `when_authorized` to `always`
- **Result:** Still 503

### 3. **Profile Configuration**
- Confirmed using `docker` profile
- Verified configuration inheritance
- **Result:** Still 503

### 4. **Container Status**
```bash
docker ps
# Shows: Up X minutes (health: starting) - never becomes healthy
```

---

## 📝 **DETAILED LOGS**

### **Application Startup Logs (Latest)**
```
2025-09-29 06:37:04.135 [main] INFO  com.chubini.pku.PkuApiApplication - Started PkuApiApplication in 28.529 seconds (process running for 29.458)
2025-09-29 06:37:02.016 [main] INFO  o.s.b.a.e.web.EndpointLinksResolver - Exposing 3 endpoints beneath base path '/actuator'
2025-09-29 06:37:04.095 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port 8080 (http) with context path '/'
```

### **Health Check Response**
```bash
Invoke-WebRequest -Uri http://localhost:8080/actuator/health -UseBasicParsing
# Returns: 503 Service Unavailable
```

### **Other Endpoint Tests**
```bash
# Main endpoint
Invoke-WebRequest -Uri http://localhost:8080/ -UseBasicParsing
# Returns: 403 Forbidden

# Actuator root
Invoke-WebRequest -Uri http://localhost:8080/actuator -UseBasicParsing  
# Returns: 403 Forbidden

# API endpoints
Invoke-WebRequest -Uri http://localhost:8080/api/public/health -UseBasicParsing
# Returns: 403 Forbidden
```

---

## 🤔 **HYPOTHESES FOR 503 ERROR**

1. **Database Health Check Failing**
   - PostgreSQL connection might be unhealthy
   - Health check might be checking database status

2. **Spring Boot Actuator Configuration Issue**
   - Management endpoints might not be properly configured
   - Health indicators might be failing

3. **Security Filter Chain Issue**
   - Security configuration might be interfering with actuator
   - JWT filter might be blocking requests

4. **Application Context Issue**
   - Application might not be fully initialized
   - Some beans might be failing to load

---

## 🎯 **NEXT STEPS FOR DEBUGGING**

### **Immediate Actions Needed:**
1. **Check Database Health**
   ```bash
   docker exec pku-diet-app-db-1 pg_isready -U pku -d pku
   ```

2. **Check Application Logs for Errors**
   ```bash
   docker logs pku-diet-app-api-1 | grep -i error
   ```

3. **Test with curl instead of PowerShell**
   ```bash
   curl -v http://localhost:8080/actuator/health
   ```

4. **Check if other actuator endpoints work**
   ```bash
   curl http://localhost:8080/actuator/info
   curl http://localhost:8080/actuator/prometheus
   ```

5. **Enable debug logging for actuator**
   - Add debug logging to see what's happening in health check

### **Configuration Changes to Try:**
1. **Disable Security for Actuator**
   ```java
   .requestMatchers("/actuator/**").permitAll()
   ```

2. **Add Health Check Configuration**
   ```yaml
   management:
     health:
       defaults:
         enabled: true
   ```

3. **Check Database Health Indicator**
   ```yaml
   management:
     health:
       db:
         enabled: true
   ```

---

## 📁 **FILES MODIFIED**

1. `services/api/src/main/resources/logback-spring.xml`
2. `docker-compose.yml`
3. `services/api/src/main/resources/application-docker.yaml`

---

## 🔗 **RELEVANT FILES TO CHECK**

1. `services/api/src/main/java/com/chubini/pku/config/SecurityConfig.java`
2. `services/api/src/main/resources/application-base.yaml`
3. `services/api/src/main/resources/application-prod.yaml`
4. `services/api/src/main/resources/application.yaml`

---

## 💡 **RECOMMENDATIONS FOR NEXT SESSION**

1. **Start with database health check** - most likely cause
2. **Enable debug logging** for actuator and health components
3. **Test with curl** instead of PowerShell Invoke-WebRequest
4. **Check Spring Boot actuator documentation** for health check configuration
5. **Consider adding custom health indicators** if needed

---

## 📊 **SUCCESS CRITERIA FOR PHASE A**

- [ ] `curl -f http://api:8080/actuator/health` returns 200 OK
- [ ] `/actuator/prometheus` endpoint accessible
- [ ] Prometheus can scrape metrics from API
- [ ] k6 smoke test passes: "status is 200: true" ≥ once in 30s

---

**Session End Time:** 2025-09-29 10:40 UTC+4  
**Next Action:** Debug database health check and enable detailed logging

