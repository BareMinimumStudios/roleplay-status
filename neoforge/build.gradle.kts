plugins {
    java
    id("net.neoforged.moddev")
}

val neoforge_version = providers.gradleProperty("neoforge_version").get()
val fzzy_neoforge_version = providers.gradleProperty("fzzy_neoforge_version").get()
val kotlin_for_forge_version = providers.gradleProperty("kotlin_for_forge_version").get()

neoForge {
    version = neoforge_version
    runs {
        create("client") { client() }
        create("server") {
            server()
            programArgument("--nogui")
        }
    }
    val roleplayStatus = mods.create("roleplay_status") {
        sourceSet(sourceSets.main.get())
    }
    unitTest {
        enable()
        testedMod = roleplayStatus
    }
}

dependencies {
    implementation("maven.modrinth:fzzy-config:$fzzy_neoforge_version")
    implementation("thedarkcolour:kotlinforforge-neoforge:$kotlin_for_forge_version")
}
