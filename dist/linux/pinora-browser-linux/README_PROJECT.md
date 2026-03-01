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

### Bookmark Persistence
- **Bookmarks are not saved to disk** - Bookmarks are lost when the application is restarted
  - This is a planned feature for a future release
  - Currently, all browser data (bookmarks, history) exists only in memory

### Bookmarks/History UI
- **No visible panel to view bookmarks or history** - There is no UI panel or sidebar to display and manage bookmarks or browsing history
  - Menu items exist but lack a corresponding interface
  - This feature is planned for a future release

### History Menu
- **"Clear History" menu item has no action** - The menu item exists but does not perform any function
  - Functionality is planned for a future release
  - History data is currently cleared automatically on application restart

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
