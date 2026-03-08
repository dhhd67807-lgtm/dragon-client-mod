# Dragon Client Mod - Deployment Guide

## Overview

This guide walks you through deploying the Dragon Client mod to a separate GitHub repository with automatic builds and releases.

## Prerequisites

- GitHub account
- Git installed locally
- Java 21 installed (for local testing)

## Step 1: Create GitHub Repository

1. Go to https://github.com/new
2. Repository name: `dragon-client-mod` (or your preferred name)
3. Description: "Multi-version Minecraft mod with modern UI and auto-updates"
4. Visibility: Public (required for GitHub Actions free tier)
5. **DO NOT** initialize with README, .gitignore, or license
6. Click "Create repository"

## Step 2: Run Setup Script

```bash
cd dragon-client-mod
./setup-repo.sh
```

When prompted, enter your repository URL:
```
https://github.com/YOUR_USERNAME/dragon-client-mod.git
```

The script will:
- Initialize git repository
- Create .gitignore
- Add remote origin
- Create initial commit
- Create v1.0.0 tag
- Update workflow with your repo URL

## Step 3: Push to GitHub

```bash
# Push main branch
git push -u origin main

# Push initial tag
git push origin v1.0.0
```

## Step 4: Enable GitHub Actions

1. Go to your repository on GitHub
2. Click "Settings" tab
3. Navigate to "Actions" → "General"
4. Under "Workflow permissions":
   - Select "Read and write permissions"
   - Check "Allow GitHub Actions to create and approve pull requests"
5. Click "Save"

## Step 5: Test the Workflow

Make a small change to trigger the build:

```bash
# Add a comment to any file
echo "# Test build" >> README.md

# Commit and push
git add README.md
git commit -m "test: trigger initial build"
git push origin main
```

Check the Actions tab in GitHub to see the build progress.

## Step 6: Verify Release

After the workflow completes:

1. Go to the "Releases" section
2. You should see release "v1.0.1" with:
   - 9 JAR files (one for each Minecraft version)
   - versions.json manifest file

## Step 7: Update Launcher

Update the manifest URL in your launcher:

1. Open `src-tauri/src/minecraft/dragon_updater.rs`
2. Change line 13:
   ```rust
   const MANIFEST_URL: &str = "https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest/download/versions.json";
   ```
3. Replace `YOUR_USERNAME/dragon-client-mod` with your actual repository path

## Step 8: Test Auto-Update

1. Build and run your launcher
2. Create a Minecraft instance with Dragon Client
3. The launcher should detect the latest version
4. Click "Update Now" to test the download

## Workflow Details

### Automatic Version Increment

The workflow automatically increments the patch version:
- v1.0.0 → v1.0.1 → v1.0.2 → etc.

To manually bump major/minor versions:

```bash
# Bump to 2.0.0
git tag v2.0.0
git push origin v2.0.0

# Next auto-increment will be 2.0.1
```

### Build Matrix

The workflow builds all versions in parallel:
- 1.21.1-fabric
- 1.21.3-fabric
- 1.21.4-fabric
- 1.21.6-fabric
- 1.21.7-fabric
- 1.21.8-fabric
- 1.21.9-fabric
- 1.21.10-fabric
- 1.21.11-fabric

### Release Assets

Each release includes:
- 9 JAR files (one per Minecraft version)
- versions.json manifest with:
  - Version number
  - Timestamp
  - Download URLs for each Minecraft version
  - SHA256 hashes for verification

## Troubleshooting

### Build Fails

**Check Java Version:**
```bash
java -version  # Should be 21
```

**Check Gradle:**
```bash
cd dragon-client-mod
./gradlew --version
```

**View Build Logs:**
- Go to Actions tab in GitHub
- Click on the failed workflow
- Expand the failed step to see error details

### Workflow Not Triggering

**Check Workflow File Location:**
```bash
ls -la .github/workflows/build-dragon-client.yml
```

**Check Branch Name:**
The workflow triggers on `main` or `master` branch. If your default branch is different, update the workflow:

```yaml
on:
  push:
    branches: [ main, master, YOUR_BRANCH_NAME ]
```

### Release Not Created

**Check Permissions:**
- Settings → Actions → General
- Ensure "Read and write permissions" is enabled

**Check Tags:**
```bash
git tag  # Should show v1.0.0 or higher
```

### Launcher Can't Download

**Check Manifest URL:**
- Open in browser: `https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest/download/versions.json`
- Should return JSON, not 404

**Check CORS:**
GitHub releases are CORS-enabled by default, but if you have issues:
- Ensure repository is public
- Check browser console for errors

## Manual Build (Local Testing)

Before pushing, test builds locally:

```bash
cd dragon-client-mod

# Build all versions
./build-all.sh

# Build specific version
./gradlew :1.21.11-fabric:build

# Check output
ls -lh versions/1.21.11-fabric/build/libs/
```

## Continuous Deployment

Every push to `dragon-client-mod/**` triggers:
1. Version increment
2. Build all 9 versions
3. Create GitHub release
4. Upload JARs and manifest

Users with the launcher will automatically receive updates!

## Security Notes

- All downloads are verified with SHA256 hashes
- Only HTTPS URLs are used
- Failed verifications don't break existing installations
- Old JARs are removed before installing new ones

## Support

If you encounter issues:
1. Check the Actions tab for build logs
2. Review this deployment guide
3. Check AUTO_UPDATE.md for launcher integration details
4. Verify all URLs are correct in both workflow and launcher

## Next Steps

After successful deployment:
1. Add a README.md to your mod repository
2. Add screenshots/GIFs of the mod in action
3. Create a CHANGELOG.md to track changes
4. Consider adding a Discord/community link
5. Share your mod with the community!

---

**Repository Structure:**
```
dragon-client-mod/
├── .github/workflows/
│   └── build-dragon-client.yml  # Auto-build workflow
├── versions/
│   ├── 1.21.1-fabric/
│   ├── 1.21.3-fabric/
│   └── ... (9 versions total)
├── build-all.sh                 # Local build script
├── setup-repo.sh                # Repository setup script
├── DEPLOYMENT.md                # This file
└── AUTO_UPDATE.md               # Launcher integration guide
```

Happy modding! 🐉
