import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_PATH = __ENV.API_PATH || '/api/v1/products?size=5';

export const options = {
  vus: 5,
  duration: '1m',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
  },
};

const params = {
  timeout: '10s',
  headers: { 'Accept': 'application/json' },
};

export default function () {
  let url = `${BASE_URL}${API_PATH}`;
  let res = http.get(url, params);

  // simple retry for 429/503 (max 2 tries)
  let tries = 0;
  while ((res.status === 429 || res.status === 503) && tries < 2) {
    sleep(0.2 * (tries + 1));
    res = http.get(url, params);
    tries++;
  }

  check(res, {
    'status 200': (r) => r.status === 200,
    'json-ish': (r) => r.headers['Content-Type']?.includes('application/json'),
  });

  // Debug: log status codes
  if (res.status !== 200) {
    console.log(`Status: ${res.status}, Body: ${res.body.substring(0, 100)}`);
  }

  sleep(0.5);
}
