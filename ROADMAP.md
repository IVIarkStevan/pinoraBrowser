# Pinora Browser - Implementation Roadmap

## Phase 1: Core Application (Current)
- [x] Project structure setup
- [x] Maven configuration with JavaFX
- [x] Basic browser window with tabs
- [x] Address bar with URL formatting
- [x] Navigation buttons (back, forward, refresh, home)
- [x] Menu bar (File, Edit, View, History, Help)
- [x] History management
- [x] Bookmarks system
- [x] Cache management
- [x] Configuration management

## Phase 2: Enhanced Features (Next)
- [ ] Advanced tab management (tab groups, preview on hover)
- [ ] **Bookmarks/History UI panel** (No visible panel to view or manage them)
- [ ] Bookmarks bar with drag-and-drop
- [ ] **Bookmark persistence to disk** (Currently bookmarks are lost on restart)
- [ ] **Clear History functionality** (Menu item exists but has no action)
- [ ] Search suggestions with multiple engines
- [ ] Download manager with pause/resume
- [ ] Password manager integration
- [ ] Cookie management
- [ ] JavaScript console
- [ ] Developer tools basics
- [ ] Website notifications
- [ ] Fullscreen mode

## Phase 3: Performance & Optimization
- [ ] Memory optimization
- [ ] Lazy loading of tabs
- [ ] Resource caching strategies
- [ ] Startup time optimization
- [ ] Plugin/extension support basics

## Phase 4: Platform-Specific Features
- [ ] Windows: System integration, shortcut creation
- [ ] Linux: Desktop file, system tray integration
- [ ] Android: Touch gestures, mobile optimization

## Phase 5: Windows EXE Creation
- [ ] Setup jpackage configuration
- [ ] Create installer bundle
- [ ] Add Windows shortcuts
- [ ] Code signing (optional)
- [ ] Auto-updater mechanism

## Key Features from Inspiration Browsers

### From Chrome
- Fast performance
- Simple, clean UI
- Multi-process architecture (simplified)

### From Firefox
- Lightweight footprint
- Privacy-focused by default
- Open development model

### From Vivaldi
- Customizable UI
- Tab stacking/grouping
- Multiple search engines

### From Brave
- Built-in privacy tools
- Block ads and trackers
- Minimal bloat

### From Opera
- Speed dial feature
- Sidebar panels
- Efficient resource management

### From Tor Browser
- HTTPS enforcement
- Tracking prevention
- Privacy settings

## Build Instructions

### Linux Build
```bash
chmod +x build-linux.sh
./build-linux.sh
```

### Windows Build (For creating EXE)
```cmd
build-windows.bat
build-exe.bat
```

### Manual Build
```bash
mvn clean javafx:run          # Run directly
mvn clean package             # Create JAR
```

## Next Steps

1. Implement advanced tab management
2. Add download manager functionality
3. Create bookmarks bar UI
4. Set up search engine selection
5. Test on Windows to create EXE
