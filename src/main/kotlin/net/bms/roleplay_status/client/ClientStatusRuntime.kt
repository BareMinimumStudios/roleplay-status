package net.bms.roleplay_status.client

import net.bms.roleplay_status.RoleplayStatus
import net.bms.roleplay_status.StatusCatalog
import net.bms.roleplay_status.config.RoleplayStatusConfig
import net.bms.roleplay_status.network.StatusPayloads
import net.bms.roleplay_status.state.PlayerStatus
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import org.lwjgl.glfw.GLFW
import java.util.UUID

object ClientStatusRuntime {
    @JvmField val KEY = KeyMapping("key.roleplay_status.open", GLFW.GLFW_KEY_U, "key.categories.roleplay_status")
    private val players = mutableMapOf<UUID, PlayerStatus>()
    private var definitions = StatusCatalog.defaults().toList()
    var ready = false
        private set
    var sendChange: (StatusPayloads.Change) -> Unit = {}

    fun initialize() {
        RoleplayStatusConfig.setClientListener { incoming ->
            Minecraft.getInstance().execute { applyDefinitions(incoming) }
        }
        applyDefinitions(RoleplayStatus.CONFIG.definitions())
    }
    fun applyDefinitions(incoming: List<StatusCatalog.Definition>) {
        definitions = StatusCatalog.defaults().map { fallback -> incoming.firstOrNull { it.id == fallback.id } ?: fallback }
    }
    fun definitions(): List<StatusCatalog.Definition> = definitions.toList()
    fun self(): PlayerStatus = Minecraft.getInstance().player?.uuid?.let(players::get) ?: PlayerStatus()
    fun request(status: String, noSleep: Boolean) {
        if (ready && PlayerStatus.validated(status, noSleep) != null) sendChange(StatusPayloads.Change(status, noSleep))
    }
    fun acceptReady(payload: StatusPayloads.Ready) {
        players.clear()
        ready = payload.protocol == StatusPayloads.PROTOCOL
    }
    fun acceptSync(payload: StatusPayloads.Sync) {
        if (!ready) return
        if (payload.removed) players.remove(payload.player)
        else players[payload.player] = PlayerStatus(StatusCatalog.sanitize(payload.status), payload.noSleep)
    }
    fun reset() {
        ready = false
        players.clear()
        applyDefinitions(StatusCatalog.defaults())
    }
    fun tick() {
        val client = Minecraft.getInstance()
        while (KEY.consumeClick()) {
            if (client.player != null && client.screen == null) client.setScreen(RoleplayStatusScreen())
        }
    }
    @JvmStatic fun decorate(info: PlayerInfo, original: Component): Component {
        if (!ready) return original
        val state = players[info.profile.id] ?: return original
        val definition = definitions.firstOrNull { it.id == state.status } ?: return original
        val badge = Component.literal("\u25a0 ").withStyle {
            it.withColor(definition.rgb).withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(definition.label)))
        }
        return badge.append(original)
    }
}
