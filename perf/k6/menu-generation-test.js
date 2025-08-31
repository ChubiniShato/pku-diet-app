import http from 'k6/http';
import { check, sleep } from 'k6';

// Test configuration
export let options = {
  stages: [
    { duration: '2m', target: 10 },   // Ramp up to 10 users over 2 minutes
    { duration: '5m', target: 10 },   // Stay at 10 users for 5 minutes
    { duration: '2m', target: 50 },   // Ramp up to 50 users over 2 minutes
    { duration: '5m', target: 50 },   // Stay at 50 users for 5 minutes
    { duration: '2m', target: 0 },    // Ramp down to 0 users over 2 minutes
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    http_req_failed: ['rate<0.1'],    // Error rate should be below 10%
  },
};

// Base URL - can be overridden via environment variable
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Test data
const patientId = '550e8400-e29b-41d4-a716-446655440000';

export default function () {
  // Generate daily menu
  let dailyPayload = {
    patientId: patientId,
    startDate: new Date().toISOString().split('T')[0],
    usePantry: true,
    varietyWeight: 0.8,
    budgetWeight: 0.7,
    preferenceWeight: 0.9
  };

  let dailyResponse = http.post(
    `${BASE_URL}/api/v1/generator/daily`,
    JSON.stringify(dailyPayload),
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  check(dailyResponse, {
    'daily menu generation status is 200': (r) => r.status === 200,
    'daily menu generation response time < 2000ms': (r) => r.timings.duration < 2000,
    'daily menu generation has success flag': (r) => r.json().success === true,
  });

  sleep(1);

  // Generate weekly menu
  let weeklyPayload = {
    patientId: patientId,
    startDate: new Date().toISOString().split('T')[0],
    endDate: new Date(Date.now() + 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    budget: 100.0,
    currency: 'EUR',
    usePantry: true,
    varietyWeight: 0.8,
    budgetWeight: 0.7,
    preferenceWeight: 0.9
  };

  let weeklyResponse = http.post(
    `${BASE_URL}/api/v1/generator/weekly`,
    JSON.stringify(weeklyPayload),
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  check(weeklyResponse, {
    'weekly menu generation status is 200': (r) => r.status === 200,
    'weekly menu generation response time < 5000ms': (r) => r.timings.duration < 5000,
    'weekly menu generation has success flag': (r) => r.json().success === true,
  });

  sleep(2);
}

// Setup function - runs before the test starts
export function setup() {
  console.log('Starting performance test for PKU Diet App');
  console.log(`Base URL: ${BASE_URL}`);

  // Health check
  let healthResponse = http.get(`${BASE_URL}/actuator/health`);
  if (healthResponse.status !== 200) {
    throw new Error(`Application is not healthy: ${healthResponse.status}`);
  }

  return { timestamp: new Date().toISOString() };
}

// Teardown function - runs after the test completes
export function teardown(data) {
  console.log('Performance test completed');
  console.log(`Test started at: ${data.timestamp}`);
  console.log(`Test completed at: ${new Date().toISOString()}`);
}