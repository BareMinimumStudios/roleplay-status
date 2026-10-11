package net.bms.roleplay_status.client

import de.maxhenkel.status.StatusClient
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava
import net.bms.roleplay_status.RoleplayStatus
import net.bms.roleplay_status.StatusCatalog
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class RoleplayStatusScreen : Screen(Component.translatable("gui.roleplay_status.title")) {
    private var left = 0
    private var top = 0
    private var panelWidth = 0
    private var panelHeight = 0
    private var columns = 1
    private var page = 0
    private var visible = emptyList<StatusCatalog.Definition>()
    private var snapshot = emptyList<StatusCatalog.Definition>()
    private var selected: String? = null
    private var noSleep = false

    override fun init() {
        panelWidth = minOf(340, width - 12)
        columns = if (panelWidth >= 290) 2 else 1
        val rows = ((height - 128) / 24).coerceIn(1, 4)
        val perPage = rows * columns
        snapshot = ClientStatusRuntime.definitions()
        val pages = maxOf(1, (snapshot.size + perPage - 1) / perPage)
        page = page.coerceIn(0, pages - 1)
        visible = snapshot.subList(page * perPage, minOf(snapshot.size, (page + 1) * perPage))
        panelHeight = 112 + rows * 24
        left = (width - panelWidth) / 2
        top = (height - panelHeight) / 2
        val manager = StatusClient.STATE_MANAGER
        selected = manager?.state?.let(StatusCatalog::sanitize)
        noSleep = manager?.noSleep ?: false
        val cellWidth = (panelWidth - 24) / columns
        visible.forEachIndexed { i, definition ->
            val current = definition.id == selected
            val buttonWidth = cellWidth - 18
            val full = (if (current) "✓ " else "") + definition.label
            val text = if (font.width(full) <= buttonWidth - 12) full else
                font.plainSubstrByWidth(full, maxOf(0, buttonWidth - 24)) + "…"
            val button = Button.builder(Component.literal(text)) {
                StatusClient.STATE_MANAGER?.let { stateManager ->
                    stateManager.setState(definition.id)
                    rebuildWidgets()
                }
            }.bounds(left + 26 + (i % columns) * cellWidth, top + 43 + (i / columns) * 24, buttonWidth, 20)
                .tooltip(Tooltip.create(Component.literal(definition.label))).build()
            button.active = manager != null && !current
            addRenderableWidget(button)
        }
        val footer = top + 47 + rows * 24
        if (pages > 1) {
            val previous = Button.builder(Component.literal("‹")) { page--; rebuildWidgets() }
                .bounds(left + 12, footer, 24, 20).build()
            previous.active = page > 0
            addRenderableWidget(previous)
            val next = Button.builder(Component.literal("›")) { page++; rebuildWidgets() }
                .bounds(left + panelWidth - 36, footer, 24, 20).build()
            next.active = page < pages - 1
            addRenderableWidget(next)
        }
        val sleep = Button.builder(Component.translatable("gui.roleplay_status.no_sleep",
            Component.translatable(if (noSleep) "options.on" else "options.off"))) {
            StatusClient.STATE_MANAGER?.let { stateManager ->
                stateManager.setNoSleep(!stateManager.noSleep)
                rebuildWidgets()
            }
        }.bounds(left + (panelWidth - 150) / 2, footer, 150, 20)
            .tooltip(Tooltip.create(Component.translatable("gui.roleplay_status.no_sleep.help"))).build()
        sleep.active = manager != null
        addRenderableWidget(sleep)
        val half = (panelWidth - 30) / 2
        addRenderableWidget(Button.builder(Component.translatable("gui.roleplay_status.configure")) {
            ConfigApiJava.openScreen(RoleplayStatus.MOD_ID)
        }.bounds(left + 12, footer + 24, half, 20)
            .tooltip(Tooltip.create(Component.translatable("gui.roleplay_status.help"))).build())
        addRenderableWidget(Button.builder(Component.translatable("gui.done")) { onClose() }
            .bounds(left + 18 + half, footer + 24, half, 20).build())
    }

    override fun tick() {
        super.tick()
        val manager = StatusClient.STATE_MANAGER
        val state = manager?.state?.let(StatusCatalog::sanitize)
        val sleep = manager?.noSleep ?: false
        if (snapshot != ClientStatusRuntime.definitions() || selected != state || sleep != noSleep) rebuildWidgets()
    }

    override fun renderBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(graphics, mouseX, mouseY, delta)
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xF21A1A1A.toInt())
        graphics.fill(left, top, left + panelWidth, top + 2, 0xFFF2F2F2.toInt())
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(graphics, mouseX, mouseY, delta)
        graphics.drawCenteredString(font, title, width / 2, top + 12, 0xFFF2F2F2.toInt())
        graphics.drawCenteredString(font, Component.translatable("gui.roleplay_status.subtitle"),
            width / 2, top + 27, 0xFFAAAAAA.toInt())
        val cellWidth = (panelWidth - 24) / columns
        visible.forEachIndexed { i, definition ->
            val x = left + 12 + (i % columns) * cellWidth
            val y = top + 49 + (i / columns) * 24
            graphics.fill(x, y, x + 8, y + 8, 0xFF000000.toInt() or definition.rgb)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (StatusClient.KEY_STATUS_GUI?.matches(keyCode, scanCode) == true) {
            onClose()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
    override fun isPauseScreen() = false
}
