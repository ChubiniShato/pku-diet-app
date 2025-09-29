#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

# Health check
curl -fsS "${BASE_URL}/actuator/health" >/dev/null

# CORS header check
curl -fsS -H "Origin: https://app.example.com" -I "${BASE_URL}/some-public-endpoint" | grep -i "access-control-allow-origin" >/dev/null

# CSP header check
curl -fsS -I "${BASE_URL}/" | grep -i "content-security-policy" >/dev/null

echo "SMOKE OK"

