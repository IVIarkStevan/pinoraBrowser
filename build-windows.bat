@echo off
REM Optimized build script for Pinora Browser on Windows
REM Features: Parallel compilation, cache optimization, YouTube support

setlocal enabledelayedexpansion

echo ==========================================
echo Pinora Browser - Windows Build ^(Optimized^)
echo ==========================================

REM Check dependencies
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed.
    echo Install from: https://maven.apache.org/download.cgi
    exit /b 1
)

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed.
    echo Install Java 21+ from: https://www.oracle.com/java/
    exit /b 1
)

REM Show Java version
echo.
for /f "tokens=*" %%A in ('java -version 2^>^&1') do (
    echo Using: %%A
)

REM Check for YouTube support tools
echo.
echo YouTube HD Player Support:
where vlc >nul 2>nul
if %errorlevel% equ 0 (
    where yt-dlp >nul 2>nul
    if %errorlevel% equ 0 (
        echo [OK] VLC and yt-dlp detected - HD playback enabled
    ) else (
        echo [WARN] VLC found but yt-dlp missing
        echo Install: pip install yt-dlp
    )
) else (
    echo [INFO] Install for YouTube HD playback:
    echo   1. Download VLC from: https://www.videolan.org/
    echo   2. Install yt-dlp: pip install yt-dlp
)

echo.
echo Building Pinora Browser with optimizations...
echo.

REM Build with optimization flags
call mvn clean package ^
    -DskipTests ^
    -T 1C ^
    -q ^
    -Dmaven.compiler.fork=true ^
    -Dmaven.compiler.debug=false ^
    -Dmaven.compiler.optimize=true

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo [SUCCESS] Build completed!
    echo ==========================================
    
    REM Find JAR file
    for /f "delims=" %%F in ('dir /b /s target\pinora-browser-*.jar 2^>nul ^| findstr /v pom') do (
        set "JAR_FILE=%%F"
        for /F %%A in ('powershell -Command "[math]::Round((Get-Item '%%F').Length / 1MB, 2) | % {$_ + ' MB'}"') do (
            echo Output: %%F ^(%%A^)
        )
    )
    
    if not defined JAR_FILE (
        echo Output: target\pinora-browser-1.0.0.jar
    )
    
    REM Create optimized PowerShell runner
    echo.
    echo Creating launcher scripts...
    
    REM Batch launcher
    (
        echo @echo off
        setlocal enabledelayedexpansion
        echo cd /d "%%~dp0"
        echo.
        echo REM Optimized JVM flags for performance
        echo set JVM_FLAGS=-Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
        echo.
        echo REM Find JAR file
        echo if exist "target\pinora-browser-1.0.0.jar" ^(
        echo     set JAR=target\pinora-browser-1.0.0.jar
        echo ^) else if exist "pinora-browser-1.0.0.jar" ^(
        echo     set JAR=pinora-browser-1.0.0.jar
        echo ^) else ^(
        echo     for /f "delims=" %%%%F in ^('dir /b /s target\pinora-browser-*.jar 2^>nul'^ ^) do ^(
        echo         set JAR=%%%%F
        echo     ^)
        echo ^)
        echo.
        echo if not defined JAR ^(
        echo     echo Error: pinora-browser JAR not found
        echo     pause
        echo     exit /b 1
        echo ^)
        echo.
        echo java %%JVM_FLAGS%% -jar "%%JAR%%"
    ) > pinora-browser.bat
    
    REM PowerShell launcher
    (
        echo # Pinora Browser Launcher for PowerShell
        echo $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
        echo Set-Location $scriptDir
        echo.
        echo # JVM optimization flags
        echo $env:JVM_FLAGS = '-Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200'
        echo.
        echo # Find JAR file
        echo $jar = $null
        echo if ^(Test-Path 'target\pinora-browser-1.0.0.jar'^) { $jar = 'target\pinora-browser-1.0.0.jar' }
        echo if ^(!$jar -and ^(Test-Path 'pinora-browser-1.0.0.jar'^)^ { $jar = 'pinora-browser-1.0.0.jar' }
        echo if ^(!$jar^ { $jar = Get-Item -Path 'target\pinora-browser-*.jar' ^| Select-Object -First 1 -ExpandProperty FullName }
        echo.
        echo if ^(!$jar^ { Write-Host 'Error: JAR not found'; exit 1 }
        echo.
        echo ^& java $env:JVM_FLAGS -jar $jar
    ) > pinora-browser.ps1
    
    echo [OK] Batch launcher: pinora-browser.bat
    echo [OK] PowerShell launcher: pinora-browser.ps1
    echo.
    echo To run: pinora-browser.bat or powershell -ExecutionPolicy Bypass -File pinora-browser.ps1
) else (
    echo.
    echo ==========================================
    echo [ERROR] Build failed!
    echo ==========================================
    exit /b 1
)

endlocal
