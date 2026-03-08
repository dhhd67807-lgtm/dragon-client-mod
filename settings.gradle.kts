pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net")
    }
}

rootProject.name = "dragon-client"
rootProject.buildFileName = "root.gradle.kts"

// Include all 1.21.x versions
listOf(
    "1.21.1-fabric",
    "1.21.3-fabric",
    "1.21.4-fabric",
    "1.21.6-fabric",
    "1.21.7-fabric",
    "1.21.8-fabric",
    "1.21.10-fabric"
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}

// 1.21.11 uses its own build.gradle.kts (Loom 1.12+)
include(":1.21.11-fabric")
project(":1.21.11-fabric").apply {
    projectDir = file("versions/1.21.11-fabric")
}
