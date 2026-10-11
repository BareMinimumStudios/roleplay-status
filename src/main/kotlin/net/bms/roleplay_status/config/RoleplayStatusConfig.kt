package net.bms.roleplay_status.config

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.fzzyhmstrs.fzzy_config.annotations.RootConfig
import me.fzzyhmstrs.fzzy_config.annotations.Version
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava
import me.fzzyhmstrs.fzzy_config.api.FileType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.config.ConfigSection
import me.fzzyhmstrs.fzzy_config.util.Translatable
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedColor
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString
import net.bms.roleplay_status.RoleplayStatus
import net.bms.roleplay_status.StatusCatalog
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import java.nio.file.Files

@RootConfig
@Version(version = 1)
@Translatable.Name("Roleplay Status")
@Translatable.Desc("Server-owned status labels and colors. Operators with permission level 2 can edit these settings. Accepted changes apply live.")
class RoleplayStatusConfig : Config(ResourceLocation.fromNamespaceAndPath(RoleplayStatus.MOD_ID, "statuses")) {
    @Translatable.Name("In Character")
    var inCharacter = StatusEntry("In Character", 0x2EEC23)
    @Translatable.Name("Out of Character")
    var outOfCharacter = StatusEntry("Out of Character", 0xFF3347)
    @Translatable.Name("Open to RP")
    var openToRoleplay = StatusEntry("Open to RP", 0xFFC83D)
    @Translatable.Name("Busy / AFK")
    var busyAfk = StatusEntry("Busy / AFK", 0x8A8A8A)
    @Translatable.Name("Event")
    var event = StatusEntry("Event", 0xA855F7)
    @Translatable.Name("New")
    var newPlayer = StatusEntry("New", 0x42D9FF)
    @Translatable.Name("Recording")
    var recording = StatusEntry("Recording", 0xFF0014)
    @Translatable.Name("Streaming")
    var streaming = StatusEntry("Streaming", 0x8821FF)

    override fun fileType() = FileType.JSON
    override fun defaultPermLevel() = 2
    private fun entries() = listOf(
        StatusCatalog.IC to inCharacter, StatusCatalog.OOC to outOfCharacter,
        StatusCatalog.OPEN_RP to openToRoleplay, StatusCatalog.BUSY_AFK to busyAfk,
        StatusCatalog.EVENT to event, StatusCatalog.NEW to newPlayer,
        StatusCatalog.RECORDING to recording, StatusCatalog.STREAMING to streaming
    )
    fun definitions() = entries().map { (id, entry) -> entry.definition(id) }
    override fun onSyncClient() { clientListener(definitions()) }
    override fun onUpdateClient() { clientListener(definitions()) }

    companion object {
        private var clientListener: (List<StatusCatalog.Definition>) -> Unit = {}
        fun setClientListener(listener: (List<StatusCatalog.Definition>) -> Unit) { clientListener = listener }

        fun register(): RoleplayStatusConfig {
            val dir = FabricLoader.getInstance().configDir
            val target = dir.resolve("roleplay_status/statuses.json")
            val migrate = listOf("json", "toml", "json5", "jsonc").none {
                Files.exists(target.resolveSibling("statuses.$it"))
            }
            val config = RoleplayStatusConfig()
            val old = dir.resolve("andromeda_status/statuses.json")
            if (migrate && Files.isRegularFile(old)) {
                try {
                    Files.newBufferedReader(old).use { reader ->
                        val legacy = requireNotNull(JsonParser.parseReader(reader).asJsonObject.getAsJsonObject("statuses")) {
                            "Missing statuses object"
                        }
                        config.entries().forEach { (id, entry) -> entry.copyFrom(legacy, id) }
                    }
                    RoleplayStatus.LOGGER.info("Migrated Roleplay Status configuration from {} (original preserved)", old)
                } catch (e: Exception) {
                    RoleplayStatus.LOGGER.error("Could not migrate {}; original preserved", old, e)
                }
            }
            // Seed before registration to avoid racing Fzzy's asynchronous initial save.
            if (migrate) config.save()
            return ConfigApiJava.registerAndLoadConfig(::RoleplayStatusConfig)
        }
    }

    class StatusEntry(name: String, rgb: Int) : ConfigSection() {
        @Translatable.Name("Label")
        @Translatable.Desc("Display name, 1–64 characters. Used by the status screen and its tooltips.")
        var label = ValidatedString(name, "[^\\p{Cc}\\p{Cf}§]{1,64}")
        @Translatable.Name("Color")
        @Translatable.Desc("Status icon and screen swatch color. Choose a color or enter its RGB hex value.")
        var color = ValidatedColor((rgb ushr 16) and 255, (rgb ushr 8) and 255, rgb and 255).withDyeColorPresets()

        fun definition(id: String) = StatusCatalog.Definition(
            id, StatusCatalog.sanitizeLabel(label.get(), StatusCatalog.defaultMap().getValue(id).label), color.toInt()
        )

        fun copyFrom(legacy: JsonObject, id: String) {
            val value = legacy.get(id.replace("roleplay_status:", "andromeda:")) ?: return
            if (!value.isJsonObject) return
            val entry = value.asJsonObject
            val oldLabel = entry.get("label")
            if (oldLabel?.isJsonPrimitive == true && oldLabel.asJsonPrimitive.isString) {
                label.validateAndSet(StatusCatalog.sanitizeLabel(oldLabel.asString, label.get()))
            }
            val oldColor = entry.get("color")
            if (oldColor?.isJsonPrimitive == true && oldColor.asJsonPrimitive.isString) {
                val hex = oldColor.asString.trim().removePrefix("#")
                if (hex.matches(Regex("[0-9a-fA-F]{6}"))) {
                    val rgb = hex.toInt(16)
                    // Fzzy 0.7.7 ColorHolder.fromInt copies green into blue; retain all RGB channels.
                    color.validateAndSet(ValidatedColor.ColorHolder((rgb ushr 16) and 255,
                        (rgb ushr 8) and 255, rgb and 255, 255, false))
                }
            }
        }
    }
}
