plugins {
    id("base")
}

version = "4.6.2"

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
