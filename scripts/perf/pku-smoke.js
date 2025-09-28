import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 5,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% under 500ms
    http_req_failed: ['rate<0.01']    // Error rate under 1%
  }
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // Test products API
  const productsRes = http.get(`${BASE}/api/v1/products?pheMax=200&size=10`);
  check(productsRes, { 
    'products status 200': r => r.status === 200,
    'products response time < 500ms': r => r.timings.duration < 500
  });

  // Test health endpoint
  const healthRes = http.get(`${BASE}/actuator/health`);
  check(healthRes, { 
    'health status 200': r => r.status === 200,
    'health response time < 200ms': r => r.timings.duration < 200
  });

  // Test metrics endpoint
  const metricsRes = http.get(`${BASE}/actuator/prometheus`);
  check(metricsRes, { 
    'metrics status 200': r => r.status === 200,
    'metrics contains pku metrics': r => r.body.includes('pku_')
  });

  sleep(1);
}
