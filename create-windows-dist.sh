#!/bin/bash

# Create Windows distribution package for Pinora Browser

echo "=========================================="
echo "Pinora Browser - Windows Distribution"
echo "=========================================="

PROJECT_DIR="/home/stiler/Documents/code/devProject/pinoraBrowser"
DIST_DIR="$PROJECT_DIR/dist/windows"
PACKAGE_NAME="pinora-browser-windows"

# Create distribution directory
mkdir -p "$DIST_DIR/$PACKAGE_NAME"

echo "Building package..."

# Copy essential files
cp "$PROJECT_DIR/pinora-browser.bat" "$DIST_DIR/$PACKAGE_NAME/"
cp "$PROJECT_DIR/pinora-browser.ps1" "$DIST_DIR/$PACKAGE_NAME/"
cp "$PROJECT_DIR/WINDOWS_GUIDE.md" "$DIST_DIR/$PACKAGE_NAME/README.md"
cp "$PROJECT_DIR/README.md" "$DIST_DIR/$PACKAGE_NAME/README_PROJECT.md"

# Copy target folder with JAR
mkdir -p "$DIST_DIR/$PACKAGE_NAME/target"
cp "$PROJECT_DIR/target/pinora-browser-1.0.0.jar" "$DIST_DIR/$PACKAGE_NAME/target/"

# Copy source code
mkdir -p "$DIST_DIR/$PACKAGE_NAME/src"
cp -r "$PROJECT_DIR/src/main" "$DIST_DIR/$PACKAGE_NAME/src/"

# Copy configuration files
cp "$PROJECT_DIR/pom.xml" "$DIST_DIR/$PACKAGE_NAME/"
cp "$PROJECT_DIR/.gitignore" "$DIST_DIR/$PACKAGE_NAME/"

# Create a Windows execution script
cat > "$DIST_DIR/$PACKAGE_NAME/RUN.bat" << 'EOF'
@echo off
echo.
echo   ╔════════════════════════════════════════╗
echo   ║     PINORA BROWSER - Windows        ║
echo   ║     Starting application...          ║
echo   ╚════════════════════════════════════════╝
echo.

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo.
    echo Please install Java 17 or higher from:
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Run Pinora Browser
call pinora-browser.bat

EOF

chmod +x "$DIST_DIR/$PACKAGE_NAME/RUN.bat"

# Create ZIP package
cd "$DIST_DIR"
if command -v zip &> /dev/null; then
    zip -r "$PACKAGE_NAME.zip" "$PACKAGE_NAME" > /dev/null
    echo "✓ Created ZIP: $DIST_DIR/$PACKAGE_NAME.zip"
else
    tar -czf "$PACKAGE_NAME.tar.gz" "$PACKAGE_NAME"
    echo "✓ Created TAR.GZ: $DIST_DIR/$PACKAGE_NAME.tar.gz"
fi

echo ""
echo "=========================================="
echo "✓ Windows distribution package created!"
echo "=========================================="
echo ""
echo "Location: $DIST_DIR/$PACKAGE_NAME"
echo ""
echo "How to use on Windows:"
echo "1. Extract the ZIP file"
echo "2. Run: RUN.bat"
echo "   OR"
echo "   Double-click: pinora-browser.bat"
echo ""
echo "How to test on Linux with Wine:"
echo "1. Install Wine: sudo apt install wine"
echo "2. Extract the package"
echo "3. Run: wine RUN.bat"
echo ""

