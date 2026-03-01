# Changelog

All notable changes to the PinoraBrowser project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-02-23

### Added
- **Multi-tab browsing** - Open and manage multiple tabs simultaneously
- **Tab management system** - Close, reorder, and navigate between tabs
- **Address bar** - Smart URL formatting and validation
- **Navigation controls** - Back, Forward, Refresh, Home buttons
- **Bookmark management** - Save and organize favorite websites
- **History tracking** - Browse your browsing history
- **Cache management** - Automatic cache cleanup and optimization
- **Dark/Light theme** - Customizable UI themes
- **Menu system** - File, Edit, View, History, and Help menus
- **Search functionality** - Quick search with address bar
- **Download manager** - Track and manage file downloads
- **Keyboard shortcuts** - Power-user friendly shortcuts
- **Configuration management** - Persistent application settings
- **Preferences dialog** - Customize browser behavior
- **JavaScript support** - Browse modern web pages
- **Security** - Basic security features for safe browsing

### Technical
- Built with **Java 21 LTS** for long-term stability
- **JavaFX 21** for modern cross-platform UI
- **jsoup** for HTML parsing and web scraping
- **GSON** for JSON processing
- **SLF4J** for logging
- Maven build system with comprehensive plugins
- Shade plugin for self-contained JAR distribution
- Cross-platform support (Windows, Linux, macOS)

### Notes
- Requires Java 21 or higher
- Minimum 512 MB RAM recommended
- Approximately 47 MB download size
- Initial release with core browser functionality
- **Known Limitations**: 
  - Bookmark data is not persisted to disk and will be lost on application restart
  - "Clear History" menu item has no action - functionality coming in future release
  - No visible UI panel to view or manage bookmarks and history

---

## Unreleased

### Planned for Phase 2
- Advanced tab management (tab groups, preview on hover)
- Bookmarks bar with drag-and-drop
- Search suggestions with multiple engines
- Advanced download manager (pause/resume)
- Password manager integration
- Cookie management
- JavaScript console
- Basic developer tools
- Website notifications
- Fullscreen mode

### Planned for Phase 3
- Memory optimization
- Lazy loading of tabs
- Advanced resource caching strategies
- Performance benchmarking

---

## Format Guidelines

### Categories
- **Added** - New features
- **Changed** - Changes in existing functionality  
- **Deprecated** - Features that will be removed
- **Removed** - Removed features
- **Fixed** - Bug fixes
- **Security** - Security vulnerability fixes
- **Technical** - Internal technical improvements

### Versioning
- MAJOR.MINOR.PATCH (e.g., 1.0.0, 1.1.0, 1.0.1)
- MAJOR: Breaking changes or significant rewrites
- MINOR: New features and enhancements
- PATCH: Bug fixes and minor improvements
