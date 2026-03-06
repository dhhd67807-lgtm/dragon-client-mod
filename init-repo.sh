#!/bin/bash

# Initialize git repository for Dragon Client mod
echo "Initializing Dragon Client mod repository..."

# Initialize git
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit: Dragon Client mod v1.0.0

Features:
- 13 HUD modules (FPS, CPS, Ping, Coordinates, etc.)
- 9 Visual modules (Zoom, Fullbright, Weather Changer, etc.)
- 3 Movement modules (Auto Sprint, Freelook, No Fall)
- 2 Player modules (Auto Respawn, Arrow Count)
- 5 Misc modules (Chat Timestamps, FPS Limiter, etc.)
- Multi-version support (Minecraft 1.16.5+)
- Client-side only, safe for multiplayer
- GitHub Actions CI/CD for automatic builds"

# Instructions
echo ""
echo "Repository initialized!"
echo ""
echo "Next steps:"
echo "1. Create a new repository on GitHub (e.g., yourusername/dragon-client-mod)"
echo "2. Run these commands:"
echo ""
echo "   git remote add origin https://github.com/yourusername/dragon-client-mod.git"
echo "   git branch -M main"
echo "   git push -u origin main"
echo ""
echo "3. Create a release:"
echo "   - Go to GitHub repository > Releases > Create a new release"
echo "   - Tag: v1.0.0"
echo "   - Title: Dragon Client v1.0.0"
echo "   - Description: Initial release"
echo "   - Click 'Publish release'"
echo ""
echo "4. GitHub Actions will automatically build and attach the JAR to the release!"
echo ""
