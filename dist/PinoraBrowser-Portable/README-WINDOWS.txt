# Pinora Browser - Windows Installation Guide

## Quick Start

### Option 1: Direct Execution (Recommended)
1. Double-click `PinoraBrowser.bat`
2. The browser will launch automatically

### Option 2: Installer (For System-wide Installation)
1. Right-click `install.bat` and select "Run as administrator"
2. Follow the installer prompts
3. The application will be installed to `Program Files\Pinora Browser`
4. A shortcut will be created in your Start Menu

## Requirements

- **Java 21 LTS or higher** - [Download Java](https://www.oracle.com/java/technologies/downloads/)
- **Windows 7 SP1 or higher**
- **Minimum 512 MB RAM** (1 GB recommended)

## Installation Instructions

### Using Portable Version (No Installation)
Simply keep all files in the same directory:
- `PinoraBrowser.bat`
- `pinora-browser-1.0.0.jar`

Then double-click `PinoraBrowser.bat` to run.

### Using System Installer
1. Extract the portable folder to any location
2. Double-click `install.bat` (requires administrator privileges)
3. The app will be installed to `Program Files\Pinora Browser`
4. A Start Menu shortcut will be created

## Usage

### Keyboard Shortcuts
- **Ctrl+T** - Open a new tab
- **Ctrl+W** - Close the current tab
- **Ctrl+Q** - Exit the application
- **Ctrl+L** - Focus address bar (standard)
- **Ctrl+Tab** - Switch to next tab (standard)

### Manual Configuration

If Java is not automatically detected, you can manually set the Java path:

Edit `PinoraBrowser.bat` and modify this line:
```batch
java -Xmx1024m -jar "%SCRIPT_DIR%pinora-browser-1.0.0.jar"
```

To:
```batch
C:\path\to\java\bin\java.exe -Xmx1024m -jar "%SCRIPT_DIR%pinora-browser-1.0.0.jar"
```

## Troubleshooting

### "Java is not installed"
- Download and install [Java 21 LTS](https://www.oracle.com/java/technologies/downloads/)
- Ensure Java bin directory is in your system PATH
- Restart your computer after installing Java

### "JAR file not found"
- Make sure `pinora-browser-1.0.0.jar` is in the same directory as `PinoraBrowser.bat`
- Do not rename or move files unless you edit the batch file accordingly

### Application crashes on startup
- Increase allocated memory by editing the batch file
- Change `-Xmx1024m` to `-Xmx2048m` for more memory
- Try running from Command Prompt to see detailed error messages

### Slow performance
- Increase Java heap memory: change `-Xmx1024m` to `-Xmx2048m`
- Close other applications to free up system resources

## Support

For issues or feature requests, visit: [GitHub Repository](https://github.com/IVIarkStevan/pinoraBrowser)

## Version Information

- **Pinora Browser**: 1.0.0
- **Java Framework**: JavaFX 21
- **Build Date**: January 2025
