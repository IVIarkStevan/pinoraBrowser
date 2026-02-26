#!/bin/bash

# Pinora Browser Launcher for Linux with Pango/GTK Fixes
# This script sets up the environment to prevent Pango text rendering crashes

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Find the JAR file
JAR_FILE="$SCRIPT_DIR/target/pinora-browser-1.0.0.jar"

# If JAR doesn't exist in target, check if it's in the current directory
if [ ! -f "$JAR_FILE" ]; then
    JAR_FILE="$SCRIPT_DIR/pinora-browser-1.0.0.jar"
fi

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    exit 1
fi

echo "Launching Pinora Browser..."
echo "JAR: $JAR_FILE"

# Set environment variables to fix Pango/GTK rendering issues on Linux
export GTK_DEBUG=""
export GDK_SCALE=1
export GDK_DPI_SCALE=1

# Disable problematic GTK features that might cause Pango errors
export GTK_IM_MODULE=fcitx  # Use more stable input method

# Set Java options for JavaFX
# Disable hardware acceleration if it causes issues
# JAVA_OPTS="${JAVA_OPTS} -Dprism.order=sw"  # Uncomment if still having rendering issues

# Run the browser
java ${JAVA_OPTS} -Xmx2g -jar "$JAR_FILE"
