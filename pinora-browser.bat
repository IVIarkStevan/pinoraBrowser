@echo off
rem Robust Pinora Browser launcher for Windows
rem - detects Java (uses JAVA_HOME or java in PATH)
rem - attempts to build jar with Maven if missing
setlocal enabledelayedexpansion

set DIR=%~dp0

rem prefer explicit PINORA_JAVA env var
if defined PINORA_JAVA (
    set JAVA_CMD=%PINORA_JAVA%
 ) else (
    if defined JAVA_HOME (
        set JAVA_CMD=%JAVA_HOME%\bin\java.exe
    ) else (
        for /f "delims=" %%J in ('where java 2^>nul') do if not defined JAVA_CMD set JAVA_CMD=%%J
    )
)

if not defined JAVA_CMD (
    echo Java not found. Install JDK 21 or newer and ensure java is on PATH or set JAVA_HOME.
    pause
    exit /b 1
)

set JAR=
for %%F in ("%DIR%target\pinora-browser-*.jar") do if not defined JAR set JAR=%%~fF

if not defined JAR (
    echo No JAR found under %DIR%target. Attempting to build with Maven...
    where mvn >nul 2>nul
    if %errorlevel% equ 0 (
        pushd "%DIR%"
        mvn -DskipTests package
        popd
        for %%F in ("%DIR%target\pinora-browser-*.jar") do if not defined JAR set JAR=%%~fF
    ) else (
        echo Maven not found; please build the project: mvn -DskipTests package
    )
)

if not defined JAR (
    echo Could not find or build the JAR. Exiting.
    pause
    exit /b 1
)

echo Starting Pinora Browser: %JAR%
"%JAVA_CMD%" -jar "%JAR%"

if %errorlevel% neq 0 (
    echo Error running Pinora Browser (exit code %errorlevel%)
    pause
)

endlocal
