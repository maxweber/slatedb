#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

docker build -t slatedb-java-builder -f "$SCRIPT_DIR/Dockerfile" "$REPO_ROOT"

exec docker run --rm \
  -e CLOJARS_USERNAME -e CLOJARS_PASSWORD \
  slatedb-java-builder "$@"
