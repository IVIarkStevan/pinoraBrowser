# Creating Windows .EXE for PinoraBrowser

There are multiple methods to create a Windows .exe file. Here are the best options:

---

## Method 1: Using jpackage (✅ Recommended - Built-in)

**Best for:** Mac/Linux developers who want to create standalone installers
**Requirements:** Java 16+ installed
**Output:** `PinoraBrowser-1.0.0.exe` (Windows installer)

### On Windows:

```cmd
# Step 1: Build the JAR
mvn clean package -DskipTests

# Step 2: Create EXE using jpackage
jpackage ^
    --input target ^
    --name PinoraBrowser ^
    --main-jar pinora-browser-1.0.0.jar ^
    --main-class com.pinora.browser.PinoraBrowser ^
    --type exe ^
    --vendor "Pinora" ^
    --description "A lightweight, minimal yet feature-rich web browser" ^
    --version 1.0.0 ^
    --dest jpackage-output ^
    --app-version 1.0.0 ^
    --java-options "-Xmx1024m" ^
    --icon src/main/resources/icons/pinora.ico

# Output: jpackage-output/PinoraBrowser-1.0.0.exe
```

### Or use PowerShell:

```powershell
# Step 1: Build JAR
mvn clean package -DskipTests

# Step 2: Create EXE
$jpackageArgs = @(
    "--input", "target",
    "--name", "PinoraBrowser",
    "--main-jar", "pinora-browser-1.0.0.jar",
    "--main-class", "com.pinora.browser.PinoraBrowser",
    "--type", "exe",
    "--vendor", "Pinora",
    "--description", "A lightweight, minimal yet feature-rich web browser",
    "--version", "1.0.0",
    "--dest", "jpackage-output",
    "--app-version", "1.0.0",
    "--java-options", "-Xmx1024m",
    "--icon", "src/main/resources/icons/pinora.ico"
)

jpackage $jpackageArgs
```

### What it creates:
✅ Self-contained Windows installer  
✅ Windows Registry entries  
✅ Start menu shortcuts  
✅ Uninstall support  
✅ No Java installation required by end-user

---

## Method 2: Using Launch4j (Alternative)

**Best for:** Maximum control and customization
**Requirements:** Launch4j software installed separately
**Output:** `PinoraBrowser.exe` (standalone executable wrapper)

### Installation:

1. **Download Launch4j**
   - Go to: https://launch4j.sourceforge.net/
   - Download the Windows installer or ZIP version
   - Install or extract to `C:\launch4j`

### Using Existing Configuration:

The project includes a pre-configured `pinora-browser-launch4j.xml` file:

```bash
# Step 1: Build JAR
mvn clean package -DskipTests

# Step 2: Run Launch4j with config (on Windows Command Prompt)
C:\launch4j\launch4jc.exe pinora-browser-launch4j.xml

# Output: PinoraBrowser.exe
```

### Via Maven Plugin:

Add to `pom.xml` (optional, for automated builds):

```xml
<plugin>
    <groupId>org.skife.maven</groupId>
    <artifactId>really-executable-jar-maven-plugin</artifactId>
    <version>1.5.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>really-executable-jar</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## Method 3: Using Batch Wrapper (Simple Alternative)

**Best for:** Quick and simple wrapper
**Requirements:** Nothing (just a batch file)
**Output:** `PinoraBrowser.bat` (↳ already in project)

Your project already includes `PinoraBrowser.bat` which is the simplest approach!

Just distribute:
- `PinoraBrowser.bat`
- `pinora-browser-1.0.0.jar`

Users run it by double-clicking the .bat file.

---

## Recommended Approach: Comparison

| Method | Ease | Professional | Size | Setup Required |
|--------|------|--------------|------|-----------------|
| **jpackage** | Medium | ⭐⭐⭐⭐⭐ | ~80 MB | Java 16+ |
| **Launch4j** | Medium | ⭐⭐⭐⭐ | ~50 MB | Launch4j software |
| **Batch Wrapper** | Easy | ⭐⭐ | ~47 MB | None |

---

## Complete Step-by-Step Guide: jpackage (Easiest)

### Prerequisites:
- Java 21 LTS installed
- Maven 3.8+
- Windows 10/11

### Step 1: Prepare Your System

```cmd
# Check Java version (must be 16+)
java -version

# Check Maven version (must be 3.8+)
mvn -version

# Set JAVA_HOME (required for jpackage)
setx JAVA_HOME "C:\Program Files\Java\jdk-21"
# Restart Command Prompt after this

# Verify JAVA_HOME is set
echo %JAVA_HOME%
```

### Step 2: Clone/Download Your Project

```cmd
cd C:\Users\YourName\Desktop
git clone https://github.com/IVIarkStevan/pinoraBrowser.git
cd pinoraBrowser
```

### Step 3: Build the JAR

```cmd
mvn clean package -DskipTests
```

Expected output: `target\pinora-browser-1.0.0.jar` (47 MB)

### Step 4: Create the EXE

```cmd
# Create output directory
mkdir jpackage-output

