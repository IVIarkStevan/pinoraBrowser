#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
DIST_DIR="$DIR/target/pinora-browser-1.0.0/pinora-browser-1.0.0"

# If distribution exists, use it; otherwise use target directory
if [ -d "$DIST_DIR" ]; then
  cd "$DIST_DIR"
  java --module-path lib --add-modules javafx.controls,javafx.web,javafx.graphics,javafx.fxml -jar pinora-browser-1.0.0.jar
else
  cd "$DIR"
  # Fallback for running from project root
  java --module-path target/pinora-browser-1.0.0/pinora-browser-1.0.0/lib --add-modules javafx.controls,javafx.web,javafx.graphics,javafx.fxml -jar target/pinora-browser-1.0.0.jar
fi
