#!/bin/bash

# Build script for Pinora Browser on Linux

echo "=========================================="
echo "Pinora Browser - Linux Build"
echo "=========================================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java is not installed. Please install Java 21 or higher."
    exit 1
fi

echo "Building Pinora Browser..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "=========================================="
    echo "Build successful!"
    echo "Output: target/pinora-browser-1.0.0-jar-with-dependencies.jar"
    echo "=========================================="
    
    # Create run script
    echo "Creating launcher script..."
    cat > pinora-browser.sh << 'EOF'
#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -jar "$DIR/target/pinora-browser-1.0.0-jar-with-dependencies.jar"
EOF
    chmod +x pinora-browser.sh
    
    echo "To run the browser, execute: ./pinora-browser.sh"
else
    echo "Build failed. Please check the error messages above."
    exit 1
fi
