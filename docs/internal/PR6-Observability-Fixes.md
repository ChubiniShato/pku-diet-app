# PR#6 - Observability Fixes & Enhancements

## 🎯 ძირითადი მიზნები
- Metrics endpoint access გამოსწორება
- k6 Performance Testing გამოსწორება  
- Grafana Dashboards დაკონფიგურირება
- Documentation Quickstart guide

## 🔧 ზუსტი ფაილები რომლებიც უნდა შეიცვალოს

### 1. API Configuration (services/api/src/main/resources/)
**application-docker.yaml** - Metrics endpoint exposure:
```yaml
management:
  server:
    port: 8080
  address: 0.0.0.0
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    health:
      probes:
        enabled: true
  metrics:
    tags:
      application: pku-api
      environment: local
      service: api
```

### 2. Prometheus Configuration (ops/prometheus.yml)
**prometheus.yml** - სწორი targets:
```yaml
scrape_configs:
  - job_name: 'api'
    scrape_interval: 10s
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['api:8080']
    metric_relabel_configs:
      - source_labels: [uri]
        regex: '.*/\d+.*'
        target_label: uri
        replacement: '/{id}'
        action: replace
```

### 3. Grafana Data Source (grafana/provisioning/datasources/datasource.yml)
**datasource.yml** - Prometheus connection:
```yaml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: false
```

### 4. k6 Performance Testing (k6/load-test.js)
**load-test.js** - Parameterized script:
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m',  target: 10 },
    { duration: '30s', target: 0  },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

export default function () {
  const res = http.get(`${BASE_URL}/api/v1/products?size=5`);
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(1);
}
```

### 5. Docker Compose Updates (docker-compose.observability.yml)
**docker-compose.observability.yml** - Health checks + dependencies:
```yaml
api:
  image: pku-api
  ports: ['8080:8080']
  healthcheck:
    test: ['CMD', 'wget', '-qO-', 'http://localhost:8080/actuator/health']
    interval: 10s
    timeout: 2s
    retries: 5

prometheus:
  depends_on: [api]
  command:
    - '--config.file=/etc/prometheus/prometheus.yml'
    - '--storage.tsdb.retention.time=15d'
    - '--web.enable-lifecycle'

grafana:
  depends_on: [prometheus]
  volumes:
    - ./grafana/provisioning/datasources:/etc/grafana/provisioning/datasources
    - ./grafana/provisioning/dashboards:/etc/grafana/provisioning/dashboards
    - ./grafana/dashboards:/var/lib/grafana/dashboards
```

### 6. Health Check Script (scripts/health-check.sh)
**health-check.sh** - Validation script:
```bash
#!/usr/bin/env bash
set -euo pipefail

echo "Checking API health..."
curl -fsS http://localhost:8080/actuator/health > /dev/null

echo "Checking Prometheus targets..."
curl -fsS http://localhost:9090/api/v1/targets | grep -q '"health":"up"'

echo "Checking Grafana..."
curl -fsS http://localhost:3000/login > /dev/null

echo "All services healthy! ✅"
```

### 7. README Quickstart Section
**README.md** - 5-step observability guide:
```markdown
## 🚀 Observability Quickstart

### 1. Start the Stack
```bash
docker compose -f docker-compose.observability.yml up -d
```

### 2. Verify Services
```bash
./scripts/health-check.sh
```

### 3. Check Metrics
- API: http://localhost:8080/actuator/prometheus
- Prometheus: http://localhost:9090/targets
- Grafana: http://localhost:3000 (admin/admin)

### 4. Run Performance Tests
```bash
# Linux
docker run --rm -e BASE_URL=http://api:8080 \
  --network host -v "$PWD/k6:/scripts" grafana/k6 run /scripts/load-test.js

# macOS/Windows
docker run --rm -e BASE_URL=http://host.docker.internal:8080 \
  -v "$PWD/k6:/scripts" grafana/k6 run /scripts/load-test.js
```

### 5. Monitor Performance
- Grafana → Import dashboards
- Prometheus → Query metrics
- Check SLOs: p95 < 500ms, error rate < 1%
```

## 🧪 Acceptance Criteria

### ✅ API Metrics
- [ ] `/actuator/prometheus` accessible from container
- [ ] Prometheus targets show `api:8080` as UP
- [ ] Metrics include: `http_server_requests_seconds_count`, `jvm_memory_used_bytes`

### ✅ Performance Testing
- [ ] k6 smoke test passes (p95 < 500ms, error rate < 1%)
- [ ] Both Linux and Windows commands work
- [ ] BASE_URL parameterization works

### ✅ Grafana Integration
- [ ] Prometheus datasource configured
- [ ] Dashboards importable
- [ ] Real-time metrics visible

### ✅ Health Checks
- [ ] All services respond to health checks
- [ ] Docker Compose dependencies work
- [ ] Script validation passes

## 🔍 Troubleshooting

### Metrics Not Working
- Check `management.server.address: 0.0.0.0`
- Verify Prometheus target: `api:8080` (not localhost)
- Test: `docker compose exec prometheus wget -qO- http://api:8080/actuator/prometheus`

### k6 Volume Issues
- Ensure `k6/load-test.js` exists
- Check volume mapping: `-v "$PWD/k6:/scripts"`
- Use correct BASE_URL for your OS

### Grafana Connection
- Verify datasource URL: `http://prometheus:9090`
- Check container networking
- Restart Grafana after datasource changes

## 📊 Expected Results

After PR#6:
- ✅ API metrics fully functional
- ✅ Performance testing working
- ✅ Grafana dashboards operational
- ✅ Complete observability stack
- ✅ Production-ready monitoring

## 🚀 Next Steps (PR#7+)
- Advanced Grafana dashboards
- Alerting rules
- Performance baselines
- CI/CD integration
- Production hardening