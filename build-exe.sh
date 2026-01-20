#!/bin/bash

# Create Windows EXE installer using jpackage
# This script should be run on Windows or with cross-platform jpackage

echo "=========================================="
echo "Creating Windows EXE Installer"
echo "=========================================="

# First, build the jar
echo "Building JAR..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "JAR build failed."
    exit 1
fi

# Create necessary directories
mkdir -p jpackage-output

# Check if jpackage is available
if ! command -v jpackage &> /dev/null; then
    echo "jpackage not found. It's included with Java 16+"
    echo "Make sure your JAVA_HOME is set correctly."
    exit 1
fi

echo "Creating Windows EXE installer..."

jpackage \
    --input "target" \
    --name "PinoraBrowser" \
    --main-jar "pinora-browser-1.0.0-jar-with-dependencies.jar" \
    --main-class "com.pinora.browser.PinoraBrowser" \
    --type exe \
    --vendor "Pinora" \
    --description "A lightweight, minimal yet feature-rich web browser" \
    --version "1.0.0" \
    --dest "jpackage-output" \
    --app-version "1.0.0" \
    --java-options "-Xmx1024m"

if [ $? -eq 0 ]; then
    echo "=========================================="
    echo "EXE creation successful!"
    echo "Installer: jpackage-output/PinoraBrowser-1.0.0.exe"
    echo "=========================================="
else
    echo "EXE creation failed. Make sure you're on Windows or have cross-compilation tools."
    exit 1
fi
