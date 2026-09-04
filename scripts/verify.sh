#!/bin/bash
# Smart HIS - read-only smoke test.
# Performs NO mutations: only kubectl get / describe-free reads and HTTP GETs.
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

GATEWAY_URL="${GATEWAY_URL:-http://localhost:30080}"
NACOS_URL="${NACOS_URL:-http://localhost:30848}"
NACOS_USER="${NACOS_USER:?Set NACOS_USER before running verification}"
NACOS_PASS="${NACOS_PASS:?Set NACOS_PASS before running verification}"

MODULES=("auth" "patient" "clinical" "resource" "operations" "collaboration" "pharma" "cdss" "drg" "emergency" "platform")

PASS=0
FAIL=0

ok()   { echo "  [ OK ]  $1"; PASS=$((PASS + 1)); }
bad()  { echo "  [FAIL]  $1"; FAIL=$((FAIL + 1)); }

echo ""
echo "=== Smart HIS - Verification (read-only) ==="

# ---------------------------------------------------------------
echo ""
echo "--- 1. Pod status ---"
if ! command -v kubectl >/dev/null 2>&1; then
  echo "  kubectl not found on PATH; skipping cluster checks."
else
  for ns in his-infra his-app; do
    echo ""
    echo "  Namespace: $ns"
    kubectl get pods -n "$ns" -o wide 2>/dev/null || echo "  (namespace $ns not found)"
  done

  echo ""
  echo "  Pods not in Running/Succeeded state:"
  NOT_READY=$(kubectl get pods -A --no-headers 2>/dev/null \
    | awk '$1 ~ /^his-/ && $4 != "Running" && $4 != "Completed" { print "    " $1 "/" $2 " -> " $4 }')
  if [ -z "$NOT_READY" ]; then
    ok "All his-* pods are Running/Completed"
  else
    echo "$NOT_READY"
    bad "Some his-* pods are not healthy"
  fi

  echo ""
  echo "  Containers reporting not-ready:"
  UNREADY=$(kubectl get pods -n his-app --no-headers 2>/dev/null \
    | awk '{ split($2, r, "/"); if (r[1] != r[2]) print "    " $1 " -> " $2 }')
  if [ -z "$UNREADY" ]; then
    ok "All his-app containers pass readiness"
  else
    echo "$UNREADY"
    bad "Some his-app containers are not ready"
  fi
fi

# ---------------------------------------------------------------
echo ""
echo "--- 2. Nacos registered instances ---"
NACOS_TOKEN=""
LOGIN_RESP=$(curl -s -m 10 -X POST "$NACOS_URL/nacos/v1/auth/login" \
  -d "username=$NACOS_USER&password=$NACOS_PASS" 2>/dev/null)
if echo "$LOGIN_RESP" | grep -q "accessToken"; then
  NACOS_TOKEN=$(echo "$LOGIN_RESP" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
  ok "Nacos login succeeded"
else
  echo "  (Nacos auth login failed or auth disabled; trying unauthenticated)"
fi

if [ -n "$NACOS_TOKEN" ]; then
  SVC_LIST=$(curl -s -m 10 "$NACOS_URL/nacos/v1/ns/service/list?pageNo=1&pageSize=100&accessToken=$NACOS_TOKEN" 2>/dev/null)
else
  SVC_LIST=$(curl -s -m 10 "$NACOS_URL/nacos/v1/ns/service/list?pageNo=1&pageSize=100" 2>/dev/null)
fi

if [ -z "$SVC_LIST" ]; then
  bad "Could not reach Nacos at $NACOS_URL"
else
  SVC_COUNT=$(echo "$SVC_LIST" | sed -n 's/.*"count":\([0-9]*\).*/\1/p')
  SVC_NAMES=$(echo "$SVC_LIST" | tr ',' '\n' | sed -n 's/.*"\(his-[a-z]*\)".*/\1/p' | sort -u)
  echo "  Registered service count: ${SVC_COUNT:-unknown}"
  if [ -n "$SVC_NAMES" ]; then
    echo "  Registered services:"
    echo "$SVC_NAMES" | sed 's/^/    /'
  fi
  if [ -n "${SVC_COUNT:-}" ] && [ "${SVC_COUNT:-0}" -gt 0 ] 2>/dev/null; then
    ok "Nacos reports $SVC_COUNT registered service(s)"
  else
    bad "Nacos reports no registered services"
  fi
fi

# ---------------------------------------------------------------
echo ""
echo "--- 3. Gateway routed health endpoints ---"
echo "  Base: $GATEWAY_URL"

GW_CODE=$(curl -s -o /dev/null -w "%{http_code}" -m 10 "$GATEWAY_URL/actuator/health" 2>/dev/null)
if [ "$GW_CODE" = "200" ]; then
  ok "Gateway /actuator/health -> 200"
else
  bad "Gateway /actuator/health -> ${GW_CODE:-no-response}"
fi

for mod in "${MODULES[@]}"; do
  URL="$GATEWAY_URL/api/$mod/health"
  CODE=$(curl -s -o /dev/null -w "%{http_code}" -m 10 "$URL" 2>/dev/null)
  if [ "$CODE" = "200" ]; then
    ok "GET /api/$mod/health -> 200"
  else
    bad "GET /api/$mod/health -> ${CODE:-no-response}"
  fi
done

# ---------------------------------------------------------------
echo ""
echo "=== Summary ==="
echo "  Passed: $PASS"
echo "  Failed: $FAIL"
echo ""
if [ "$FAIL" -eq 0 ]; then
  echo "All checks passed."
  exit 0
fi
echo "Some checks failed. Inspect with:"
echo "  kubectl get pods -n his-app"
echo "  kubectl logs deployment/his-<svc> -n his-app --tail=100"
exit 1
