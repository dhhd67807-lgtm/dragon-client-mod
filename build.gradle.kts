plugins {
    id("fabric-loom") version "1.12.7"
    kotlin("jvm") version "2.0.21"
}

val mcVersion = project.name.substringBefore("-")
val mcVersionInt = mcVersion.replace(".", "").toInt()

base.archivesName.set("dragon-client-$mcVersion")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    // Bundle Continuity per MC version so CTM works without a separate mods jar.
    val continuityByProject = mapOf(
        "1.21.1-fabric" to "maven.modrinth:continuity:3.0.0+1.21",
        "1.21.3-fabric" to "maven.modrinth:continuity:3.0.0+1.21.3",
        "1.21.4-fabric" to "maven.modrinth:continuity:3.0.0+1.21.4",
        "1.21.6-fabric" to "maven.modrinth:continuity:3.0.1-beta.1+1.21.6",
        "1.21.7-fabric" to "maven.modrinth:continuity:3.0.1-beta.1+1.21.6",
        "1.21.8-fabric" to "maven.modrinth:continuity:3.0.1-beta.1+1.21.6",
        "1.21.10-fabric" to "maven.modrinth:continuity:3.0.1-beta.2+1.21.10"
    )
    continuityByProject[project.name]?.let { continuity ->
        modImplementation(continuity)
        include(continuity)
    }
    
    // Mixin Extras for @Local annotation support
    implementation("io.github.llamalad7:mixinextras-fabric:0.4.1")
    annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.4.1")
    include("io.github.llamalad7:mixinextras-fabric:0.4.1")
    
    // Bundle performance mods (jar-in-jar) - version specific
    val libsDir = file("libs")
    if (libsDir.exists()) {
        fileTree(libsDir) {
            include("*.jar")
        }.forEach { jarFile ->
            // Exclude Sodium for 1.21.11 (causes crashes)
            if (mcVersion == "1.21.11" && jarFile.name.contains("sodium", ignoreCase = true)) {
                println("[Dragon] Skipping Sodium for 1.21.11: ${jarFile.name}")
            } else {
                println("[Dragon] Including bundled mod: ${jarFile.name}")
                modImplementation(files(jarFile))
                include(files(jarFile))
            }
        }
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version))
    }
}

tasks.jar {
    manifest {
        attributes(
            "ModSide" to "CLIENT"
        )
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}
