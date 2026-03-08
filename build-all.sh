#!/bin/bash
# Build all supported Dragon Client versions

echo "Building Dragon Client for multiple Minecraft versions..."
echo ""

# Working versions
VERSIONS=(
    "1.21.1-fabric"
    "1.21.3-fabric"
    "1.21.4-fabric"
)

# Build each version
for version in "${VERSIONS[@]}"; do
    echo "=========================================="
    echo "Building $version..."
    echo "=========================================="
    ./gradlew :$version:build --no-daemon
    if [ $? -eq 0 ]; then
        echo "✓ $version built successfully"
    else
        echo "✗ $version build failed"
    fi
    echo ""
done

echo "=========================================="
echo "Build Summary"
echo "=========================================="
echo "Built JARs:"
ls -lh versions/*/build/libs/*.jar 2>/dev/null | grep -v sources | awk '{print $9, "(" $5 ")"}'
echo ""
echo "Done!"
