# Performance Testing

This directory contains k6 performance tests for the PKU Diet App API.

## Prerequisites

1. Install k6: https://k6.io/docs/get-started/installation/
2. Start the application locally or have a test environment running

## Available Tests

### menu-generation-test.js
Tests the menu generation endpoints (daily and weekly).
- **Load Pattern**: Gradual ramp-up to simulate realistic usage
- **Endpoints Tested**:
  - `POST /api/v1/generator/daily`
  - `POST /api/v1/generator/weekly`
- **Performance Targets**:
  - Daily generation: < 2000ms (95th percentile)
  - Weekly generation: < 5000ms (95th percentile)
  - Error rate: < 10%

### validation-test.js
Tests the nutritional validation endpoints.
- **Load Pattern**: Higher concurrency to test validation performance
- **Endpoints Tested**:
  - `POST /api/v1/validation/patient/{id}`
  - `POST /api/v1/validation/patient/{id}/meal`
- **Performance Targets**:
  - Validation: < 200ms (95th percentile)
  - Meal validation: < 150ms (95th percentile)
  - Error rate: < 5%

## Running Tests

### Local Development
```bash
# Test against local application
k6 run perf/k6/menu-generation-test.js

# Test against local application with custom URL
k6 run -e BASE_URL=http://localhost:8080 perf/k6/validation-test.js
```

### Docker Environment
```bash
# Test against Docker Compose environment
k6 run -e BASE_URL=http://localhost:8080 perf/k6/menu-generation-test.js

# Run all tests
for test in perf/k6/*.js; do
  echo "Running $test..."
  k6 run "$test"
done
```

### CI/CD Integration
Tests can be integrated into CI/CD pipelines:

```yaml
- name: Run Performance Tests
  run: |
    npm install -g k6
    k6 run -e BASE_URL=http://your-test-environment perf/k6/menu-generation-test.js
```

## Test Configuration

### Environment Variables
- `BASE_URL`: Base URL of the API (default: `http://localhost:8080`)

### Modifying Load Patterns
Edit the `options.stages` array in each test file to adjust:
- `duration`: How long each stage runs
- `target`: Number of virtual users for that stage

### Customizing Thresholds
Modify the `options.thresholds` object to set different performance targets:
- `http_req_duration`: Response time thresholds
- `http_req_failed`: Error rate thresholds

## Interpreting Results

### Key Metrics
- **http_req_duration**: Response time distribution
- **http_req_failed**: Error rate
- **vus**: Virtual users (load)
- **iteration_duration**: Time per test iteration

### Performance Analysis
1. Check if 95th percentile response times meet targets
2. Monitor error rates during high load
3. Identify performance degradation points
4. Compare results across different environments

## Best Practices

1. **Test Realistic Scenarios**: Use load patterns that match expected usage
2. **Monitor System Resources**: Check CPU, memory, and database performance
3. **Run Regularly**: Include in CI/CD for continuous performance monitoring
4. **Baseline Performance**: Establish performance baselines for regression detection
5. **Scale Testing**: Test with different user loads and data volumes

## Troubleshooting

### Common Issues
- **Connection Refused**: Ensure application is running and accessible
- **High Error Rates**: Check application logs for errors during load
- **Slow Response Times**: Investigate database queries, caching, and resource usage

### Debug Mode
Run tests with verbose output:
```bash
k6 run --verbose perf/k6/menu-generation-test.js
```
