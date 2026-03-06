#!/bin/bash

# Push Dragon Client Mod to GitHub
# Make sure you've created the repo at: https://github.com/dhhd67807-lgtm/dragon-client-mod

echo "🚀 Pushing Dragon Client Mod to GitHub..."

# Add remote
git remote add origin https://github.com/dhhd67807-lgtm/dragon-client-mod.git

# Push main branch
echo "📤 Pushing main branch..."
git push -u origin main

# Push tag
echo "🏷️  Pushing v1.0.0 tag..."
git push origin v1.0.0

echo ""
echo "✅ Done! Check your repository at:"
echo "   https://github.com/dhhd67807-lgtm/dragon-client-mod"
echo ""
echo "🔨 GitHub Actions will now build the mod JAR"
echo "📦 Release will be created at:"
echo "   https://github.com/dhhd67807-lgtm/dragon-client-mod/releases/tag/v1.0.0"
echo ""
echo "⏳ Wait 2-3 minutes for the build to complete, then your launcher will auto-download it!"
