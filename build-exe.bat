@echo off
REM Create Windows EXE installer using jpackage
REM This script should be run on Windows with Java 16+

setlocal enabledelayedexpansion

echo ==========================================
echo Creating Windows EXE Installer
echo ==========================================

REM First, build the jar
echo Building JAR...
call mvn clean package -DskipTests

if !errorlevel! neq 0 (
    echo JAR build failed.
    exit /b 1
)

REM Create necessary directories
if not exist "jpackage-output" mkdir jpackage-output

REM Check if jpackage is available
where jpackage >nul 2>nul
if !errorlevel! neq 0 (
    echo jpackage not found. It's included with Java 16+
    echo Make sure your JAVA_HOME is set correctly.
    exit /b 1
)

echo Creating Windows EXE installer...

jpackage ^
    --input "target" ^
    --name "PinoraBrowser" ^
    --main-jar "pinora-browser-1.0.0-jar-with-dependencies.jar" ^
    --main-class "com.pinora.browser.PinoraBrowser" ^
    --type exe ^
    --vendor "Pinora" ^
    --description "A lightweight, minimal yet feature-rich web browser" ^
    --version "1.0.0" ^
    --dest "jpackage-output" ^
    --app-version "1.0.0" ^
    --java-options "-Xmx1024m" ^
    --win-console

if !errorlevel! equ 0 (
    echo ==========================================
    echo EXE creation successful!
    echo Installer: jpackage-output\PinoraBrowser-1.0.0.exe
    echo ==========================================
) else (
    echo EXE creation failed.
    exit /b 1
)

endlocal
