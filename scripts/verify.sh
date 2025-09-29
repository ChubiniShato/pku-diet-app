#!/usr/bin/env bash
set -euo pipefail

API_URL="${API_URL:-http://localhost:8080}"

echo "1) /actuator/health (full)"
curl -sf "${API_URL}/actuator/health" | jq .

echo "2) readiness & liveness (must NOT be 403)"
curl -si "${API_URL}/actuator/health/readiness" | head -n 1
curl -sf "${API_URL}/actuator/health/readiness" | jq .

curl -si "${API_URL}/actuator/health/liveness" | head -n 1
curl -sf "${API_URL}/actuator/health/liveness" | jq .

echo "3) info & prometheus reachability"
curl -si "${API_URL}/actuator/info" | head -n 1
curl -si "${API_URL}/actuator/prometheus" | head -n 1

echo "4) confirm Redis host picked up (only if env/configprops temporarily exposed)"
if curl -sf "${API_URL}/actuator/env" >/dev/null 2>&1; then
  curl -sf "${API_URL}/actuator/env" \
    | jq '.propertySources[]? | select(.name|tostring|test("application-docker")) \
    | .properties."spring.data.redis.host".value'
fi

echo "OK"