# Run jpackage
jpackage ^
    --input target ^
    --name PinoraBrowser ^
    --main-jar pinora-browser-1.0.0.jar ^
    --main-class com.pinora.browser.PinoraBrowser ^
    --type exe ^
    --vendor "Pinora" ^
    --description "A lightweight, minimal yet feature-rich web browser" ^
    --version 1.0.0 ^
    --dest jpackage-output ^
    --app-version 1.0.0 ^
    --java-options "-Xmx1024m" ^
    --icon src/main/resources/icons/pinora.ico
```

### Step 5: Verify the Output

```cmd
# List the generated EXE
dir jpackage-output\
```

Expected output location: `jpackage-output\PinoraBrowser-1.0.0.exe`

### Step 6: Test the EXE

```cmd
# Run the installer
jpackage-output\PinoraBrowser-1.0.0.exe
```

This will:
1. Open an installer wizard
2. Ask for installation location (default: `C:\Program Files\Pinora\PinoraBrowser`)
3. Ask for shortcuts preferences
4. Install the application
5. Create Start menu entries

### Step 7: Distribute

The file `PinoraBrowser-1.0.0.exe` is now ready to distribute!

---

## Using the Existing Build Script

Your project includes `build-exe.bat`:

```cmd
# On Windows, simply run:
build-exe.bat

# Or use the bash version (for Git Bash / WSL):
bash build-exe.sh
```

This automates all the steps above!

---

## Advanced jpackage Options

### Include a Splash Screen:
```cmd
jpackage ... ^
    --splash src/main/resources/icons/splash.png
```

### Bundle Java Runtime (No Java needed):
```cmd
jpackage ... ^
    --runtime C:\Program Files\Java\jdk-21 ^
    --app-version 1.0.0
```

### Set Start Menu Group:
```cmd
jpackage ... ^
    --win-menu ^
    --win-menu-group "Pinora"
```

### Create MSI Installer Instead:
```cmd
jpackage ^
    --input target ^
    --type msi ^
    ... (other options)
```

---

## Troubleshooting

### Issue: "jpackage: command not found"

**Solution:**
```cmd
# Check if jpackage is in PATH
where jpackage

# If not found, use full path
"C:\Program Files\Java\jdk-21\bin\jpackage.exe" ...

# Or add to PATH:
setx PATH "%PATH%;C:\Program Files\Java\jdk-21\bin"
```

### Issue: "Icon file not found"

```cmd
# Make sure icon exists:
dir src\main\resources\icons\pinora.ico

# If not, either:
# 1. Add the icon file, or
# 2. Remove the --icon option from jpackage command
```

### Issue: "Cannot find main class"

```cmd
# Verify main class name in pom.xml matches:
mvn help:describe -Ddetail=true | findstr "mainClass"

# Should show: com.pinora.browser.PinoraBrowser
```

### Issue: "Unsupported module system"

```cmd
# Make sure Java 21 is used:
java -version
# Check output shows "21"

# If not, set JAVA_HOME:
setx JAVA_HOME "C:\Program Files\Java\jdk-21"
```

---

## File Sizes and Performance

| Type | Size | Startup Time | Java Required |
|------|------|--------------|---------------|
| JAR + Batch | 47 MB | Medium | Yes |
| EXE (jpackage) | 80 MB | Fast | No |
| MSI Installer | 90 MB | N/A | No |

---

## Releasing the EXE on GitHub

### Option 1: Release as ZIP
```bash
# Create a ZIP with all files
zip PinoraBrowser-1.0.0-Windows.zip jpackage-output/PinoraBrowser-1.0.0.exe

# Upload to GitHub Release page
```

### Option 2: Upload Directly
On GitHub release page:
1. Click "Attach binaries"
2. Upload: `jpackage-output/PinoraBrowser-1.0.0.exe`
3. Add description: "Windows Installer - No Java installation required"

---

## Quick Reference

### jpackage Parameters:
- `--input` = Directory with JAR files
- `--name` = Application name
- `--main-jar` = Main JAR filename
- `--main-class` = Main class (com.pinora.browser.PinoraBrowser)
- `--type` = Output type (exe, msi, app-image)
- `--version` = Version number
- `--icon` = Icon file (.ico format)
- `--java-options` = JVM arguments
- `--dest` = Output directory

---

## Next Steps

1. **Run the build:**
   ```cmd
   build-exe.bat
   ```

2. **Test the EXE:**
   ```cmd
   jpackage-output\PinoraBrowser-1.0.0.exe
   ```

3. **Upload to GitHub:**
   - Go to Releases page
   - Upload the .exe file

---

**Your Windows .EXE is ready! 🪟**
