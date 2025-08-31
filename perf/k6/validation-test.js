import http from 'k6/http';
import { check, sleep } from 'k6';

// Test configuration for validation endpoints
export let options = {
  stages: [
    { duration: '1m', target: 20 },   // Ramp up to 20 users over 1 minute
    { duration: '3m', target: 20 },   // Stay at 20 users for 3 minutes
    { duration: '1m', target: 100 },  // Ramp up to 100 users over 1 minute
    { duration: '3m', target: 100 },  // Stay at 100 users for 3 minutes
    { duration: '1m', target: 0 },    // Ramp down to 0 users over 1 minute
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'], // 95% of requests should be below 300ms
    http_req_failed: ['rate<0.05'],   // Error rate should be below 5%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const patientId = '550e8400-e29b-41d4-a716-446655440000';

// Sample nutritional values for testing
const sampleNutrition = {
  calories: 450,
  protein: 25.0,
  phenylalanine: 1800,
  tyrosine: 2200,
  carbohydrate: 45.0,
  fat: 15.0,
  fiber: 3.5,
  sodium: 800,
  potassium: 600
};

export default function () {
  // Test nutritional validation
  let validationResponse = http.post(
    `${BASE_URL}/api/v1/validation/patient/${patientId}`,
    JSON.stringify(sampleNutrition),
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  check(validationResponse, {
    'validation status is 200': (r) => r.status === 200,
    'validation response time < 200ms': (r) => r.timings.duration < 200,
  });

  sleep(0.5);

  // Test meal validation
  let mealValidationResponse = http.post(
    `${BASE_URL}/api/v1/validation/patient/${patientId}/meal?mealPortion=0.25`,
    JSON.stringify(sampleNutrition),
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

  check(mealValidationResponse, {
    'meal validation status is 200': (r) => r.status === 200,
    'meal validation response time < 150ms': (r) => r.timings.duration < 150,
  });

  sleep(0.5);
}

export function setup() {
  console.log('Starting validation performance test');
  console.log(`Base URL: ${BASE_URL}`);

  // Health check
  let healthResponse = http.get(`${BASE_URL}/actuator/health`);
  if (healthResponse.status !== 200) {
    throw new Error(`Application is not healthy: ${healthResponse.status}`);
  }

  return { timestamp: new Date().toISOString() };
}

export function teardown(data) {
  console.log('Validation performance test completed');
  console.log(`Test started at: ${data.timestamp}`);
  console.log(`Test completed at: ${new Date().toISOString()}`);
}
