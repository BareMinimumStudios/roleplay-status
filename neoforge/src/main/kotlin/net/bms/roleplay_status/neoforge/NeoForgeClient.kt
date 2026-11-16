package net.bms.roleplay_status.neoforge

import net.bms.roleplay_status.client.ClientStatusRuntime
import net.bms.roleplay_status.network.StatusPayloads
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.network.PacketDistributor

object NeoForgeClient {
    fun register(bus: IEventBus) {
        bus.addListener<FMLClientSetupEvent> { event ->
            event.enqueueWork { ClientStatusRuntime.initialize() }
        }
        bus.addListener<RegisterKeyMappingsEvent> { it.register(ClientStatusRuntime.KEY) }
        ClientStatusRuntime.sendChange = { PacketDistributor.sendToServer(it) }
        NeoForge.EVENT_BUS.addListener<ClientPlayerNetworkEvent.LoggingIn> {
            PacketDistributor.sendToServer(StatusPayloads.Hello())
        }
        NeoForge.EVENT_BUS.addListener<ClientPlayerNetworkEvent.LoggingOut> { ClientStatusRuntime.reset() }
        NeoForge.EVENT_BUS.addListener<ClientTickEvent.Post> { ClientStatusRuntime.tick() }
    }
    fun ready(packet: StatusPayloads.Ready) = ClientStatusRuntime.acceptReady(packet)
    fun sync(packet: StatusPayloads.Sync) = ClientStatusRuntime.acceptSync(packet)
}
