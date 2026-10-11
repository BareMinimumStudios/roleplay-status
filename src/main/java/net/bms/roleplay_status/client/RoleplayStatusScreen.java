package net.bms.roleplay_status.client;

import net.bms.roleplay_status.RoleplayStatus;
import net.bms.roleplay_status.StatusCatalog;
import de.maxhenkel.status.StatusClient;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Objects;

public class RoleplayStatusScreen extends Screen {
    private int left, top, panelWidth, panelHeight, columns, page, perPage;
    private List<StatusCatalog.Definition> visible = List.of();
    private List<StatusCatalog.Definition> snapshot = List.of();
    private String selected;
    private boolean noSleep;

    public RoleplayStatusScreen() { super(Component.translatable("gui.roleplay_status.title")); }

    @Override protected void init() {
        panelWidth = Math.min(340, width - 12);
        columns = panelWidth >= 290 ? 2 : 1;
        int rows = Math.max(1, Math.min(4, (height - 128) / 24));
        perPage = rows * columns;
        snapshot = ClientStatusRuntime.definitions();
        int pages = Math.max(1, (snapshot.size() + perPage - 1) / perPage);
        page = Math.min(page, pages - 1);
        visible = snapshot.subList(page * perPage, Math.min(snapshot.size(), (page + 1) * perPage));
        panelHeight = 112 + rows * 24;
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        selected = StatusClient.STATE_MANAGER == null ? null : StatusCatalog.sanitize(StatusClient.STATE_MANAGER.getState());
        noSleep = StatusClient.STATE_MANAGER != null && StatusClient.STATE_MANAGER.getNoSleep();
        int cellWidth = (panelWidth - 24) / columns;
        for (int i = 0; i < visible.size(); i++) {
            var definition = visible.get(i);
            boolean current = definition.id().equals(selected);
            int buttonWidth = cellWidth - 18;
            String full = (current ? "✓ " : "") + definition.label();
            String text = font.width(full) <= buttonWidth - 12 ? full : font.plainSubstrByWidth(full, Math.max(0, buttonWidth - 24)) + "…";
            Button button = Button.builder(Component.literal(text), ignored -> {
                if (StatusClient.STATE_MANAGER == null) return;
                StatusClient.STATE_MANAGER.setState(definition.id());
                rebuildWidgets();
            }).bounds(left + 26 + (i % columns) * cellWidth, top + 43 + (i / columns) * 24, buttonWidth, 20)
                    .tooltip(Tooltip.create(Component.literal(definition.label()))).build();
            button.active = StatusClient.STATE_MANAGER != null && !current;
            addRenderableWidget(button);
        }
        int footer = top + 47 + rows * 24;
        if (pages > 1) {
            Button previous = Button.builder(Component.literal("‹"), b -> { page--; rebuildWidgets(); }).bounds(left + 12, footer, 24, 20).build();
            previous.active = page > 0;
            addRenderableWidget(previous);
            Button next = Button.builder(Component.literal("›"), b -> { page++; rebuildWidgets(); }).bounds(left + panelWidth - 36, footer, 24, 20).build();
            next.active = page < pages - 1;
            addRenderableWidget(next);
        }
        Button sleep = Button.builder(Component.translatable("gui.roleplay_status.no_sleep", Component.translatable(noSleep ? "options.on" : "options.off")), b -> {
            if (StatusClient.STATE_MANAGER == null) return;
            StatusClient.STATE_MANAGER.setNoSleep(!StatusClient.STATE_MANAGER.getNoSleep());
            rebuildWidgets();
        }).bounds(left + (panelWidth - 150) / 2, footer, 150, 20).tooltip(Tooltip.create(Component.translatable("gui.roleplay_status.no_sleep.help"))).build();
        sleep.active = StatusClient.STATE_MANAGER != null;
        addRenderableWidget(sleep);
        int half = (panelWidth - 30) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.roleplay_status.configure"), b -> ConfigApiJava.INSTANCE.openScreen(RoleplayStatus.MOD_ID))
                .bounds(left + 12, footer + 24, half, 20).tooltip(Tooltip.create(Component.translatable("gui.roleplay_status.help"))).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(left + 18 + half, footer + 24, half, 20).build());
    }

    @Override public void tick() {
        super.tick();
        String state = StatusClient.STATE_MANAGER == null ? null : StatusCatalog.sanitize(StatusClient.STATE_MANAGER.getState());
        boolean sleep = StatusClient.STATE_MANAGER != null && StatusClient.STATE_MANAGER.getNoSleep();
        if (!snapshot.equals(ClientStatusRuntime.definitions()) || !Objects.equals(selected, state) || sleep != noSleep) rebuildWidgets();
    }

    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(graphics, mouseX, mouseY, delta);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xF21A1A1A);
        graphics.fill(left, top, left + panelWidth, top + 2, 0xFFF2F2F2);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, top + 12, 0xFFF2F2F2);
        Component hint = Component.translatable("gui.roleplay_status.subtitle");
        graphics.drawCenteredString(font, hint, width / 2, top + 27, 0xFFAAAAAA);
        int cellWidth = (panelWidth - 24) / columns;
        for (int i = 0; i < visible.size(); i++) {
            int x = left + 12 + (i % columns) * cellWidth;
            int y = top + 49 + (i / columns) * 24;
            graphics.fill(x, y, x + 8, y + 8, 0xFF000000 | visible.get(i).rgb());
        }
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (StatusClient.KEY_STATUS_GUI != null && StatusClient.KEY_STATUS_GUI.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean isPauseScreen() { return false; }
}

