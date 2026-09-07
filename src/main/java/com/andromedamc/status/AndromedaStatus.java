package com.andromedamc.status;

import com.andromedamc.status.config.AndromedaStatusConfig;
import com.andromedamc.status.mixin.PlayerStateManagerAccessor;
import com.andromedamc.status.network.StatusDefinitionsPayload;
import de.maxhenkel.status.Status;
import de.maxhenkel.status.events.PlayerEvents;
import de.maxhenkel.status.net.PlayerStatePacket;
import de.maxhenkel.status.playerstate.PlayerState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class AndromedaStatus implements ModInitializer {

    public static final String MOD_ID = "andromeda_status";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile AndromedaStatusConfig serverConfig = AndromedaStatusConfig.defaults();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(StatusDefinitionsPayload.TYPE, StatusDefinitionsPayload.CODEC);

        PlayerEvents.PLAYER_LOGGED_IN.register(player -> {
            if (Status.STATE_MANAGER == null) {
                return;
            }
            var states = ((PlayerStateManagerAccessor) Status.STATE_MANAGER).andromeda$getStates();
            PlayerState defaultState = states.computeIfAbsent(player.getUUID(), PlayerState::new);
            PlayerStatePacket packet = new PlayerStatePacket(defaultState);
            player.server.getPlayerList().getPlayers().forEach(target -> ServerPlayNetworking.send(target, packet));
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Path path = server.getServerDirectory()
                    .resolve("config")
                    .resolve(MOD_ID)
                    .resolve("statuses.json");
            serverConfig = AndromedaStatusConfig.load(path);
            LOGGER.info("Loaded {} Andromeda status definitions", serverConfig.definitions().size());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            StatusDefinitionsPayload payload = new StatusDefinitionsPayload(serverConfig.definitions());
            if (ServerPlayNetworking.canSend(handler.player, StatusDefinitionsPayload.TYPE)) {
                ServerPlayNetworking.send(handler.player, payload);
            }
        });
    }
}
