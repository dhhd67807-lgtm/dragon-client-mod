#!/bin/bash

# Dragon Client Mod - Repository Setup Script
# This script initializes a new git repository for the mod

set -e

echo "🐉 Dragon Client Mod - Repository Setup"
echo "========================================"
echo ""

# Check if we're in the right directory
if [ ! -f "build-all.sh" ]; then
    echo "❌ Error: Please run this script from the dragon-client-mod directory"
    exit 1
fi

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "❌ Error: git is not installed"
    exit 1
fi

# Get repository URL from user
echo "Enter your GitHub repository URL:"
echo "Example: https://github.com/YOUR_USERNAME/dragon-client-mod.git"
read -p "Repository URL: " REPO_URL

if [ -z "$REPO_URL" ]; then
    echo "❌ Error: Repository URL cannot be empty"
    exit 1
fi

# Extract username and repo name from URL
REPO_PATH=$(echo "$REPO_URL" | sed -E 's|https://github.com/||' | sed 's|.git$||')
echo ""
echo "Repository: $REPO_PATH"
echo ""

# Initialize git repository
echo "📦 Initializing git repository..."
git init

# Create .gitignore
echo "📝 Creating .gitignore..."
cat > .gitignore << 'EOF'
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
!**/src/main/**/build/
!**/src/test/**/build/

# IDE
.idea/
*.iml
*.ipr
*.iws
.vscode/
.DS_Store

# Minecraft
run/
logs/
crash-reports/

# Compiled
*.class
*.jar
!gradle/wrapper/gradle-wrapper.jar

# Temporary
*.tmp
*.bak
*.swp
*~
EOF

# Add all files
echo "➕ Adding files to git..."
git add .

# Create initial commit
echo "💾 Creating initial commit..."
git commit -m "Initial commit: Dragon Client multi-version mod

- Support for Minecraft 1.21.1, 1.21.3, 1.21.4, 1.21.6, 1.21.7, 1.21.8, 1.21.10, 1.21.11
- Custom skin system with local storage
- Modern UI with HUD editor
- Cosmetics system
- Auto-update support via GitHub releases"

# Add remote
echo "🔗 Adding remote origin..."
git remote add origin "$REPO_URL"

# Create initial tag
echo "🏷️  Creating initial tag v1.0.0..."
git tag v1.0.0

echo ""
echo "✅ Repository setup complete!"
echo ""
echo "Next steps:"
echo "1. Push to GitHub:"
echo "   git push -u origin main"
echo "   git push origin v1.0.0"
echo ""
echo "2. Enable GitHub Actions:"
echo "   - Go to: https://github.com/$REPO_PATH/settings/actions"
echo "   - Under 'Workflow permissions', select 'Read and write permissions'"
echo "   - Check 'Allow GitHub Actions to create and approve pull requests'"
echo "   - Click 'Save'"
echo ""
echo "3. Trigger first build:"
echo "   - Make a small change and push, or"
echo "   - Go to Actions tab and manually trigger the workflow"
echo ""
echo "4. Update launcher code:"
echo "   - Edit src-tauri/src/minecraft/dragon_mod_installer.rs"
echo "   - Change DRAGON_CLIENT_REPO to: \"$REPO_PATH\""
echo ""
