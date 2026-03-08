# Dragon Client Multi-Version Build System

Dragon Client uses a multi-version build system to support multiple Minecraft versions from a single codebase.

## Supported Versions

Currently building successfully:
- ✅ 1.21.1 (Fabric) - Base version, all features working
- ✅ 1.21.3 (Fabric) - API compatibility layer implemented
- ✅ 1.21.4 (Fabric) - API compatibility layer implemented

Not yet supported (requires additional work):
- ❌ 1.21.6 (Fabric) - Matrix API completely redesigned, requires major refactoring
- ❌ 1.21.7 (Fabric) - Matrix API completely redesigned, requires major refactoring
- ❌ 1.21.9 (Fabric) - Requires Loom 1.8+ for Yarn unpick support
- ❌ 1.21.11 (Fabric) - Requires Loom 1.8+ for Yarn unpick support

## Building

### Build All Versions
```bash
./build-all.sh
```

### Build Specific Version
```bash
./gradlew :1.21.1-fabric:build
./gradlew :1.21.3-fabric:build
./gradlew :1.21.4-fabric:build
```

### Output Location
Built JARs are located at:
```
versions/{version}/build/libs/dragon-client-{version}-1.0.0.jar
```

## Architecture

### Directory Structure
```
dragon-client-mod/
├── build.gradle.kts          # Shared build configuration
├── settings.gradle.kts        # Multi-project setup
├── root.gradle.kts           # Root project configuration
└── versions/
    ├── 1.21.1-fabric/
    │   ├── gradle.properties  # Version-specific properties
    │   └── src/main/java/     # Shared source code
    ├── 1.21.3-fabric/
    │   ├── gradle.properties
    │   └── src/main/java/     # Version-specific overrides
    └── ...
```

### Version-Specific Code

Some Minecraft versions have API changes that require version-specific implementations:

#### 1.21.1 (Base Version)
- Uses standard Minecraft 1.21.1 APIs
- All source code in `versions/1.21.1-fabric/src/`

#### 1.21.3+ Changes
Files with version-specific implementations:
- `DummyPlayerEntity.java` - `LimbAnimator.updateLimbs()` now requires 3 parameters
- `DragonMenuScreen.java` - `DrawContext.drawTexture()` requires RenderLayer parameter
- `DragonClientScreen.java` - `DrawContext.drawTexture()` requires RenderLayer parameter
- `CosmeticsScreen.java` - `DrawContext.drawTexture()` and `EntityRenderDispatcher.render()` signature changes

#### 1.21.6+ Changes (Not Yet Implemented)
- Entity position fields (`prevX`, `prevY`, `prevZ`) removed/renamed
- Requires refactoring of cape physics system

## API Compatibility Layer

### DrawContext.drawTexture()
**1.21.1-1.21.2:**
```java
context.drawTexture(texture, x, y, u, v, width, height, textureWidth, textureHeight);
```

**1.21.3+:**
```java
context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, u, v, width, height, textureWidth, textureHeight);
```

### LimbAnimator.updateLimbs()
**1.21.1-1.21.2:**
```java
limbAnimator.updateLimbs(speed, amount);
```

**1.21.3+:**
```java
limbAnimator.updateLimbs(speed, amount, tickDelta);
```

### EntityRenderDispatcher.render()
**1.21.1-1.21.2:**
```java
dispatcher.render(entity, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light);
```

**1.21.3+:**
```java
dispatcher.render(entity, x, y, z, tickDelta, matrices, vertexConsumers, light);
// Note: yaw parameter removed, parameters reordered
```

## Adding New Versions

1. Create version directory:
```bash
mkdir -p versions/1.21.X-fabric/src/main/java
```

2. Create `gradle.properties`:
```properties
minecraft_version=1.21.X
yarn_mappings=1.21.X+build.Y
loader_version=0.16.14
fabric_version=X.X.X+1.21.X
```

3. Copy source code from nearest version:
```bash
cp -r versions/1.21.1-fabric/src versions/1.21.X-fabric/
```

4. Add to `settings.gradle.kts`:
```kotlin
listOf(
    // ...
    "1.21.X-fabric"
).forEach { version ->
```

5. Test build:
```bash
./gradlew :1.21.X-fabric:build
```

6. If API changes exist, create version-specific implementations in `versions/1.21.X-fabric/src/`

## Troubleshooting

### Unpick Version Issues (1.21.9+)
Some versions use newer Yarn mappings with unsupported unpick versions. This requires Loom 1.8+ which may not be compatible with older versions.

**Workaround:** Use separate build configurations or wait for Loom updates.

### Fabric API Compatibility
Ensure the Fabric API version in `gradle.properties` matches the Minecraft version and was built with a compatible Loom version.

### Build Cache Issues
If you encounter cache lock errors:
```bash
rm -rf ~/.gradle/caches/fabric-loom
./gradlew --stop
```

## Development Workflow

1. Make changes in the base version (1.21.1)
2. Test build: `./gradlew :1.21.1-fabric:build`
3. Copy changes to other versions if needed
4. Build all versions: `./build-all.sh`
5. Test in-game on each version

## Future Improvements

- [ ] Implement preprocessor system for cleaner version-specific code
- [ ] Add 1.21.6+ support with entity API refactoring
- [ ] Add 1.21.9+ support with Loom upgrade
- [ ] Add 1.21.11+ support
- [ ] Automate version-specific code generation
- [ ] Add CI/CD pipeline for multi-version builds
