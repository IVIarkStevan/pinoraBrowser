# Pinora Browser - Linux Distribution Guide

## Quick Start (Linux)

### Option 1: Using Shell Script (Easiest)
```bash
chmod +x pinora-browser.sh
./pinora-browser.sh
```

### Option 2: Direct JAR Execution
```bash
java -jar pinora-browser-1.0.0.jar
```

### Option 3: With Custom Memory
```bash
java -Xmx2g -jar pinora-browser-1.0.0.jar
```

## System Requirements

- **Java 21 or higher** (OpenJDK or Oracle JDK)
- **Linux kernel 5.0 or later** (Any modern distribution)
- **1 GB RAM minimum** (2 GB recommended)
- **200 MB free disk space**
- **GTK 3.0+** or **Wayland** support (for JavaFX)

## Installation Steps

### Step 1: Install Java 21

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

#### Fedora/RHEL/CentOS
```bash
sudo dnf install java-21-openjdk
```

#### Arch Linux
```bash
sudo pacman -S jdk-openjdk
```

#### openSUSE
```bash
sudo zypper install java-21-openjdk
```

#### Manual Installation (All Distributions)
1. Download from [adoptium.net](https://adoptium.net/)
2. Extract the archive:
```bash
tar -xzf OpenJDK21U-jdk_x64_linux_*.tar.gz
```
3. Add to PATH:
```bash
export JAVA_HOME=/path/to/jdk-21
export PATH=$JAVA_HOME/bin:$PATH
```

### Verify Java Installation
```bash
java -version
```
You should see output like:
```
openjdk version "21.0.1" 2023-10-17
OpenJDK Runtime Environment (Red Hat Inc.) (build 21.0.1+13)
OpenJDK 64-Bit Server VM (build 21.0.1+13, mixed mode, sharing)
```

### Step 2: Extract Pinora Browser

```bash
# Create a directory for the browser
mkdir -p ~/applications/pinora-browser
cd ~/applications/pinora-browser

# Extract the release archive
tar -xzf pinora-browser-1.0.0.tar.gz
```

Or download the JAR and script separately:
```bash
wget https://github.com/IVIarkStevan/pinoraBrowser/releases/download/v1.0.0/pinora-browser-1.0.0.jar
wget https://raw.githubusercontent.com/IVIarkStevan/pinoraBrowser/master/pinora-browser.sh
chmod +x pinora-browser.sh
```

### Step 3: Run the Browser

#### Simple Launch
```bash
./pinora-browser.sh
```

#### With Environment Variables
```bash
# Allocate 2GB RAM
java -Xmx2g -jar pinora-browser-1.0.0.jar

# Allocate 4GB RAM (for heavy usage)
java -Xmx4g -jar pinora-browser-1.0.0.jar
```

---

## Creating a Desktop Launcher

### Option 1: Using Install Script (Recommended)
```bash
chmod +x packaging/install-desktop.sh
./packaging/install-desktop.sh
```

This will:
- Create a `.desktop` file in `~/.local/share/applications/`
- Make it appear in your application menu
- Allow pinning to taskbar

### Option 2: Manual Desktop Entry

Create `~/.local/share/applications/pinora-browser.desktop`:

```ini
[Desktop Entry]
Type=Application
Name=Pinora Browser
Comment=A lightweight, minimal yet feature-rich web browser
Icon=applications-internet
Exec=bash -c 'cd %USERPROFILE%/applications/pinora-browser && java -jar pinora-browser-1.0.0.jar'
Terminal=false
Categories=Network;WebBrowser;
Keywords=browser;web;internet;
StartupNotify=true
```

Make it executable:
```bash
chmod +x ~/.local/share/applications/pinora-browser.desktop
```

### Option 3: Create an Alias

Add to your shell configuration file (`~/.bashrc`, `~/.zshrc`, or `~/.config/fish/config.fish`):

```bash
alias pinora='java -jar ~/applications/pinora-browser/pinora-browser-1.0.0.jar'
```

Then reload:
```bash
source ~/.bashrc  # or source ~/.zshrc for zsh
```

Now you can launch with:
```bash
pinora
```

---

## Creating a Launcher Shortcut

### Using Menu Editor (GNOME/KDE)

1. Right-click on desktop → **Create Launcher**
2. Fill in:
   - **Name**: Pinora Browser
   - **Command**: `bash -c 'cd ~/applications/pinora-browser && ./pinora-browser.sh'`
   - **Icon**: Choose an internet/browser icon
3. Click **Save**
4. Double-click the launcher to run

---

## Troubleshooting

### Issue: "Java: command not found"

**Solution 1:** Install Java (see installation steps above)

**Solution 2:** Java is installed but not in PATH
```bash
# Find Java installation
which java
# or
find / -name java -type f 2>/dev/null

# Add to PATH temporarily
export PATH=/usr/lib/jvm/java-21-openjdk/bin:$PATH
java -version

# Make permanent: Add to ~/.bashrc or ~/.zshrc
echo 'export PATH=/usr/lib/jvm/java-21-openjdk/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

### Issue: "Permission denied" when running script

**Solution:**
```bash
chmod +x pinora-browser.sh
./pinora-browser.sh
```

### Issue: "Browser fails to start" or "X11 error"

**Solution 1:** Update graphics drivers
```bash
# For NVIDIA
ubuntu-drivers autoinstall

# For AMD
# Check your distribution's documentation
```

**Solution 2:** Try with Wayland (if available)
```bash
QT_QPA_PLATFORM=wayland java -jar pinora-browser-1.0.0.jar
```

**Solution 3:** Force X11
```bash
DISPLAY=:0 java -jar pinora-browser-1.0.0.jar
```

### Issue: Very slow performance

**Solution 1:** Allocate more RAM
```bash
java -Xmx4g -jar pinora-browser-1.0.0.jar
```

**Solution 2:** Check system resources
```bash
# View system memory
free -h

# View CPU usage
top
```

**Solution 3:** Clear cache
- Run the browser
- Go to View → Preferences → Clear Cache
- Restart

### Issue: Some websites don't load properly

**Solution 1:** Clear browser cache and history
```bash
# Browser settings: View → Preferences → Clear Cache
```

**Solution 2:** Update Java
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# Fedora
sudo dnf update java-21-openjdk
```

---

## Advanced Configuration

### Memory Management

#### For Low-End Machines (512 MB available)
```bash
java -Xmx512m -jar pinora-browser-1.0.0.jar
```

#### For Medium Machines (2 GB available)
```bash
java -Xmx1g -jar pinora-browser-1.0.0.jar
```

#### For High-Performance (4+ GB available)
```bash
java -Xmx4g -jar pinora-browser-1.0.0.jar
```

### Verbose Output for Debugging
```bash
java -verbose:class -jar pinora-browser-1.0.0.jar
```

### Enable Garbage Collection Logging
```bash
java -Xlog:gc* -jar pinora-browser-1.0.0.jar
```

---

## Running as a Service (Advanced)

Create a systemd service file at `~/.config/systemd/user/pinora-browser.service`:

```ini
[Unit]
Description=Pinora Browser
After=graphical-session.target

[Service]
Type=simple
ExecStart=%h/applications/pinora-browser/pinora-browser.sh
Restart=on-failure
Environment="DISPLAY=:0"

[Install]
WantedBy=graphical-session.target
```

Enable and start:
```bash
systemctl --user daemon-reload
systemctl --user enable pinora-browser
systemctl --user start pinora-browser
```

Check status:
```bash
systemctl --user status pinora-browser
```

---

## Uninstalling Pinora Browser

### Remove the Application Files
```bash
rm -rf ~/applications/pinora-browser
```

### Remove Desktop Shortcut (if created)
```bash
rm ~/.local/share/applications/pinora-browser.desktop
```

### Remove Alias (if added)
```bash
# Edit ~/.bashrc or ~/.zshrc and remove the alias line
nano ~/.bashrc
# Find and delete: alias pinora='...'
```

### Keep Java (Optional)
Java will remain installed if you want to use it for other applications.

---

## Distribution-Specific Notes

### Ubuntu/Debian
- Well-tested with OpenJDK and Oracle JDK
- May need to install `libgtk-3-0` for full GUI support
```bash
sudo apt install libgtk-3-0
```

### Fedora/RHEL/CentOS
- Uses DNF package manager instead of apt
- Good compatibility with OpenJDK
- May need `gtk3` and `glib2` packages

### Arch Linux
- Minimal installation - ensure all dependencies are met
- Use `yay` for AUR packages if needed
```bash
yay -S openjdk-21
```

### openSUSE
- Use `zypper` for package management
- Good compatibility with latest Java versions

### Alpine Linux (Lightweight)
```bash
apk update
apk add openjdk21
apk add gtk+3 glib  # For GUI support
```

---

## Tips & Tricks

### Maximize Performance
```bash
# Create an optimized launcher
java -server -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar pinora-browser-1.0.0.jar
```

### Run in Background
```bash
nohup java -jar pinora-browser-1.0.0.jar &
# or
java -jar pinora-browser-1.0.0.jar &
disown
```

### Create a Portable Installation
```bash
# Copy to USB drive
cp -r ~/applications/pinora-browser/ /media/usb/pinora-browser/
# Run from anywhere
/media/usb/pinora-browser/pinora-browser.sh
```

### Monitor Resource Usage
```bash
# In a separate terminal, monitor Java process
watch -n 1 'ps aux | grep java'

# Or use htop
htop
```

---

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| New Tab | Ctrl+T |
| Close Tab | Ctrl+W |
| Next Tab | Ctrl+Tab |
| Previous Tab | Ctrl+Shift+Tab |
| Go Back | Alt+Left Arrow |
| Go Forward | Alt+Right Arrow |
| Refresh Page | F5 or R |
| Bookmark Page | Ctrl+D |
| Show Bookmarks | Ctrl+B |
| Show History | Ctrl+H |

---

## Getting Help

### Report Issues
- **GitHub Issues**: https://github.com/IVIarkStevan/pinoraBrowser/issues
- Include: OS version, Java version, error messages

### Check Documentation
- [QUICKSTART.md](QUICKSTART.md) - General user guide
- [CHANGELOG.md](CHANGELOG.md) - Version history and features
- [README.md](README.md) - Project overview

### Community Support
- GitHub Discussions tab
- Leave comments on issues

---

## Uninstall Java (Optional)

If you want to remove Java after uninstalling Pinora Browser:

#### Ubuntu/Debian
```bash
sudo apt remove openjdk-21-jdk
sudo apt autoremove
```

#### Fedora/RHEL/CentOS
```bash
sudo dnf remove java-21-openjdk
sudo dnf autoremove
```

#### Arch Linux
```bash
sudo pacman -R jdk-openjdk
```

---

## Frequently Asked Questions

**Q: Can I run multiple instances?**
A: Yes, just run the command multiple times

**Q: Does it support Wayland?**
A: Yes, modern JavaFX supports Wayland

**Q: Can I customize the theme?**
A: Yes, see View → Preferences for theme options

**Q: How do I report bugs?**
A: Visit the GitHub Issues page with your Java version and Linux distribution

**Q: Is it safe to use?**
A: Yes, it's open source. Review the code at GitHub

---

**Happy browsing on Linux! 🐧🌐**
