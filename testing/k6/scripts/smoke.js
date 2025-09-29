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

