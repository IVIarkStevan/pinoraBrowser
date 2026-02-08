#!/bin/bash
# Simple launcher to test the browser with extension UI

JAVAFX_PATH="${HOME}/.m2/repository/org/openjfx"
MODULE_PATH=""

# Find all javafx module JARs
for jar in $(find "$JAVAFX_PATH" -name "javafx-*.jar" ! -name "*sources*" ! -name "*docs*" | sort); do
    MODULE_PATH="${MODULE_PATH}:${jar}"
done

# Remove leading colon
MODULE_PATH="${MODULE_PATH:1}"

# Launch with module path
java --module-path "$MODULE_PATH" \
     --add-modules javafx.controls,javafx.web,javafx.fxml,javafx.graphics,javafx.base \
     -Dpinora.extensions.autoload=false \
     -cp "target/pinora-browser-1.0.0.jar" \
     com.pinora.browser.test.OpenExtensionsLauncher
