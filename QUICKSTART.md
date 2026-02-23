# Quick Start Guide - PinoraBrowser

## 🚀 Getting Started

### Prerequisites
- **Java 21 or higher** (Required)
  - Download from: https://adoptium.net/ (Recommended)
  - Or: https://www.oracle.com/java/technologies/downloads/

### Installation & Running

#### Option 1: Direct JAR Execution (Recommended for most users)
```bash
java -jar pinora-browser-1.0.0.jar
```

#### Option 2: Windows Users
- Download `PinoraBrowser.bat`
- Double-click to run
- Or run from command prompt: `PinoraBrowser.bat`

#### Option 3: Linux/macOS Users
```bash
chmod +x pinora-browser.sh
./pinora-browser.sh
```

#### Option 4: Create Desktop Shortcut (Linux)
```bash
# Run the installer
sudo bash packaging/install-desktop.sh
```

---

## 📖 Basic Usage

### Navigation
| Action | Method |
|--------|--------|
| Go to website | Type URL in address bar and press Enter |
| Go back | Click ← button or Alt+Left Arrow |
| Go forward | Click → button or Alt+Right Arrow |
| Refresh page | Click ⟲ button or F5 |
| Go home | Click Home button |

### Tab Management
| Action | Shortcut |
|--------|----------|
| New tab | Ctrl+T |
| Close tab | Ctrl+W |
| Next tab | Ctrl+Tab |
| Previous tab | Ctrl+Shift+Tab |
| Reopen closed tab | Ctrl+Shift+T |

### Bookmarks & History
| Action | Shortcut |
|--------|----------|
| Bookmark page | Ctrl+D |
| Show bookmarks | Ctrl+B |
| Show history | Ctrl+H |
| Clear history | See Preferences |

### Search
| Action | Method |
|--------|--------|
| Quick search | Click search icon or Ctrl+F |
| Search with engine | Select engine from dropdown |

---

## ⚙️ System Requirements

| Component | Requirement |
|-----------|-------------|
| **Java Version** | 21 or higher (LTS) |
| **RAM** | 512 MB minimum, 2 GB recommended |
| **Disk Space** | 200 MB minimum |
| **OS** | Windows 10+, Linux (any), macOS 10.13+ |
| **Download Size** | ~47 MB |

---

## 🎨 Customization

### Change Theme
1. Click **View** → **Preferences**
2. Select **Dark** or **Light** theme
3. Click **Apply**

### Configure Cache
1. Click **View** → **Preferences**
2. Adjust cache size settings
3. Click **Apply**

### Manage Bookmarks
1. Click **View** → **Show Bookmarks Panel**
2. Right-click to add, edit, or delete bookmarks

---

## 🐛 Troubleshooting

### Issue: "Java not found" or "Java is not recognized"
**Solution:** 
- Ensure Java 21 is installed: `java -version`
- Add Java to PATH environment variable
- Restart terminal/command prompt after installation

### Issue: Browser fails to start
**Solution:**
- Check Java version: `java -version` (must be 21+)
- Ensure 512 MB RAM is available
- Try running with specific memory: `java -Xmx1g -jar pinora-browser-1.0.0.jar`

### Issue: Very slow performance
**Solution:**
- Close other applications to free up RAM
- Clear cache: View → Preferences → Clear Cache
- Allocate more memory: `java -Xmx2g -jar pinora-browser-1.0.0.jar`

### Issue: Some websites don't load properly
**Solution:**
- Clear cache and cookies
- Try disabling JavaScript (not recommended)
- Report the issue on GitHub

---

## 💡 Tips & Tricks

### Memory Configuration
For low-end machines:
```bash
java -Xmx512m -jar pinora-browser-1.0.0.jar
```

For high-performance:
```bash
java -Xmx2g -jar pinora-browser-1.0.0.jar
```

### Creating a Windows Shortcut
1. Right-click on desktop → New → Shortcut
2. Enter: `java -jar "C:\path\to\pinora-browser-1.0.0.jar"`
3. Name it "PinoraBrowser"
4. Click Finish

### Creating a Linux Launcher
Create `~/.local/share/applications/pinora-browser.desktop`:
```ini
[Desktop Entry]
Type=Application
Name=PinoraBrowser
Comment=A lightweight web browser
Exec=java -jar /path/to/pinora-browser-1.0.0.jar
Icon=application-x-java
Terminal=false
Categories=Network;
```

---

## 📚 More Information

- **GitHub Repository:** https://github.com/YOUR_USERNAME/pinoraBrowser
- **Issue Tracker:** https://github.com/YOUR_USERNAME/pinoraBrowser/issues
- **Changelog:** See CHANGELOG.md for release notes
- **Roadmap:** See ROADMAP.md for future features

---

## 📞 Getting Help

### Report a Bug
1. Go to GitHub Issues: https://github.com/YOUR_USERNAME/pinoraBrowser/issues
2. Click "New Issue"
3. Describe the problem with:
   - Steps to reproduce
   - Expected behavior
   - Actual behavior
   - Java version: `java -version`
   - Operating system

### Request Feature
- Use GitHub Issues with [FEATURE REQUEST] prefix
- Describe what you'd like to see and why

### Getting Community Support
- Check Discussions tab on GitHub
- Ask questions or share tips

---

## 📄 License

PinoraBrowser is licensed under [Your License Here]. See LICENSE file for details.

---

**Happy browsing! 🌐**
