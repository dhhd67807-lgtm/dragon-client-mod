# Dragon Client Auto-Update System

## Overview

The Dragon Client mod now has an automatic update system that:
1. Builds all 9 Minecraft versions on every push to main
2. Auto-increments version numbers
3. Creates GitHub releases with all JARs
4. Provides a manifest file for the launcher to check updates
5. Auto-downloads and installs updates in the launcher

## How It Works

### 1. GitHub Actions Workflow

When you push changes to `dragon-client-mod/**`:

```yaml
# .github/workflows/build-dragon-client.yml
- Detects latest version tag (e.g., v1.0.5)
- Increments patch version (v1.0.6)
- Builds all 9 Minecraft versions in parallel
- Creates a versions.json manifest with download URLs and SHA256 hashes
- Creates a GitHub release with all JARs
```

### 2. Version Manifest

The `versions.json` file contains:

```json
{
  "version": "1.0.6",
  "timestamp": "2024-03-07T12:00:00Z",
  "minecraft_versions": {
    "1.21.1": {
      "url": "https://github.com/YOUR_USERNAME/YOUR_REPO/releases/download/v1.0.6/dragon-client-1.21.1-1.0.6.jar",
      "sha256": "abc123..."
    },
    ...
  }
}
```

### 3. Launcher Integration

The launcher automatically:
- Checks for updates when launching an instance
- Shows a notification if an update is available
- Downloads and verifies the new JAR (SHA256 check)
- Removes old Dragon Client JARs
- Installs the new version

## Setup Instructions

### 1. Update GitHub Repository URL

Edit `.github/workflows/build-dragon-client.yml` and replace:

```yaml
const MANIFEST_URL: &str = "https://github.com/YOUR_USERNAME/YOUR_REPO/releases/latest/download/versions.json";
```

With your actual repository URL.

Also update in `src-tauri/src/minecraft/dragon_updater.rs`:

```rust
const MANIFEST_URL: &str = "https://github.com/YOUR_USERNAME/YOUR_REPO/releases/latest/download/versions.json";
```

### 2. Create Initial Release Tag

```bash
git tag v1.0.0
git push origin v1.0.0
```

### 3. Enable GitHub Actions

1. Go to your repository settings
2. Navigate to Actions → General
3. Enable "Read and write permissions" for workflows
4. Save changes

### 4. Test the Workflow

```bash
# Make a change to any file in dragon-client-mod/
echo "# Test" >> dragon-client-mod/README.md

# Commit and push
git add dragon-client-mod/README.md
git commit -m "test: trigger build"
git push origin main
```

The workflow will:
- Build all versions
- Create release v1.0.1
- Upload all JARs and versions.json

## Usage in Launcher

### Add Update Notification to Instance Page

```tsx
import { DragonClientUpdateNotification } from '../components/DragonClientUpdateNotification';

function InstancePage() {
  const minecraftVersion = "1.21.11"; // Get from instance
  const instancePath = "/path/to/instance"; // Get from instance
  
  return (
    <div>
      {/* Your instance UI */}
      
      <DragonClientUpdateNotification 
        minecraftVersion={minecraftVersion}
        instancePath={instancePath}
      />
    </div>
  );
}
```

### Manual Update Check

```tsx
import { useDragonClientUpdater } from '../hooks/useDragonClientUpdater';

function Settings() {
  const { status, checkForUpdates, updateDragonClient } = useDragonClientUpdater(
    "1.21.11",
    "/path/to/instance"
  );
  
  return (
    <div>
      <button onClick={checkForUpdates}>
        Check for Updates
      </button>
      
      {status.available && (
        <button onClick={updateDragonClient}>
          Update to {status.latestVersion}
        </button>
      )}
    </div>
  );
}
```

## Version Numbering

The system uses semantic versioning:
- **Major**: Breaking changes (manual bump)
- **Minor**: New features (manual bump)
- **Patch**: Bug fixes and updates (auto-incremented)

To manually bump major/minor:

```bash
# Bump to 2.0.0
git tag v2.0.0
git push origin v2.0.0

# Next auto-increment will be 2.0.1
```

## Troubleshooting

### Workflow Fails

Check the Actions tab in GitHub for error logs. Common issues:
- Java version mismatch
- Gradle build errors
- Missing permissions

### Updates Not Detected

1. Check the manifest URL is correct
2. Verify the release was created successfully
3. Check browser console for CORS errors
4. Ensure versions.json is publicly accessible

### SHA256 Verification Fails

The downloaded JAR is corrupted. The system will:
1. Delete the corrupted file
2. Show an error message
3. Keep the old version installed

## Security

- All downloads are verified with SHA256 hashes
- Only HTTPS URLs are used
- Old JARs are removed before installing new ones
- Failed downloads don't break existing installations

## Manual Installation

Users can still manually download JARs from:
```
https://github.com/YOUR_USERNAME/YOUR_REPO/releases
```

Each release includes all 9 Minecraft versions.
