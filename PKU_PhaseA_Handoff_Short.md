### PKU Diet App – Phase A Handoff (Concise)

- **Status**: API starts; DB OK; `/actuator/health` = 503 (DOWN). Cause: Redis health failing; app tries `localhost:6379`.

- **Root Cause**: Redis host resolves to `localhost` inside API. Must use Docker service host `redis`.

- **Do This (Final Fix – Option B)**
1) docker-compose.yml → under `api.environment` ensure:
   - `SPRING_DATA_REDIS_HOST: redis`
   - `SPRING_DATA_REDIS_PORT: "6379"`
   - `SPRING_REDIS_HOST: redis`
   - `SPRING_REDIS_PORT: "6379"`

2) application-docker.yaml → guarantee mapping (if missing):
```
spring:
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST:redis}
      port: ${SPRING_DATA_REDIS_PORT:6379}
      timeout: 2s
```

3) (Optional during stabilization)
   - Keep `MANAGEMENT_HEALTH_REDIS_ENABLED: "false"` temporarily; remove when Redis OK.

- **Security & Actuator** (already set)
   - `SecurityConfig` permits `/actuator/**` and ignores CSRF.
   - Actuator base path `/actuator`, exposure `health,info,prometheus`, `show-details: always`.

- **Rebuild/Restart & Validate**
```
docker compose build api
docker compose restart api
curl -i http://localhost:8080/actuator/health
curl -s http://localhost:8080/actuator/health?showDetails=always
```
Expected: `status: "UP"` once Redis resolves to `redis:6379` (or when Redis health temporarily disabled).

- **If Still DOWN**
1) Confirm Redis reachable:
```
docker exec pku-diet-app-redis-1 redis-cli -h 127.0.0.1 PING
```
2) Check logs for failing indicator names.

- **Files touched this session**
`docker-compose.yml`, `services/api/src/main/resources/application-docker.yaml`, `services/api/src/main/resources/logback-spring.xml`, `services/api/src/main/java/com/chubini/pku/config/SecurityConfig.java`



