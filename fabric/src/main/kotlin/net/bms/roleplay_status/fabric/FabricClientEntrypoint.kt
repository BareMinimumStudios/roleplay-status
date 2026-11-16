package net.bms.roleplay_status.fabric

import net.bms.roleplay_status.client.ClientStatusRuntime
import net.bms.roleplay_status.network.StatusPayloads
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

class FabricClientEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        ClientStatusRuntime.initialize()
        KeyBindingHelper.registerKeyBinding(ClientStatusRuntime.KEY)
        ClientStatusRuntime.sendChange = { ClientPlayNetworking.send(it) }
        ClientPlayNetworking.registerGlobalReceiver(StatusPayloads.READY) { packet, context ->
            context.client().execute { ClientStatusRuntime.acceptReady(packet) }
        }
        ClientPlayNetworking.registerGlobalReceiver(StatusPayloads.SYNC) { packet, context ->
            context.client().execute { ClientStatusRuntime.acceptSync(packet) }
        }
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (ClientPlayNetworking.canSend(StatusPayloads.HELLO)) ClientPlayNetworking.send(StatusPayloads.Hello())
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> ClientStatusRuntime.reset() }
        ClientTickEvents.END_CLIENT_TICK.register { ClientStatusRuntime.tick() }
    }
}
