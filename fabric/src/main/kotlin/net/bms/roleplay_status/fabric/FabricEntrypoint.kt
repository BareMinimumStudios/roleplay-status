package net.bms.roleplay_status.fabric

import net.bms.roleplay_status.RoleplayStatus
import net.bms.roleplay_status.network.StatusPayloads
import net.bms.roleplay_status.state.ServerStatuses
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader

class FabricEntrypoint : ModInitializer {
    override fun onInitialize() {
        RoleplayStatus.initialize(FabricLoader.getInstance().configDir)
        PayloadTypeRegistry.playC2S().register(StatusPayloads.HELLO, StatusPayloads.HELLO_CODEC)
        PayloadTypeRegistry.playC2S().register(StatusPayloads.CHANGE, StatusPayloads.CHANGE_CODEC)
        PayloadTypeRegistry.playS2C().register(StatusPayloads.READY, StatusPayloads.READY_CODEC)
        PayloadTypeRegistry.playS2C().register(StatusPayloads.SYNC, StatusPayloads.SYNC_CODEC)
        ServerStatuses.send = { player, packet -> ServerPlayNetworking.send(player, packet) }
        ServerPlayNetworking.registerGlobalReceiver(StatusPayloads.HELLO) { packet, context ->
            context.server().execute { ServerStatuses.hello(context.player(), packet.protocol) }
        }
        ServerPlayNetworking.registerGlobalReceiver(StatusPayloads.CHANGE) { packet, context ->
            context.server().execute { ServerStatuses.change(context.player(), packet.status, packet.noSleep) }
        }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> ServerStatuses.leave(handler.player) }
        ServerLifecycleEvents.SERVER_STARTED.register { ServerStatuses.clear() }
        ServerLifecycleEvents.SERVER_STOPPED.register { ServerStatuses.clear() }
    }
}
