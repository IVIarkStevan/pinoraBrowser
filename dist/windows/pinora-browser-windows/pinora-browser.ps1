# Pinora Browser Launcher Script for Windows PowerShell
# This script launches Pinora Browser with proper JavaFX configuration

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$jarPath = Join-Path $scriptDir "target\pinora-browser-1.0.0.jar"

# Check if Java is available
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java version: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 17 or higher" -ForegroundColor Yellow
    exit 1
}

# Check if JAR file exists
if (-Not (Test-Path $jarPath)) {
    Write-Host "Error: JAR file not found at $jarPath" -ForegroundColor Red
    exit 1
}

Write-Host "Starting Pinora Browser..." -ForegroundColor Cyan
Write-Host "JAR Path: $jarPath" -ForegroundColor Gray

# Run the browser
java -Xmx1024m -jar $jarPath

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error running Pinora Browser (Exit code: $LASTEXITCODE)" -ForegroundColor Red
}
