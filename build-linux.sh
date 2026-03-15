#!/bin/bash

# Optimized build script for Pinora Browser on Linux
# Features: Parallel compilation, cache optimization, YouTube support

set -e  # Exit on error

echo "=========================================="
echo "Pinora Browser - Linux Build (Optimized)"
echo "=========================================="

# Check dependencies
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo "ERROR: $2 is not installed."
        echo "Install with: sudo apt-get install $1"
        exit 1
    fi
}

check_command "mvn" "Maven"
check_command "java" "Java 21+"

# Show Java version
JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[^"]*')
echo "Using Java: $JAVA_VERSION"

# Optional: Check for YouTube support tools
echo ""
echo "YouTube HD Player Support:"
if command -v vlc &> /dev/null && command -v yt-dlp &> /dev/null; then
    echo "✓ VLC and yt-dlp detected - HD playback enabled"
else
    echo "⚠ Install for YouTube HD playback:"
    echo "  sudo apt-get install vlc yt-dlp"
fi

echo ""
echo "Building Pinora Browser (with parallel compilation)..."

# Build with optimization flags
mvn clean package \
    -DskipTests \
    -T 1C \
    -q \
    -Dmaven.compiler.fork=true \
    -Dmaven.compiler.debug=false \
    -Dmaven.compiler.optimize=true

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✓ Build successful!"
    echo "=========================================="
    JAR_FILE=$(find target -name "pinora-browser-*.jar" -type f | head -1)
    if [ -f "$JAR_FILE" ]; then
        SIZE=$(du -h "$JAR_FILE" | cut -f1)
        echo "Output: $JAR_FILE ($SIZE)"
    fi
    
    # Create optimized run script
    echo ""
    echo "Creating launcher script..."
    cat > pinora-browser.sh << 'EOF'
#!/bin/bash
# Optimized Pinora Browser launcher for Linux
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Use best available JVM flags for performance
JVM_FLAGS="-Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35"

# Enable hardware acceleration
JVM_FLAGS="$JVM_FLAGS -Djavafx.graphics.fullscreen=false -Dprism.order=d3d,es2,sw"

# Find JAR file
JAR_FILE=""
if [ -f "$DIR/target/pinora-browser-1.0.0.jar" ]; then
    JAR_FILE="$DIR/target/pinora-browser-1.0.0.jar"
elif [ -f "$DIR/pinora-browser-1.0.0.jar" ]; then
    JAR_FILE="$DIR/pinora-browser-1.0.0.jar"
else
    JAR_FILE=$(find "$DIR" -name "pinora-browser-*.jar" -type f | head -1)
fi

if [ -z "$JAR_FILE" ]; then
    echo "Error: pinora-browser JAR not found"
    exit 1
fi

exec java $JVM_FLAGS -jar "$JAR_FILE"
EOF
    chmod +x pinora-browser.sh
    
    echo "✓ Launcher created: ./pinora-browser.sh"
    echo ""
    echo "To run: ./pinora-browser.sh"
else
    echo ""
    echo "=========================================="
    echo "✗ Build failed!"
    echo "=========================================="
    exit 1
fi
