# Dragon Client Mod

A modern, multi-version Minecraft Fabric mod with a sleek UI, module system, and cosmetics.

![Version](https://img.shields.io/github/v/release/YOUR_USERNAME/dragon-client-mod?label=version)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.1--1.21.11-green)
![License](https://img.shields.io/badge/license-MIT-blue)

## ✨ Features

### 🎮 Module System
- **HUD Modules**: FPS Counter, CPS Counter, Armor Status, Keystrokes
- **Visual Modules**: Fullbright, No Weather, Custom UI
- **Movement Modules**: Sprint Toggle, Fly (Creative)
- **Player Modules**: Auto-respawn, Fast Eat
- **Misc Modules**: Zoom, Time Changer

### 🎨 Modern UI
- Custom Bebas Neue font
- Smooth animations and transitions
- Rounded corners and modern styling
- Color-coded categories
- Responsive design

### 👕 Cosmetics System
- 4 unique capes: Dragon, Fire, Ice, Shadow
- 3D preview with rotating mannequin
- Easy equip/unequip system
- Persistent across sessions

### 🔄 Auto-Update System
- Automatic version detection
- One-click updates via launcher
- SHA256 verification for security
- Seamless installation

## 📦 Supported Versions

| Minecraft Version | Status | Download |
|-------------------|--------|----------|
| 1.21.1 | ✅ Stable | [Download](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest) |
| 1.21.3 | ✅ Stable | [Download](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest) |
| 1.21.4 | ✅ Stable | [Download](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest) |
| 1.21.6 | ✅ Stable | [Download](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest) |
| 1.21.7 | ✅ Stable | [Download](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest) |
| 1.21.8 | ✅ Stable | [Download](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest) |
| 1.21.9 | ✅ Stable | [Download](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest) |
| 1.21.10 | ✅ Stable | [Download](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest) |
| 1.21.11 | ✅ Stable | [Download](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest) |

## 🚀 Installation

### Automatic (via Launcher)
1. Install the Dragon Launcher
2. Create a Minecraft instance
3. The mod will be automatically installed and updated

### Manual Installation
1. Download the JAR for your Minecraft version from [Releases](https://github.com/YOUR_USERNAME/dragon-client-mod/releases/latest)
2. Install Fabric Loader for your Minecraft version
3. Place the JAR in your `.minecraft/mods` folder
4. Launch Minecraft with Fabric

## 🎮 Usage

### Opening the Menu
- Press `Right Shift` to open the Dragon Client menu
- Or use `/dragonclient` command in-game

### Navigating the UI
- **MODS Tab**: Enable/disable modules
- **CAPES Tab**: Equip cosmetics
- **HUD Tab**: Configure HUD elements (coming soon)

### Module Controls
- Click the toggle button on any module card to enable/disable
- Enabled modules show a white "ENABLED" button
- Disabled modules show a gray "DISABLED" button

## 🛠️ Building from Source

### Prerequisites
- Java 21
- Gradle 9.3.1 (included via wrapper)

### Build All Versions
```bash
./build-all.sh
```

### Build Specific Version
```bash
./gradlew :1.21.11-fabric:build
```

### Output Location
```
versions/{version}-fabric/build/libs/dragon-client-{version}-{mod_version}.jar
```

## 🔧 Development

### Project Structure
```
dragon-client-mod/
├── versions/
│   ├── 1.21.1-fabric/    # Full source for 1.21.1
│   ├── 1.21.3-fabric/    # Full source for 1.21.3
│   └── ...               # 9 versions total
├── build-all.sh          # Build script
└── settings.gradle.kts   # Multi-project setup
```

### Adding a New Module
1. Create module class in `com.dragonclient.module`
2. Extend `Module` base class
3. Register in `ModuleManager`
4. Add to appropriate category

### API Changes Between Versions
See [MULTIVERSION.md](MULTIVERSION.md) for detailed API compatibility notes.

## 📝 Changelog

See [Releases](https://github.com/YOUR_USERNAME/dragon-client-mod/releases) for version history.

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test on all supported versions
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- **Fabric API**: Modding framework
- **Essential Mod**: Inspiration for input handling
- **Bebas Neue Font**: Custom UI font
- **Community**: Testing and feedback

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/YOUR_USERNAME/dragon-client-mod/issues)
- **Discord**: [Join our server](#) (coming soon)
- **Documentation**: [Wiki](https://github.com/YOUR_USERNAME/dragon-client-mod/wiki) (coming soon)

## 🎯 Roadmap

- [ ] HUD editor with drag-and-drop
- [ ] More cosmetics (hats, particles)
- [ ] Config system for modules
- [ ] Multiplayer support
- [ ] More Minecraft versions (1.20.x, 1.19.x)
- [ ] Forge support

---

Made with ❤️ by the Dragon Client team
