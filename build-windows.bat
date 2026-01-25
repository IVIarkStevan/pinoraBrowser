@echo off
REM Build script for Pinora Browser on Windows

setlocal enabledelayedexpansion

echo ==========================================
echo Pinora Browser - Windows Build
echo ==========================================

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Maven is not installed. Please install Maven first.
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo Java is not installed. Please install Java 21 or higher.
    exit /b 1
)

echo Building Pinora Browser...
call mvn clean package -DskipTests

if %errorlevel% equ 0 (
    echo ==========================================
    echo Build successful!
    echo Output: target\pinora-browser-1.0.0-jar-with-dependencies.jar
    echo ==========================================
    
    REM Create batch runner
    echo Creating launcher script...
    (
        echo @echo off
        echo cd /d "%%~dp0"
        echo java -jar "target\pinora-browser-1.0.0-jar-with-dependencies.jar"
    ) > pinora-browser.bat
    
    echo To run the browser, execute: pinora-browser.bat
) else (
    echo Build failed. Please check the error messages above.
    exit /b 1
)

endlocal
