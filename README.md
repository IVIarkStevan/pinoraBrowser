# Pinora Browser

A lightweight, minimal yet feature-rich web browser built with Java, designed for Windows, Linux, and Android.

## Features

### Key Features (Inspired by Modern Browsers)
- **Lightweight & Fast**: Minimal resource consumption like Firefox
- **Multi-tab Support**: Tab management similar to Chrome
- **Customizable Interface**: Dark/Light theme like Vivaldi
- **Privacy-focused**: No tracking, clean browsing like Tor Browser
- **Keyboard Shortcuts**: Power-user friendly like Opera
- **Bookmarks & History**: Essential browsing tools
- **Auto-refresh & Cache Management**: Smart performance optimization
- **Download Manager**: File download tracking
- **Search Suggestions**: Quick search with multiple engines

## Known Limitations

### Current Features
- **✓ Bookmarks with Persistence**: Bookmarks are automatically saved to disk and restored on application startup
  - File location: `~/.pinora/bookmarks.json`
  - Features: Add, remove, search, import/export (JSON/HTML)
  - Status: **FULLY IMPLEMENTED** ✓
  
- **✓ Browsing History with Persistence**: History is automatically tracked and saved to disk
  - File location: `~/.pinora/history.json`
  - Features: Search, date filtering, export, most-visited tracking (max 1000 entries)
  - Status: **FULLY IMPLEMENTED** ✓
  
- **✓ Clear History Function**: "Clear History" menu item is fully functional
  - Clears both in-memory history and disk storage
  - Status: **FULLY IMPLEMENTED** ✓

### Planned Features
- **Bookmarks/History UI Panels**: Visual panels to view and manage bookmarks/history (in development)
- **Tab Persistence**: Automatic restoration of open tabs on startup
- **Cookie Management UI**: Visual interface for managing cookies
- **Advanced Search Features**: Full-text history search with filters
- **Sync Across Devices**: Cloud-based bookmark/history synchronization
- **Custom Extensions**: WebExtensionAPI compatibility improvements

## Project Structure

```
pinora-browser/
├── src/
│   ├── main/
│   │   ├── java/com/pinora/browser/
│   │   │   ├── PinoraBrowser.java (Main Entry Point)
│   │   │   ├── ui/ (UI Components)
│   │   │   ├── core/ (Core Browser Logic)
│   │   │   └── util/ (Utilities)
│   │   └── resources/
│   │       ├── css/ (Stylesheets)
│   │       └── icons/ (Application Icons)
├── pom.xml
└── README.md
```

## Build Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.8 or higher

### Build for Windows (.exe)
```bash
mvn clean package -P windows
```

### Build for Linux
```bash
mvn clean package
```

### Run Locally
```bash
mvn clean javafx:run
```

## Download & Install

You can install Pinora Browser from GitHub Releases (recommended) or build and run it locally.

Important: this project targets Java 21 (LTS). Install a Java 21 JDK (Adoptium/Eclipse Temurin, Azul, or your distro's packages) before running the app.

Linux
 - Download the latest release archive from the Releases page, extract it, and run the provided launcher:

```bash
# Example (replace URL/version with the release you want):
wget https://github.com/<owner>/<repo>/releases/download/vX.Y.Z/pinora-browser-X.Y.Z-linux.tar.gz
tar xzf pinora-browser-X.Y.Z-linux.tar.gz
cd pinora-browser-X.Y.Z
./pinora-browser.sh
```

 - Alternatively, run the packed JAR directly (requires Java 21):

```bash
# From a release or from target/ after building
java -jar pinora-browser-<version>.jar
```

Windows
 - Download the Windows installer or ZIP from the Releases page and run the executable or batch launcher:

```powershell
# Example (replace URL/version with the release you want):
Invoke-WebRequest -Uri "https://github.com/<owner>/<repo>/releases/download/vX.Y.Z/pinora-browser-X.Y.Z-windows.zip" -OutFile "pinora-browser.zip"
Expand-Archive .\pinora-browser.zip -DestinationPath .\pinora-browser
Set-Location .\pinora-browser
.\PinoraBrowser.bat
```

 - Or run the JAR directly (requires Java 21):

```powershell
java -jar pinora-browser-<version>.jar
```

Build from source (both platforms)
 - Clone and build with Maven; the produced artifacts are placed in `target/`:

```bash
git clone <repo-url>
cd pinora-browser
mvn clean package
# On Linux: run ./pinora-browser.sh or java -jar target/pinora-browser-<version>.jar
# On Windows: run PinoraBrowser.bat or java -jar target/pinora-browser-<version>.jar
```

Notes
 - Replace `<owner>/<repo>` and version placeholders with the repository owner, repo name and release tag used on your GitHub Releases page.
 - If you plan to distribute installers, upload installer artifacts to GitHub Releases so users can download them directly.

## Technology Stack

- **Language**: Java 17+
- **UI Framework**: JavaFX 21
- **Web Rendering**: JavaFX WebView
- **Build Tool**: Maven
- **Cross-platform**: Windows, Linux, and Android support

## License

MIT License - Feel free to use and modify

## Author

Pinora Browser Team
