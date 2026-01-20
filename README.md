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
