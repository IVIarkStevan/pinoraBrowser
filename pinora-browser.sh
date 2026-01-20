#!/bin/bash

# Pinora Browser Launcher Script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Set JavaFX path
JAVAFX_PATH="/usr/share/openjfx/lib"

# Run with explicit module path
java --module-path "$JAVAFX_PATH" \
     --add-modules javafx.controls,javafx.web,javafx.fxml,javafx.graphics \
     -jar "$DIR/target/pinora-browser-1.0.0.jar"
