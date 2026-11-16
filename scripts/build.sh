#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
loader="${1:-both}"
case "$loader" in
  both) bash ./gradlew build --console=plain; python3 scripts/verify_release.py ;;
  fabric|neoforge) bash ./gradlew ":$loader:build" --console=plain; RELEASE_LOADER="$loader" python3 scripts/verify_release.py ;;
  *) echo "Usage: $0 [both|fabric|neoforge]" >&2; exit 2 ;;
esac
