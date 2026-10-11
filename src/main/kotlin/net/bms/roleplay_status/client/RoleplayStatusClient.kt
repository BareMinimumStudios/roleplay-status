package net.bms.roleplay_status.client

import net.bms.roleplay_status.RoleplayStatus
import net.bms.roleplay_status.config.RoleplayStatusConfig
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft

class RoleplayStatusClient : ClientModInitializer {
    override fun onInitializeClient() {
        RoleplayStatusConfig.setClientListener { definitions ->
            Minecraft.getInstance().execute { ClientStatusRuntime.applyDefinitions(definitions) }
        }
        ClientStatusRuntime.applyDefinitions(RoleplayStatus.CONFIG.definitions())
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> ClientStatusRuntime.resetToDefaults() }
        ClientTickEvents.END_CLIENT_TICK.register { ClientStatusRuntime.rebuildIconsIfNeeded() }
    }
}
