#!/bin/bash

# PKU Diet App Performance Tests Runner
# Usage: ./run-perf-tests.sh [BASE_URL] [TEST_PATTERN]

set -e

# Default values
BASE_URL="${1:-http://localhost:8080}"
TEST_PATTERN="${2:-*.js}"

echo "🚀 Starting PKU Diet App Performance Tests"
echo "📍 Base URL: $BASE_URL"
echo "🎯 Test Pattern: $TEST_PATTERN"
echo "📊 Timestamp: $(date)"
echo

# Check if k6 is installed
if ! command -v k6 &> /dev/null; then
    echo "❌ k6 is not installed. Please install k6 first:"
    echo "   https://k6.io/docs/get-started/installation/"
    exit 1
fi

# Check if BASE_URL is accessible
echo "🔍 Checking application health..."
if ! curl -f -s "$BASE_URL/actuator/health" > /dev/null; then
    echo "❌ Application is not healthy at $BASE_URL"
    echo "   Make sure the application is running and accessible"
    exit 1
fi
echo "✅ Application is healthy"
echo

# Find and run test files
TEST_FILES=$(find perf/k6 -name "$TEST_PATTERN" -type f)

if [ -z "$TEST_FILES" ]; then
    echo "❌ No test files found matching pattern: $TEST_PATTERN"
    exit 1
fi

# Run each test file
for test_file in $TEST_FILES; do
    echo "🏃 Running test: $test_file"
    echo "────────────────────────────────────────"

    # Run the test with environment variable
    if k6 run -e BASE_URL="$BASE_URL" "$test_file"; then
        echo "✅ Test completed successfully: $test_file"
    else
        echo "❌ Test failed: $test_file"
        exit 1
    fi

    echo
done

echo "🎉 All performance tests completed successfully!"
echo "📊 Check the output above for detailed results"
echo "💡 Use 'k6 run --out json=results.json <test-file>' to save results"
