package net.bms.roleplay_status.client;

import net.bms.roleplay_status.RoleplayStatus;
import net.bms.roleplay_status.config.RoleplayStatusConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class RoleplayStatusClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        RoleplayStatusConfig.setClientListener(definitions -> net.minecraft.client.Minecraft.getInstance().execute(() -> ClientStatusRuntime.applyDefinitions(definitions)));
        ClientStatusRuntime.applyDefinitions(RoleplayStatus.CONFIG.definitions());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientStatusRuntime.resetToDefaults());
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientStatusRuntime.rebuildIconsIfNeeded());
    }
}
