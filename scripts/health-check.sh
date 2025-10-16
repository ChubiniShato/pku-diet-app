#!/usr/bin/env bash
set -euo pipefail

echo "🔍 Checking PKU Diet App Observability Stack..."

echo "✅ Checking API health..."
curl -fsS http://localhost:8080/actuator/health > /dev/null
echo "   API is healthy"

echo "✅ Checking Prometheus targets..."
curl -fsS http://localhost:9090/api/v1/targets | grep -q '"health":"up"'
echo "   Prometheus targets are UP"

echo "✅ Checking Grafana..."
curl -fsS http://localhost:3000/login > /dev/null
echo "   Grafana is accessible"

echo "✅ Checking metrics endpoint..."
curl -fsS http://localhost:8080/actuator/prometheus | grep -q "http_server_requests_seconds_count"
echo "   Metrics endpoint is working"

echo ""
echo "🎉 All services healthy! ✅"
echo ""
echo "📊 Access URLs:"
echo "   API: http://localhost:8080"
echo "   Prometheus: http://localhost:9090"
echo "   Grafana: http://localhost:3000 (admin/admin)"
echo "   Metrics: http://localhost:8080/actuator/prometheus"
