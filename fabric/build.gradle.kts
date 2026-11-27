plugins {
    id("fabric-loom")
    id("dev.mixinmcp.decompile")
}

val minecraft_version = providers.gradleProperty("minecraft_version").get()
val loader_version = providers.gradleProperty("loader_version").get()
val fabric_api_version = providers.gradleProperty("fabric_api_version").get()
val fabric_kotlin_version = providers.gradleProperty("fabric_kotlin_version").get()
val fzzy_config_version = providers.gradleProperty("fzzy_config_version").get()

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")
    modImplementation("me.fzzyhmstrs:fzzy_config:$fzzy_config_version")
}
