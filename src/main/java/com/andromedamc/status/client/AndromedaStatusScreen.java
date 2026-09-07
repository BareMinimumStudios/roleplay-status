package com.andromedamc.status.client;

import com.andromedamc.status.StatusCatalog;
import de.maxhenkel.status.StatusClient;
import de.maxhenkel.status.playerstate.Availability;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AndromedaStatusScreen extends Screen {

    private static final int PANEL_WIDTH = 310;
    private static final int PANEL_HEIGHT = 172;
    private static final int BUTTON_WIDTH = 132;
    private static final int BUTTON_HEIGHT = 20;

    private int left;
    private int top;
    private Button noSleepButton;

    public AndromedaStatusScreen() {
        super(Component.translatable("gui.andromeda_status.title"));
    }

    @Override
    protected void init() {
        super.init();
        left = (width - PANEL_WIDTH) / 2;
        top = (height - PANEL_HEIGHT) / 2;

        // The addon replaces Status' availability system entirely.
        if (StatusClient.STATE_MANAGER.getAvailabilityIcon() != Availability.NONE) {
            StatusClient.STATE_MANAGER.setAvailability(Availability.NONE);
        }

        List<StatusCatalog.Definition> definitions = ClientStatusRuntime.definitions();
        for (int i = 0; i < definitions.size(); i++) {
            StatusCatalog.Definition definition = definitions.get(i);
            int column = i % 2;
            int row = i / 2;
            int x = left + 24 + column * 148;
            int y = top + 30 + row * 25;

            Button button = Button.builder(Component.literal(definition.label()), ignored -> {
                        StatusClient.STATE_MANAGER.setState(definition.id());
                        rebuildWidgets();
                    })
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            button.active = !StatusClient.STATE_MANAGER.getState().equals(definition.id());
            addRenderableWidget(button);
        }

        noSleepButton = Button.builder(noSleepText(), ignored -> {
                    StatusClient.STATE_MANAGER.setNoSleep(!StatusClient.STATE_MANAGER.getNoSleep());
                    noSleepButton.setMessage(noSleepText());
                })
                .bounds(left + 89, top + 135, 132, BUTTON_HEIGHT)
                .build();
        addRenderableWidget(noSleepButton);
    }

    private Component noSleepText() {
        return Component.literal("No Sleep: " + (StatusClient.STATE_MANAGER.getNoSleep() ? "ON" : "OFF"));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Screen#render already invokes the background pass on 1.21.1. Keep all
        // background/panel drawing here so the blur is completed before widgets,
        // status swatches, and text are rendered.
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xD9101010);
        guiGraphics.fill(left + 1, top + 1, left + PANEL_WIDTH - 1, top + 2, 0xFF6B6B6B);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Let vanilla render the background and widgets first. Custom foreground
        // elements are intentionally last so they cannot be caught by the blur pass.
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        List<StatusCatalog.Definition> definitions = ClientStatusRuntime.definitions();
        for (int i = 0; i < definitions.size(); i++) {
            StatusCatalog.Definition definition = definitions.get(i);
            int column = i % 2;
            int row = i / 2;
            int swatchX = left + 11 + column * 148;
            int swatchY = top + 36 + row * 25;
            int color = 0xFF000000 | definition.rgb();
            guiGraphics.fill(swatchX, swatchY, swatchX + 8, swatchY + 8, color);
        }

        int titleWidth = font.width(title);
        guiGraphics.drawString(font, title, left + (PANEL_WIDTH - titleWidth) / 2, top + 10, 0xFFFFFF, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
