import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    base
    kotlin("jvm") apply false
    id("fabric-loom") apply false
    id("net.neoforged.moddev") apply false
    id("dev.mixinmcp.decompile") version "1.5.0" apply false
}

allprojects {
    group = providers.gradleProperty("maven_group").get()
    version = providers.gradleProperty("mod_version").get()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven("https://api.modrinth.com/maven")
        maven("https://maven.fzzyhmstrs.me/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
    }

    extensions.configure<BasePluginExtension> {
        archivesName.set("${providers.gradleProperty("archives_base_name").get()}-${project.name}")
    }
    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withSourcesJar()
        sourceSets.named("main") {
            java.srcDir(rootProject.file("src/main/java"))
            resources.srcDir(rootProject.file("src/main/resources"))
        }
        sourceSets.named("test") {
            java.srcDir(rootProject.file("src/test/java"))
        }
    }
    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(21)
        sourceSets.named("main") {
            kotlin.srcDir(rootProject.file("src/main/kotlin"))
        }
    }

    dependencies {
        add("compileOnly", "io.github.llamalad7:mixinextras-common:0.5.5")
        add("testImplementation", platform("org.junit:junit-bom:5.14.0"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
        options.encoding = "UTF-8"
    }
    tasks.withType<Test>().configureEach { useJUnitPlatform() }
    tasks.withType<ProcessResources>().configureEach {
        val metadata = mapOf("version" to project.version.toString())
        filteringCharset = "UTF-8"
        inputs.properties(metadata)
        filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
            expand(metadata)
        }
    }
    tasks.withType<Jar>().configureEach {
        from(rootProject.file("LICENSE"), rootProject.file("NOTICE.md"))
    }
}

tasks.named("build") { dependsOn(":fabric:build", ":neoforge:build") }
tasks.named("clean") { dependsOn(":fabric:clean", ":neoforge:clean") }
