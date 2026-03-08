# Dragon Client Multi-Version Build

## Current Status

### ✅ Fully Supported
- **1.21.1** - All features working (GUI, textures, capes, modules, HUD)

### 🔄 Prepared But Not Built
- 1.21.2 through 1.21.11 folders created with gradle.properties

## Version Compatibility

### Loom 1.7 (Current - Gradle 8.8)
- ✅ 1.21.1
- ❌ 1.21.2 (no mappings)
- ❌ 1.21.3+ (API changes: drawTexture, entity rendering, limbAnimator)
- ❌ 1.21.5+ (requires Loom 1.10+)
- ❌ 1.21.9+ (requires Loom 1.8+, unpick issues)

### To Support More Versions

**Option 1: Upgrade Gradle & Loom**
- Upgrade to Gradle 8.10+
- Use Loom 1.8+ or 1.10+
- This enables 1.21.5+ support

**Option 2: API Compatibility Layer**
- Create version-specific code for API changes
- Use reflection/abstraction for changed methods
- Similar to Essential's approach

**Option 3: Use 1.21.1 JAR Universally**
- The 1.21.1 build works on 1.21.11
- May have minor compatibility issues
- Simplest approach for now

## API Changes Per Version

### 1.21.3+
- `DrawContext.drawTexture()` signature changed (added RenderLayer parameter)
- `LimbAnimator.updateLimbs()` now requires 3 parameters instead of 2
- `EntityRenderDispatcher.render()` signature changed

### 1.21.5+
- Fabric API modules built with Loom 1.10+
- Requires Gradle 8.10+

### 1.21.9+
- Yarn mappings use unsupported unpick version
- Requires Loom 1.8+ with unpick support

## Building

```bash
# Build 1.21.1 (works on 1.21.1 and 1.21.11)
./gradlew :1.21.1-fabric:build

# Output
versions/1.21.1-fabric/build/libs/dragon-client-1.21.1-1.0.0.jar
```

## Structure

```
dragon-client-mod/
├── build.gradle              # Shared build configuration
├── settings.gradle           # Multi-version project setup
└── versions/
    ├── 1.21.1-fabric/       # ✅ Working
    │   ├── gradle.properties
    │   └── src/
    ├── 1.21.2-fabric/       # Prepared
    ├── 1.21.3-fabric/       # Prepared
    └── ...
```
