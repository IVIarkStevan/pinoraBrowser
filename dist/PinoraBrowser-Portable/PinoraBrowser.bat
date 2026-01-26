@echo off
REM Pinora Browser Windows Launcher
REM This batch file launches the Pinora Browser JavaFX application

setlocal enabledelayedexpansion

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not found in PATH
    echo Please install Java 21 LTS or higher from https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Get the directory where this batch file is located
set "SCRIPT_DIR=%~dp0"

REM Check if JAR file exists
if not exist "%SCRIPT_DIR%pinora-browser-1.0.0.jar" (
    echo Error: pinora-browser-1.0.0.jar not found
    echo Make sure the JAR file is in the same directory as this batch file
    pause
    exit /b 1
)

REM Launch the application with Java
java -Xmx1024m -jar "%SCRIPT_DIR%pinora-browser-1.0.0.jar"

if errorlevel 1 (
    echo Application exited with error code !errorlevel!
    pause
)

endlocal
