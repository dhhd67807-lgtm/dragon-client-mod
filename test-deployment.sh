#!/bin/bash

# Dragon Client Mod - Pre-Deployment Test Script
# Run this before deploying to verify everything works

set -e

echo "=========================================="
echo "Dragon Client Mod - Pre-Deployment Tests"
echo "=========================================="
echo ""

FAILED=0

# Test 1: Check Java version
echo "Test 1: Checking Java version..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        echo "✓ Java $JAVA_VERSION detected"
    else
        echo "✗ Java 21+ required (found Java $JAVA_VERSION)"
        FAILED=1
    fi
else
    echo "✗ Java not found"
    FAILED=1
fi
echo ""

# Test 2: Check Gradle wrapper
echo "Test 2: Checking Gradle wrapper..."
if [ -f "gradlew" ]; then
    echo "✓ Gradle wrapper found"
    chmod +x gradlew
else
    echo "✗ Gradle wrapper not found"
    FAILED=1
fi
echo ""

# Test 3: Check all version directories
echo "Test 3: Checking version directories..."
VERSIONS=("1.21.1" "1.21.3" "1.21.4" "1.21.5" "1.21.8" "1.21.9" "1.21.10" "1.21.11")
for VERSION in "${VERSIONS[@]}"; do
    if [ -d "versions/${VERSION}-fabric" ]; then
        echo "✓ ${VERSION}-fabric exists"
    else
        echo "✗ ${VERSION}-fabric missing"
        FAILED=1
    fi
done
echo ""

# Test 4: Build a single version (fastest test)
echo "Test 4: Building 1.21.11-fabric (test build)..."
if ./gradlew :1.21.11-fabric:build --quiet; then
    echo "✓ Build successful"
    
    # Check JAR exists
    JAR_PATH="versions/1.21.11-fabric/build/libs"
    if ls $JAR_PATH/*.jar 1> /dev/null 2>&1; then
        JAR_SIZE=$(du -h $JAR_PATH/*.jar | cut -f1)
        echo "✓ JAR created ($JAR_SIZE)"
    else
        echo "✗ JAR not found"
        FAILED=1
    fi
else
    echo "✗ Build failed"
    FAILED=1
fi
echo ""

# Test 5: Check required files
echo "Test 5: Checking required files..."
REQUIRED_FILES=(
    "build-all.sh"
    "setup-repo.sh"
    "settings.gradle.kts"
    "build.gradle.kts"
    "root.gradle.kts"
    "README.md"
    "DEPLOYMENT.md"
    "AUTO_UPDATE.md"
)

for FILE in "${REQUIRED_FILES[@]}"; do
    if [ -f "$FILE" ]; then
        echo "✓ $FILE exists"
    else
        echo "✗ $FILE missing"
        FAILED=1
    fi
done
echo ""

# Test 6: Check GitHub workflow
echo "Test 6: Checking GitHub Actions workflow..."
WORKFLOW="../.github/workflows/build-dragon-client.yml"
if [ -f "$WORKFLOW" ]; then
    echo "✓ Workflow file exists"
    
    # Check if it has the placeholder
    if grep -q "YOUR_USERNAME/YOUR_REPO" "$WORKFLOW"; then
        echo "⚠ Workflow still has placeholder URL (will be updated by setup script)"
    else
        echo "✓ Workflow URL configured"
    fi
else
    echo "✗ Workflow file missing"
    FAILED=1
fi
echo ""

# Test 7: Check for common issues
echo "Test 7: Checking for common issues..."

# Check for .DS_Store files
if find . -name ".DS_Store" | grep -q .; then
    echo "⚠ Found .DS_Store files (will be ignored by .gitignore)"
else
    echo "✓ No .DS_Store files"
fi

# Check for build artifacts
if find versions -name "build" -type d | grep -q .; then
    echo "⚠ Found build directories (will be ignored by .gitignore)"
else
    echo "✓ No build artifacts"
fi

echo ""

# Summary
echo "=========================================="
if [ $FAILED -eq 0 ]; then
    echo "✅ All tests passed!"
    echo "=========================================="
    echo ""
    echo "Ready to deploy! Next steps:"
    echo ""
    echo "1. Run setup script:"
    echo "   ./setup-repo.sh"
    echo ""
    echo "2. Follow the prompts to configure your repository"
    echo ""
    echo "3. See DEPLOYMENT_QUICKSTART.md for full instructions"
    echo ""
else
    echo "❌ Some tests failed"
    echo "=========================================="
    echo ""
    echo "Please fix the issues above before deploying."
    echo ""
fi

exit $FAILED
