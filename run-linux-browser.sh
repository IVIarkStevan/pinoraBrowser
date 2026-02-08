#!/usr/bin/env bash
set -euo pipefail

# Simple wrapper: delegate to the robust `pinora-browser.sh` launcher
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LAUNCHER="$DIR/pinora-browser.sh"

if [ ! -x "$LAUNCHER" ]; then
  echo "Launcher not executable. Attempting to set +x on $LAUNCHER"
  chmod +x "$LAUNCHER" || true
fi

exec "$LAUNCHER" "$@"
