plugins {
    id("base")
}

version = "2.0.76"

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
