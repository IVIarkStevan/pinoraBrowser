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

