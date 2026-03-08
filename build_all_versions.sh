#!/bin/bash

# Array of Minecraft versions to build
versions=("1.21.1" "1.21.2" "1.21.4")

# Create output directory
mkdir -p builds

for version in "${versions[@]}"; do
    echo "Building for Minecraft $version..."
    
    # Update gradle.properties
    sed -i.bak "s/minecraft_version=.*/minecraft_version=$version/" gradle.properties
    
    # Update yarn mappings based on version
    case $version in
        "1.21.1")
            sed -i.bak "s/yarn_mappings=.*/yarn_mappings=1.21.1+build.3/" gradle.properties
            sed -i.bak "s/fabric_version=.*/fabric_version=0.107.0+1.21.1/" gradle.properties
            ;;
        "1.21.2")
            sed -i.bak "s/yarn_mappings=.*/yarn_mappings=1.21.2+build.4/" gradle.properties
            sed -i.bak "s/fabric_version=.*/fabric_version=0.107.2+1.21.2/" gradle.properties
            ;;
        "1.21.4")
            sed -i.bak "s/yarn_mappings=.*/yarn_mappings=1.21.4+build.3/" gradle.properties
            sed -i.bak "s/fabric_version=.*/fabric_version=0.110.5+1.21.4/" gradle.properties
            ;;
    esac
    
    # Clean and build
    ./gradlew clean build
    
    # Copy the built jar
    cp build/libs/dragon-client-1.0.0.jar "builds/dragon-client-1.0.0-mc$version.jar"
    
    echo "Completed build for Minecraft $version"
    echo "---"
done

# Restore original gradle.properties
sed -i.bak "s/minecraft_version=.*/minecraft_version=1.21.1/" gradle.properties
sed -i.bak "s/yarn_mappings=.*/yarn_mappings=1.21.1+build.3/" gradle.properties
sed -i.bak "s/fabric_version=.*/fabric_version=0.107.0+1.21.1/" gradle.properties

echo "All builds completed! Check the 'builds' directory."
