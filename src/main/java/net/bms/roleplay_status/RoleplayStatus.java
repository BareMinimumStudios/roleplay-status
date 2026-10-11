package net.bms.roleplay_status;

import net.bms.roleplay_status.config.RoleplayStatusConfig;
import net.bms.roleplay_status.mixin.PlayerStateManagerAccessor;
import de.maxhenkel.status.Status;
import de.maxhenkel.status.events.PlayerEvents;
import de.maxhenkel.status.net.PlayerStatePacket;
import de.maxhenkel.status.playerstate.PlayerState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleplayStatus implements ModInitializer {
    public static final String MOD_ID = "roleplay_status";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static RoleplayStatusConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG = RoleplayStatusConfig.register();
        PlayerEvents.PLAYER_LOGGED_IN.register(player -> {
            if (Status.STATE_MANAGER == null) return;
            var states = ((PlayerStateManagerAccessor) Status.STATE_MANAGER).roleplay$getStates();
            PlayerState state = states.computeIfAbsent(player.getUUID(), PlayerState::new);
            PlayerStatePacket packet = new PlayerStatePacket(state);
            player.server.getPlayerList().getPlayers().forEach(target -> {
                if (ServerPlayNetworking.canSend(target, packet.type())) {
                    ServerPlayNetworking.send(target, packet);
                }
            });
        });
    }
}

