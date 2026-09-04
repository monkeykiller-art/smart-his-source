#!/bin/bash
set -euo pipefail

echo "=== Smart HIS - Environment Setup ==="

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# Detect and set JAVA_HOME
if [ -d "/c/Program Files/Eclipse Adoptium" ]; then
  JDK_DIR=$(ls -d /c/Program\ Files/Eclipse\ Adoptium/jdk-21* 2>/dev/null | head -1)
  if [ -n "$JDK_DIR" ]; then
    export JAVA_HOME="$JDK_DIR"
    echo "Found JDK 21: $JAVA_HOME"
  fi
fi

if [ -z "${JAVA_HOME:-}" ]; then
  echo "ERROR: JDK 21 not found. Install with: winget install EclipseAdoptium.Temurin.21.JDK"
  exit 1
fi

# Detect Maven
if [ -d "/c/project4/tools/apache-maven-3.9.16" ]; then
  export MAVEN_HOME="/c/project4/tools/apache-maven-3.9.16"
elif [ -n "${MAVEN_HOME:-}" ]; then
  echo "Using MAVEN_HOME: $MAVEN_HOME"
else
  echo "ERROR: Maven not found. Set MAVEN_HOME manually."
  exit 1
fi

export MAVEN_OPTS="-Dfile.encoding=UTF-8 -Xmx2g"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

echo "Java: $(java -version 2>&1 | head -1)"
echo "Maven: $(mvn -version 2>&1 | head -1)"

# Verify Docker
if command -v docker &>/dev/null; then
  echo "Docker: $(docker --version)"
else
  DOCKER_PATH="/c/Program Files/Docker/Docker/resources/bin/docker"
  if [ -x "$DOCKER_PATH" ]; then
    export PATH="$PATH:/c/Program Files/Docker/Docker/resources/bin"
    echo "Docker: $(docker --version)"
  else
    echo "WARNING: Docker not found on PATH"
  fi
fi

# Verify kubectl
if command -v kubectl &>/dev/null; then
  echo "kubectl: $(kubectl version --client --short 2>/dev/null || kubectl version --client 2>&1 | head -1)"
else
  KUBECTL_PATH="/c/Program Files/Docker/Docker/resources/bin/kubectl"
  if [ -x "$KUBECTL_PATH" ]; then
    export PATH="$PATH:/c/Program Files/Docker/Docker/resources/bin"
    echo "kubectl: $(kubectl version --client 2>&1 | head -1)"
  else
    echo "WARNING: kubectl not found on PATH"
  fi
fi

echo ""
echo "Environment ready."
