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

var devVersion = "1.21.5"

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    shared {
        vers("dev", devVersion)
        versions("1.21.5", "1.21.2", "1.21")
        vcsVersion = devVersion
    }
    create(rootProject)
}

rootProject.name = "Zombies Can't Gather"
