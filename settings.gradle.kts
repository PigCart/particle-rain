pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.parchmentmc.org")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8"
}

stonecutter {
    create(rootProject) {
        fun mc(version: String, vararg loaders: String) = loaders
            .forEach { version("$version-$it", version).buildscript = "build.$it.gradle.kts" }

        mc("1.21.11", "fabric")
        mc("1.21.9", "fabric")
        mc("1.21.6", "fabric", "neoforge")
        mc("1.21.5", "fabric", "neoforge")
        mc("1.21.4", "fabric", "neoforge")
        mc("1.21.1", "fabric", "neoforge")
        mc("1.20.1", "fabric", "forge")

        vcsVersion = "1.20.1-fabric"
    }
}
