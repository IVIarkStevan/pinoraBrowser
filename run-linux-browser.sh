#!/bin/bash

# Pinora Browser Linux Launcher with Java 21 and Maven JavaFX
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAVA_HOME="${HOME}/.jdk/jdk-21.0.8"
JAR_FILE="${DIR}/target/pinora-browser-1.0.0-jar-with-dependencies.jar"
JAVAFX_REPO="${HOME}/.m2/repository/org/openjfx"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR not found at $JAR_FILE"
    echo "Please build the project first with: mvn clean compile assembly:single"
    exit 1
fi

# Build module path for JavaFX
MODULE_PATH=""
for jar in $(find "$JAVAFX_REPO" -name "javafx-*.jar" ! -name "*sources*" ! -name "*docs*" 2>/dev/null | sort); do
    if [ -z "$MODULE_PATH" ]; then
        MODULE_PATH="$jar"
    else
        MODULE_PATH="${MODULE_PATH}:${jar}"
    fi
done

# Launch with Java 21 and module path for JavaFX
exec "${JAVA_HOME}/bin/java" \
     --module-path "$MODULE_PATH" \
     --add-modules javafx.controls,javafx.web,javafx.fxml,javafx.graphics,javafx.base \
     -Dpinora.extensions.autoload=true \
     -cp "$JAR_FILE" \
     com.pinora.browser.PinoraBrowser
