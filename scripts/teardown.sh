#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
K8S_DIR="$PROJECT_DIR/k8s"

source "$SCRIPT_DIR/setup-env.sh"

echo ""
echo "=== Smart HIS - Teardown ==="
echo "This will delete all Smart HIS resources from Kubernetes."
read -p "Continue? [y/N] " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo "Aborted."
  exit 0
fi

echo ""
echo "--- Deleting application services ---"
SERVICES=("gateway" "auth" "patient" "clinical" "resource" "operations" "collaboration")
for svc in "${SERVICES[@]}"; do
  kubectl delete -f "$K8S_DIR/services/$svc/service.yaml" --ignore-not-found
  kubectl delete -f "$K8S_DIR/services/$svc/deployment.yaml" --ignore-not-found
done

echo ""
echo "--- Deleting infrastructure ---"
kubectl delete -f "$K8S_DIR/infrastructure/monitoring/" --ignore-not-found
kubectl delete -f "$K8S_DIR/infrastructure/kafka/" --ignore-not-found
kubectl delete -f "$K8S_DIR/infrastructure/redis/" --ignore-not-found
kubectl delete -f "$K8S_DIR/infrastructure/nacos/" --ignore-not-found
kubectl delete -f "$K8S_DIR/infrastructure/postgresql/" --ignore-not-found

echo ""
echo "--- Deleting namespaces ---"
kubectl delete namespace his-app --ignore-not-found
kubectl delete namespace his-infra --ignore-not-found

echo ""
echo "Teardown complete."
