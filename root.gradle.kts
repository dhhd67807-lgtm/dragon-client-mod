plugins {
    id("base")
}

version = "1.0.0"

allprojects {
    group = "com.dragonclient"
    version = rootProject.version
}

allprojects {
    tasks.withType<AbstractArchiveTask> {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}
