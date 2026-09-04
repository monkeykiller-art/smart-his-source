#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

source "$SCRIPT_DIR/setup-env.sh"

SERVICES=("his-gateway" "his-auth" "his-patient" "his-clinical" "his-resource" "his-operations" "his-collaboration" "his-pharma" "his-cdss" "his-drg" "his-emergency" "his-platform")

usage() {
  cat <<'EOF'
Usage:
  build-images.sh                 Build all modules and all Docker images
  build-images.sh --module <mod>  Rebuild a single module + its image (e.g. his-pharma)
  build-images.sh --help          Show this help
EOF
}

# ---- Single module mode ----
if [ "${1:-}" = "--module" ]; then
  MODULE="${2:-}"
  if [ -z "$MODULE" ]; then
    echo "ERROR: --module requires a module name (e.g. --module his-pharma)"
    usage
    exit 1
  fi
  if [ ! -d "$PROJECT_DIR/$MODULE" ]; then
    echo "ERROR: module directory not found: $PROJECT_DIR/$MODULE"
    exit 1
  fi

  echo ""
  echo "=== Smart HIS - Build single module: $MODULE ==="
  cd "$PROJECT_DIR"
  mvn -pl "$MODULE" -am clean package -DskipTests -q
  echo "Maven build complete for $MODULE."

  if [ -f "$PROJECT_DIR/$MODULE/Dockerfile" ]; then
    echo "Building Docker image smart-his/$MODULE:latest ..."
    docker build -t "smart-his/$MODULE:latest" "$PROJECT_DIR/$MODULE"
    docker images "smart-his/$MODULE" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
  else
    echo "No Dockerfile in $MODULE (library module?) - skipping image build."
  fi
  exit 0
fi

if [ "${1:-}" = "--help" ] || [ "${1:-}" = "-h" ]; then
  usage
  exit 0
fi

echo ""
echo "=== Smart HIS - Build Docker Images ==="

# Step 1: Maven build
echo ""
echo "--- Building JARs ---"
cd "$PROJECT_DIR"
mvn clean package -DskipTests -q
echo "Maven build complete."

# Step 2: Docker build for each service
echo ""
echo "--- Building Docker images ---"
for svc in "${SERVICES[@]}"; do
  if [ ! -f "$PROJECT_DIR/$svc/Dockerfile" ]; then
    echo "Skipping $svc (no Dockerfile)."
    continue
  fi
  echo "Building $svc..."
  docker build -t "smart-his/$svc:latest" "$PROJECT_DIR/$svc"
done

echo ""
echo "All images built:"
docker images "smart-his/*" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
