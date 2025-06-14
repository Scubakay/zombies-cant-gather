pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7-alpha.22"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    shared {
        vers("dev", "1.21.2")
        versions("1.21.2", "1.21")

    }
    create(rootProject)
}

rootProject.name = "Zombies Can't Gather"
