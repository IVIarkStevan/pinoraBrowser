@echo off
REM Pinora Browser Launcher for Windows
REM This batch file runs the Pinora Browser JAR with proper JavaFX configuration

setlocal enabledelayedexpansion

REM Get the directory where this batch file is located
set DIR=%~dp0

REM Set Java options
set JAVA_OPTS=-Xmx1024m

REM Try to detect Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

REM Run the browser
echo Starting Pinora Browser...
java %JAVA_OPTS% -jar "%DIR%target\pinora-browser-1.0.0.jar"

if %errorlevel% neq 0 (
    echo.
    echo Error running Pinora Browser
    pause
)

endlocal
