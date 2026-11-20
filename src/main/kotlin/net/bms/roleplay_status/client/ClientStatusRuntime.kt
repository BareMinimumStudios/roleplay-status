package net.bms.roleplay_status.client

import net.bms.roleplay_status.mixin.ChatComponentAccessor
import net.bms.roleplay_status.RoleplayStatus
import net.bms.roleplay_status.StatusCatalog
import net.bms.roleplay_status.config.RoleplayStatusConfig
import net.bms.roleplay_status.config.AccessibilityConfig
import net.bms.roleplay_status.network.StatusPayloads
import net.bms.roleplay_status.state.PlayerStatus
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.network.chat.Component
import net.bms.roleplay_status.state.StatusRoster
import org.lwjgl.glfw.GLFW

object ClientStatusRuntime {
    @JvmField val KEY = KeyMapping("key.roleplay_status.open", GLFW.GLFW_KEY_U, "key.categories.roleplay_status")
    private lateinit var accessibility: AccessibilityConfig
    fun dicePanelX(): Int = accessibility.dicePanelX.get()
    fun dicePanelY(): Int = accessibility.dicePanelY.get()
    fun dicePanelScale(): Int = accessibility.dicePanelScale.get()
    fun saveDicePanel(x: Int, y: Int, scale: Int) {
        accessibility.dicePanelX.validateAndSet(x.coerceIn(0,100))
        accessibility.dicePanelY.validateAndSet(y.coerceIn(0,100))
        accessibility.dicePanelScale.validateAndSet(scale.coerceIn(50,150))
        accessibility.save()
    }
    fun diceAnimations(): Boolean = ::accessibility.isInitialized && accessibility.diceAnimations.get()
    fun colorblindMode(): Boolean = accessibility.colorblindMode.get()
    fun showStatusLabels(): Boolean = accessibility.showStatusLabels.get()
    fun toggleColorblindMode() {
        accessibility.colorblindMode.validateAndSet(!colorblindMode())
        accessibility.save()
    }
    private val players = StatusRoster()
    private val joinReminder = JoinReminder()
    private var missingServerLogged = false
    private var helloTicks = 0
    var sendHello: () -> Unit = {}
    private var definitions = StatusCatalog.defaults().toList()
    var ready = false
        private set
    var sendChange: (StatusPayloads.Change) -> Unit = {}

    fun initialize() {
        accessibility = AccessibilityConfig.register()
        RoleplayStatusConfig.setClientListener { incoming ->
            Minecraft.getInstance().execute { applyDefinitions(incoming) }
        }
        applyDefinitions(RoleplayStatus.CONFIG.definitions())
    }
    fun applyDefinitions(incoming: List<StatusCatalog.Definition>) {
        definitions = StatusCatalog.defaults().map { fallback -> incoming.firstOrNull { it.id == fallback.id } ?: fallback }
    }
    fun definitions(): List<StatusCatalog.Definition> = definitions.toList()
    fun self(): PlayerStatus = Minecraft.getInstance().player?.let { players.find(it.uuid, it.gameProfile.name) } ?: PlayerStatus()
    fun request(status: String, noSleep: Boolean) {
        if (ready && PlayerStatus.validated(status, noSleep) != null) sendChange(StatusPayloads.Change(status, noSleep))
    }
    fun acceptReady(payload: StatusPayloads.Ready) {
        players.clear()
        ready = payload.protocol == StatusPayloads.PROTOCOL
        RoleplayStatus.LOGGER.info("Roleplay Status handshake acknowledged: protocol {}, ready={}", payload.protocol, ready)
    }
    fun acceptSync(payload: StatusPayloads.Sync) {
        if (!ready) return
        if (payload.removed) players.remove(payload.player)
        else players.set(payload.player, payload.profileName, PlayerStatus(StatusCatalog.sanitize(payload.status), payload.noSleep))
    }
    fun reset() {
        DiceOverlay.clear()
        ready = false
        helloTicks = 0
        joinReminder.reset()
        missingServerLogged = false
        players.clear()
        applyDefinitions(StatusCatalog.defaults())
    }
    fun tick() {
        val client = Minecraft.getInstance()
        if (client.player != null && client.connection != null) {
            if (joinReminder.tick()) {
                val chat = client.gui.chat
                val history = chat as ChatComponentAccessor
                JoinReminder.trimRecentBlank(history.`roleplay$messages`(), history.`roleplay$lines`(), client.gui.guiTicks)
                chat.addMessage(JoinReminder.message(KEY.translatedKeyMessage))
            }
            if (!ready && helloTicks++ % 20 == 0) sendHello()
            if (!ready && helloTicks >= 100 && !missingServerLogged) {
                missingServerLogged = true
                RoleplayStatus.LOGGER.warn("Roleplay Status has not received a server handshake after 5 seconds; indicators await server synchronization")
            }
        }
        while (KEY.consumeClick()) {
            if (client.player != null && client.screen == null) client.setScreen(RoleplayStatusScreen())
        }
    }
    @JvmStatic fun decorate(info: PlayerInfo, original: Component): Component {
        if (!ready) return original
        val state = players.stateFor(info.profile.id, info.profile.name)
        val definition = definitions.firstOrNull { it.id == state.status } ?: return original
        return TabIndicator.decorate(original, definition.label, definition.rgb,
            state.status == StatusCatalog.RECORDING || state.status == StatusCatalog.STREAMING,
            if (colorblindMode()) IndicatorShapes.forStatus(state.status, true) else -1, showStatusLabels())
    }
}
