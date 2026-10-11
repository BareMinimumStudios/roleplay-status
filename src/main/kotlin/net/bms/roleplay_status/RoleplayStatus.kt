package net.bms.roleplay_status

import de.maxhenkel.status.Status
import de.maxhenkel.status.events.PlayerEvents
import de.maxhenkel.status.net.PlayerStatePacket
import de.maxhenkel.status.playerstate.PlayerState
import net.bms.roleplay_status.config.RoleplayStatusConfig
import net.bms.roleplay_status.mixin.PlayerStateManagerAccessor
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.slf4j.LoggerFactory

class RoleplayStatus : ModInitializer {
    override fun onInitialize() {
        CONFIG = RoleplayStatusConfig.register()
        PlayerEvents.PLAYER_LOGGED_IN.register { player ->
            val manager = Status.STATE_MANAGER ?: return@register
            val states = (manager as PlayerStateManagerAccessor).`roleplay$getStates`()
            val packet = PlayerStatePacket(states.computeIfAbsent(player.uuid, ::PlayerState))
            player.server.playerList.players.forEach { target ->
                if (ServerPlayNetworking.canSend(target, packet.type())) {
                    ServerPlayNetworking.send(target, packet)
                }
            }
        }
    }

    companion object {
        const val MOD_ID = "roleplay_status"
        @JvmField val LOGGER = LoggerFactory.getLogger(MOD_ID)
        lateinit var CONFIG: RoleplayStatusConfig
            private set
    }
}
