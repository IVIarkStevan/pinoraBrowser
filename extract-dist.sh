#!/bin/bash

# Extract Pinora Browser distribution from JAR
# This script extracts the lib folder from the packaged JAR

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILE="$SCRIPT_DIR/target/pinora-browser-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    exit 1
fi

echo "Extracting distribution files from JAR..."

# Extract lib folder to target/dist
mkdir -p "$SCRIPT_DIR/target/dist/lib"

# Extract lib files from JAR
cd "$SCRIPT_DIR/target/dist"
jar xf "$JAR_FILE" lib/

# Copy the JAR itself
cp "$JAR_FILE" "$SCRIPT_DIR/target/dist/"

echo "Distribution created at: $SCRIPT_DIR/target/dist"
echo "Contents:"
ls -lh "$SCRIPT_DIR/target/dist/"
ls -1 "$SCRIPT_DIR/target/dist/lib/" | head -10

echo ""
echo "To run the browser from dist:"
echo "  cd $SCRIPT_DIR/target/dist"
echo "  java -cp 'pinora-browser-1.0.0.jar:lib/*' com.pinora.browser.PinoraBrowser"
