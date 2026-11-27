#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
loader="${1:-both}"
case "$loader" in
  both) task=build ;;
  fabric|neoforge) task=":$loader:build" ;;
  *) echo "Usage: $0 [both|fabric|neoforge]" >&2; exit 2 ;;
esac
python3 scripts/release_notes.py --check
bash ./gradlew "$task" --console=plain
if [[ "$loader" == both ]]; then
  env -u RELEASE_LOADER python3 scripts/verify_release.py
else
  RELEASE_LOADER="$loader" python3 scripts/verify_release.py
fi
