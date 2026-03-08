plugins {
    id("fabric-loom") version "1.11.7"
    kotlin("jvm") version "2.0.21"
}

val mcVersion = project.name.substringBefore("-")
val mcVersionInt = mcVersion.replace(".", "").toInt()

base.archivesName.set("dragon-client-$mcVersion")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net")
}

loom {
    // Don't use split sources - causes import issues
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    
    // Mixin Extras for @Local annotation support
    implementation("io.github.llamalad7:mixinextras-fabric:0.4.1")
    annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.4.1")
    include("io.github.llamalad7:mixinextras-fabric:0.4.1")
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
