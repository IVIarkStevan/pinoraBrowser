#!/usr/bin/env bash
set -euo pipefail

# Robust Pinora Browser launcher for Linux
# - finds a suitable Java executable
# - warns if Java < 21 (recommended)
# - locates the built JAR in target/ and attempts to build via Maven if missing
# - tries a plain `java -jar` first, then falls back to assembling a JavaFX module-path

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Pinora Browser — launcher"

# Determine Java command
if [ -n "${PINORA_JAVA:-}" ]; then
     JAVA_CMD="$PINORA_JAVA"
elif [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
     JAVA_CMD="${JAVA_HOME}/bin/java"
else
     JAVA_CMD="$(command -v java || true)"
fi

if [ -z "$JAVA_CMD" ]; then
     cat <<EOF
Java not found.
Install OpenJDK 21 (or later) and ensure `java` is on your PATH, or set JAVA_HOME/PINORA_JAVA.
On Debian/Ubuntu: sudo apt install openjdk-21-jdk
EOF
     exit 1
fi

# Report Java version (best-effort parse)
ver="$($JAVA_CMD -version 2>&1 | awk -F\" 'NR==1{print $2}')"
major="$(echo "$ver" | awk -F. '{print $1}')"
if [ -z "$major" ]; then major=0; fi
if [ "$major" -lt 21 ]; then
     echo "Warning: Detected Java version: $ver — Java 21 or later is recommended. Continuing..."
else
     echo "Detected Java: $ver"
fi

# Locate JAR
JAR_FILE=""
JAR_FILE="$(ls "$DIR"/target/pinora-browser-*.jar 2>/dev/null | grep -v '\.original' | head -n1 || true)"
if [ -z "$JAR_FILE" ]; then
     echo "No built JAR found in $DIR/target."
     if command -v mvn >/dev/null 2>&1; then
          echo "Attempting to build with Maven (this may take a moment)..."
          (cd "$DIR" && mvn -DskipTests package) || { echo "Build failed — run 'mvn -DskipTests package' and check output."; exit 1; }
          JAR_FILE="$(ls "$DIR"/target/pinora-browser-*.jar 2>/dev/null | grep -v '\.original' | head -n1 || true)"
     fi
fi

if [ -z "$JAR_FILE" ]; then
     echo "Could not locate built JAR. Build the project and try again: mvn -DskipTests package"
     exit 1
fi

echo "Launching: $JAR_FILE"

# Set environment variables to fix Pango/GTK rendering issues on Linux
export GTK_DEBUG=""
export GDK_SCALE=1
export GDK_DPI_SCALE=1
# Use more stable input method to prevent Pango assertion errors
export GTK_IM_MODULE=fcitx

# Setup for JavaFX module-path requirement
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Try launching from target/dist with module-path
if [ -d "$DIR/target/dist" ] && [ -d "$DIR/target/dist/lib" ]; then
     echo "Found distribution in target/dist"
     JAR_FILE="$DIR/target/dist/pinora-browser-1.0.0.jar"
     LIB_PATH="$DIR/target/dist/lib"
     
     if [ -f "$JAR_FILE" ] && [ -f "$LIB_PATH/javafx-controls-21.jar" ]; then
          echo "Using module-path approach with extracted lib..."
          exec "$JAVA_CMD" \
               --module-path "$LIB_PATH" \
               --add-modules javafx.controls,javafx.web,javafx.fxml,javafx.graphics,javafx.media,javafx.base \
               -cp "$JAR_FILE" \
               com.pinora.browser.PinoraBrowser
     fi
fi

# Fallback: Try with original JAR if it has embedded lib
if [ -f "$JAR_FILE" ]; then
     echo "Attempting to launch original JAR with fallback..."
     # Extract to temp and use module-path
     TEMP_DIR=$(mktemp -d)
     trap "rm -rf $TEMP_DIR" EXIT
     cd "$TEMP_DIR"
     jar xf "$JAR_FILE" lib/ 2>/dev/null || true
     
     if [ -d "$TEMP_DIR/lib" ] && [ -f "$TEMP_DIR/lib/javafx-controls-21.jar" ]; then
          exec "$JAVA_CMD" \
               --module-path "$TEMP_DIR/lib" \
               --add-modules javafx.controls,javafx.web,javafx.fxml,javafx.graphics,javafx.media,javafx.base \
               -cp "$JAR_FILE" \
               com.pinora.browser.PinoraBrowser
     fi
fi

echo "Failed to launch: could not find JavaFX dependencies"
exit 1


