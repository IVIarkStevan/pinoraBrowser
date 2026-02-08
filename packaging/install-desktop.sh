#!/usr/bin/env bash
set -euo pipefail

# Installer for the Pinora Browser desktop entry
# Usage: ./install-desktop.sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
LAUNCHER="$DIR/pinora-browser.sh"
DESKTOP_SRC="$DIR/packaging/pinora-browser.desktop"
DESKTOP_DEST="$HOME/.local/share/applications/pinora-browser.desktop"

echo "Installing Pinora Browser desktop entry..."

if [ ! -f "$LAUNCHER" ]; then
  echo "Error: launcher not found at $LAUNCHER"
  exit 1
fi

# Find an icon in resources/icons
ICON_SRC="$(find "$DIR" -type f -path '*/resources/icons/*' \( -iname '*.png' -o -iname '*.svg' \) | head -n1 || true)"
ICON_DIR="$HOME/.local/share/icons/hicolor/128x128/apps"

mkdir -p "$(dirname "$DESKTOP_DEST")" "$ICON_DIR"

if [ -n "$ICON_SRC" ]; then
  echo "Found icon: $ICON_SRC — installing to $ICON_DIR/pinora-browser.png"
  cp "$ICON_SRC" "$ICON_DIR/pinora-browser.png"
  ICON_INSTALLED=true
else
  ICON_INSTALLED=false
fi

echo "Writing desktop file to $DESKTOP_DEST"
sed "s|__EXEC__|$LAUNCHER|g" "$DESKTOP_SRC" > "$DESKTOP_DEST"

if [ "$ICON_INSTALLED" = true ]; then
  sed -i "s|^Icon=.*|Icon=pinora-browser|" "$DESKTOP_DEST"
fi

chmod 644 "$DESKTOP_DEST"
update-desktop-database --user 2>/dev/null || true

echo "Installed. You can find Pinora Browser in your desktop application menu."
echo "To uninstall: rm '$DESKTOP_DEST' && rm '$ICON_DIR/pinora-browser.png'"
