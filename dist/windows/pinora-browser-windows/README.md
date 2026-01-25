# Pinora Browser - Windows Distribution Guide

## Quick Start (Windows)

### Option 1: Using Batch Script (Easiest)
```cmd
pinora-browser.bat
```

### Option 2: Using PowerShell
```powershell
powershell -ExecutionPolicy Bypass -File pinora-browser.ps1
```

### Option 3: Command Prompt
```cmd
java -jar target\pinora-browser-1.0.0.jar
```

## System Requirements

- **Java 17 or higher** (OpenJDK or Oracle JDK)
- **Windows 7 or later** (Windows 10/11 recommended)
- **1 GB RAM minimum** (2 GB recommended)
- **200 MB free disk space**

## Installation Steps

### Step 1: Install Java
If Java is not installed:
1. Download Java 17+ from [adoptium.net](https://adoptium.net/)
2. Install and ensure `java` is in PATH
3. Verify: Open cmd and type `java -version`

### Step 2: Extract Pinora Browser
1. Extract the `pinora-browser-windows.zip` file
2. You'll have a folder with:
   - `pinora-browser.bat` (launcher)
   - `pinora-browser.ps1` (PowerShell launcher)
   - `target/` folder (containing the JAR)
   - `src/` folder (source code)

### Step 3: Run the Browser
Double-click `pinora-browser.bat` or use command line:
```cmd
cd pinora-browser
pinora-browser.bat
```

## Creating a Desktop Shortcut

1. Right-click `pinora-browser.bat`
2. Select "Send to" > "Desktop (create shortcut)"
3. Right-click the shortcut and select "Properties"
4. Under "Shortcut" tab:
   - **Target**: `C:\path\to\pinora-browser.bat`
   - **Start in**: `C:\path\to\pinora-browser`
   - **Comment**: Pinora Browser

## Troubleshooting

### "Java is not recognized"
- Install Java 17+
- Add Java to system PATH:
  1. System Properties > Environment Variables
  2. Add JAVA_HOME pointing to Java installation
  3. Add %JAVA_HOME%\bin to PATH

### "JAR file not found"
- Ensure `target/pinora-browser-1.0.0.jar` exists
- Run from the correct directory

### "Out of memory" error
- Edit `pinora-browser.bat`
- Change `-Xmx1024m` to `-Xmx2048m` (or higher)

### No window appears
- Wait 5-10 seconds (JavaFX takes time to initialize)
- Check Java version: `java -version`
- Try PowerShell script instead: `powershell -File pinora-browser.ps1`

## Usage

1. **Enter URL** in the address bar
2. **Navigation** buttons: Back, Forward, Refresh, Home
3. **New Tab** - File menu > New Tab
4. **Bookmarks** - Save your favorite sites
5. **History** - View and manage browsing history
6. **Preferences** - Edit > Preferences

## Features

✓ Multi-tab browsing  
✓ Smart address bar  
✓ History management  
✓ Bookmarks system  
✓ Download manager  
✓ Lightweight caching  
✓ Customizable settings  
✓ Clean, minimal UI  

## On Linux with Wine

To run on Linux with Wine:
```bash
wine pinora-browser.bat
# or
wine java -jar target\pinora-browser-1.0.0.jar
```

## Support

For issues or feature requests, check the project README.md

---

**Pinora Browser v1.0.0**  
*A lightweight, minimal yet feature-rich web browser*  
© 2026 Pinora Browser Team
