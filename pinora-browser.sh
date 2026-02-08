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

# Try simple jar launch first
if "$JAVA_CMD" -jar "$JAR_FILE"; then
     exit 0
fi

echo "Plain -jar launch failed; attempting JavaFX module-path fallback..."

# Build module path from common locations (m2 and system openjfx)
MODULE_PATHS=""
CANDIDATES=("$HOME/.m2/repository/org/openjfx" "/usr/share/openjfx/lib" "/usr/lib/jvm")
for base in "${CANDIDATES[@]}"; do
     if [ -d "$base" ]; then
          while IFS= read -r jar; do
                    MODULE_PATHS="$MODULE_PATHS:$jar"
               done < <(find "$base" -name "javafx-*.jar" ! -name "*sources*" ! -name "*javadoc*" ! -name "*docs*" 2>/dev/null | sort)
     fi
done
MODULE_PATHS="${MODULE_PATHS#:}"

if [ -n "$MODULE_PATHS" ]; then
     exec "$JAVA_CMD" --module-path "$MODULE_PATHS" --add-modules javafx.controls,javafx.web,javafx.fxml,javafx.graphics -jar "$JAR_FILE"
else
     echo "Could not locate JavaFX jars automatically. If the JAR doesn't bundle JavaFX, install OpenJFX or set PINORA_JAVAFX to the path containing javafx-*.jar files."
     exit 1
fi
