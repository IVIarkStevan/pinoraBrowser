@echo off
REM Pinora Browser Windows Installer
REM Creates a shortcut and installs the application

setlocal

set "INSTALL_DIR=%ProgramFiles%\Pinora Browser"
set "JAR_FILE=%~dp0pinora-browser-1.0.0.jar"
set "BAT_FILE=%~dp0PinoraBrowser.bat"

REM Check for administrator privileges
net session >nul 2>&1
if errorlevel 1 (
    echo This installer requires administrator privileges.
    echo Please run this batch file as Administrator.
    pause
    exit /b 1
)

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not found in PATH
    echo Please install Java 21 LTS or higher from https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo Installing Pinora Browser to %INSTALL_DIR%...

REM Create installation directory
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

REM Copy files
copy "%JAR_FILE%" "%INSTALL_DIR%\"
copy "%BAT_FILE%" "%INSTALL_DIR%\"

REM Create Start Menu shortcut
set "LINK=%AppData%\Microsoft\Windows\Start Menu\Programs\Pinora Browser.lnk"
powershell -Command "$WshShell = New-Object -ComObject WScript.Shell; $Shortcut = $WshShell.CreateShortcut('%LINK%'); $Shortcut.TargetPath = '%INSTALL_DIR%\PinoraBrowser.bat'; $Shortcut.WorkingDirectory = '%INSTALL_DIR%'; $Shortcut.Description = 'Pinora Browser - JavaFX Web Browser'; $Shortcut.Save()"

echo.
echo Installation complete!
echo You can now launch Pinora Browser from:
echo - Start Menu ^> Pinora Browser
echo - Or run: "%INSTALL_DIR%\PinoraBrowser.bat"
echo.
pause
endlocal
