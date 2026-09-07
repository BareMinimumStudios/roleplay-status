package com.andromedamc.status.client;

import com.andromedamc.status.StatusCatalog;
import com.andromedamc.status.network.StatusDefinitionsPayload;
import de.maxhenkel.status.gui.StatusScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class AndromedaStatusClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Only update the logical definitions here. Dynamic textures are created
        // later from END_CLIENT_TICK, after Minecraft has a valid render context.
        ClientStatusRuntime.applyDefinitions(StatusCatalog.defaults());

        ClientPlayNetworking.registerGlobalReceiver(StatusDefinitionsPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientStatusRuntime.applyDefinitions(payload.definitions()))
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientStatusRuntime.resetToDefaults());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientStatusRuntime.rebuildIconsIfNeeded();

            if (client.screen != null
                    && client.screen.getClass() == StatusScreen.class) {
                client.setScreen(new AndromedaStatusScreen());
            }
        });
    }
}
