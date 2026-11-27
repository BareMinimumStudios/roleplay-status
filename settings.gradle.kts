pluginManagement {
    val kotlin_version = providers.gradleProperty("kotlin_version").get()
    val loom_version = providers.gradleProperty("loom_version").get()
    val moddev_version = providers.gradleProperty("moddev_version").get()
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlin_version
        id("fabric-loom") version loom_version
        id("net.neoforged.moddev") version moddev_version
    }
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.muon.rip/releases")
        gradlePluginPortal()
    }
}

rootProject.name = "roleplay-status"
include("fabric", "neoforge")
