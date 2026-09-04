#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
K8S_DIR="$PROJECT_DIR/k8s"

source "$SCRIPT_DIR/setup-env.sh"

SERVICES=("gateway" "auth" "patient" "clinical" "resource" "operations" "collaboration" "pharma" "cdss" "drg" "emergency" "platform")

DEPLOY_INFRA=true
DEPLOY_SERVICES=true
RESTART_SVC=""

usage() {
  cat <<'EOF'
Usage:
  deploy.sh                    Deploy infrastructure + all application services
  deploy.sh --infra-only       Deploy namespaces, config and infrastructure only
  deploy.sh --services-only    Deploy config + application services only
  deploy.sh --restart <svc>    Restart a single service (e.g. --restart pharma)
  deploy.sh --help             Show this help
EOF
}

while [ $# -gt 0 ]; do
  case "$1" in
    --infra-only)
      DEPLOY_SERVICES=false
      shift
      ;;
    --services-only)
      DEPLOY_INFRA=false
      shift
      ;;
    --restart)
      RESTART_SVC="${2:-}"
      if [ -z "$RESTART_SVC" ]; then
        echo "ERROR: --restart requires a service name (e.g. --restart pharma)"
        usage
        exit 1
      fi
      shift 2
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "ERROR: unknown option: $1"
      usage
      exit 1
      ;;
  esac
done

echo ""
echo "=== Smart HIS - Deploy to Kubernetes ==="

# Ensure we're targeting docker-desktop
kubectl config use-context docker-desktop 2>/dev/null || {
  echo "ERROR: docker-desktop context not found. Is Docker Desktop running with Kubernetes enabled?"
  exit 1
}

wait_for_ready() {
  local ns=$1
  local label=$2
  local timeout=${3:-300}
  echo "Waiting for $label in $ns (timeout ${timeout}s)..."
  kubectl wait --for=condition=ready pod -l "$label" -n "$ns" --timeout="${timeout}s" 2>/dev/null || true
}

# ---- Single service restart mode ----
if [ -n "$RESTART_SVC" ]; then
  echo ""
  echo "--- Restarting his-$RESTART_SVC ---"
  if [ -d "$K8S_DIR/services/$RESTART_SVC" ]; then
    kubectl apply -f "$K8S_DIR/services/$RESTART_SVC/deployment.yaml"
    kubectl apply -f "$K8S_DIR/services/$RESTART_SVC/service.yaml"
  fi
  kubectl rollout restart "deployment/his-$RESTART_SVC" -n his-app
  kubectl rollout status "deployment/his-$RESTART_SVC" -n his-app --timeout=300s || true
  kubectl get pods -n his-app -l "app=his-$RESTART_SVC"
  exit 0
fi

# Phase 1: Namespaces
echo ""
echo "--- Applying namespaces ---"
kubectl apply -f "$K8S_DIR/namespace.yaml"

# Phase 2: Shared application config (ConfigMap + Secret) - needed by all services
echo ""
echo "--- Applying shared app config ---"
kubectl apply -f "$K8S_DIR/config/his-app-configmap.yaml"
kubectl get secret his-app-secret -n his-app >/dev/null || {
  echo "ERROR: Missing his-app-secret. Create it from k8s/config/README.md before deploying."
  exit 1
}

if [ "$DEPLOY_INFRA" = true ]; then
  kubectl get secret his-infra-secret -n his-infra >/dev/null || {
    echo "ERROR: Missing his-infra-secret. Create it from k8s/config/README.md before deploying infrastructure."
    exit 1
  }
  # Phase 3: Infrastructure
  echo ""
  echo "--- Deploying PostgreSQL ---"
  kubectl apply -f "$K8S_DIR/infrastructure/postgresql/pvc.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/postgresql/configmap.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/postgresql/deployment.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/postgresql/service.yaml"
  wait_for_ready his-infra "app=postgresql" 120

  echo ""
  echo "--- Running PostgreSQL init job (idempotent schema bootstrap) ---"
  kubectl delete job postgresql-init -n his-infra --ignore-not-found=true
  kubectl apply -f "$K8S_DIR/infrastructure/postgresql/init-job.yaml"
  kubectl wait --for=condition=complete job/postgresql-init -n his-infra --timeout=120s || {
    echo "WARNING: postgresql-init job did not complete in time. Logs:"
    kubectl logs job/postgresql-init -n his-infra --tail=50 || true
  }

  echo ""
  echo "--- Deploying Nacos ---"
  kubectl apply -f "$K8S_DIR/infrastructure/nacos/deployment.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/nacos/service.yaml"
  wait_for_ready his-infra "app=nacos" 180

  echo ""
  echo "--- Deploying Redis ---"
  kubectl apply -f "$K8S_DIR/infrastructure/redis/configmap.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/redis/deployment.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/redis/service.yaml"
  wait_for_ready his-infra "app=redis" 60

  echo ""
  echo "--- Deploying Kafka ---"
  kubectl apply -f "$K8S_DIR/infrastructure/kafka/pvc.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/kafka/statefulset.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/kafka/service.yaml"
  wait_for_ready his-infra "app=kafka" 120

  echo ""
  echo "--- Deploying Monitoring ---"
  kubectl apply -f "$K8S_DIR/infrastructure/monitoring/prometheus-rbac.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/monitoring/prometheus-configmap.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/monitoring/prometheus-deployment.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/monitoring/prometheus-service.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/monitoring/grafana-configmap.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/monitoring/grafana-deployment.yaml"
  kubectl apply -f "$K8S_DIR/infrastructure/monitoring/grafana-service.yaml"

  echo ""
  echo "--- Infrastructure status ---"
  kubectl get pods -n his-infra
fi

if [ "$DEPLOY_SERVICES" = true ]; then
  # Phase 4: Application services
  echo ""
  echo "--- Deploying application services ---"
  for svc in "${SERVICES[@]}"; do
    if [ ! -d "$K8S_DIR/services/$svc" ]; then
      echo "Skipping his-$svc (no manifests yet)."
      continue
    fi
    echo "Deploying his-$svc..."
    kubectl apply -f "$K8S_DIR/services/$svc/deployment.yaml"
    kubectl apply -f "$K8S_DIR/services/$svc/service.yaml"
  done

  echo ""
  echo "Waiting for all services to become ready..."
  for svc in "${SERVICES[@]}"; do
    [ -d "$K8S_DIR/services/$svc" ] || continue
    wait_for_ready his-app "app=his-$svc" 180
  done
fi

echo ""
echo "=== Deployment complete ==="
echo ""
echo "--- his-infra pods ---"
kubectl get pods -n his-infra
echo ""
echo "--- his-app pods ---"
kubectl get pods -n his-app
echo ""
echo "--- Services ---"
kubectl get svc -n his-app
echo ""
echo "Access points:"
echo "  Gateway:  http://localhost:30080"
echo "  Nacos:    http://localhost:30848/nacos"
echo "  Grafana:  http://localhost:30300"
echo ""
echo "Run './scripts/verify.sh' for a read-only smoke test."
