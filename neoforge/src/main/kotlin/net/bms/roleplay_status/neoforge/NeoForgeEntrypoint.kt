package net.bms.roleplay_status.neoforge

import net.bms.roleplay_status.RoleplayStatus
import net.bms.roleplay_status.dice.DiceCommands
import net.bms.roleplay_status.dice.DicePayload
import net.bms.roleplay_status.network.StatusPayloads
import net.bms.roleplay_status.state.ServerStatuses
import net.minecraft.server.level.ServerPlayer
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

@Mod(RoleplayStatus.MOD_ID)
class NeoForgeEntrypoint(bus: IEventBus) {
    init {
        RoleplayStatus.initialize(FMLPaths.CONFIGDIR.get())
        NeoForge.EVENT_BUS.addListener<RegisterCommandsEvent> { event -> DiceCommands.register(event.dispatcher) }
        DiceCommands.sendAnimation = { player, packet ->
            if (player.connection.hasChannel(DicePayload.TYPE)) PacketDistributor.sendToPlayer(player, packet)
        }
        ServerStatuses.send = { player, packet -> PacketDistributor.sendToPlayer(player, packet) }
        bus.addListener<RegisterPayloadHandlersEvent> { event ->
            event.registrar("1").optional().playToClient(DicePayload.TYPE, DicePayload.CODEC) { packet, context ->
                context.enqueueWork { NeoForgeClient.dice(packet) }
            }
            val registrar = event.registrar(StatusPayloads.PROTOCOL.toString())
            registrar.playToServer(StatusPayloads.HELLO, StatusPayloads.HELLO_CODEC) { packet, context ->
                context.enqueueWork { ServerStatuses.hello(context.player() as ServerPlayer, packet.protocol) }
            }
            registrar.playToServer(StatusPayloads.CHANGE, StatusPayloads.CHANGE_CODEC) { packet, context ->
                context.enqueueWork { ServerStatuses.change(context.player() as ServerPlayer, packet.status, packet.noSleep) }
            }
            registrar.playToClient(StatusPayloads.READY, StatusPayloads.READY_CODEC) { packet, context ->
                context.enqueueWork { NeoForgeClient.ready(packet) }
            }
            registrar.playToClient(StatusPayloads.SYNC, StatusPayloads.SYNC_CODEC) { packet, context ->
                context.enqueueWork { NeoForgeClient.sync(packet) }
            }
        }
        NeoForge.EVENT_BUS.addListener<PlayerEvent.PlayerLoggedInEvent> { event ->
            (event.entity as? ServerPlayer)?.let(ServerStatuses::join)
        }
        NeoForge.EVENT_BUS.addListener<PlayerEvent.PlayerLoggedOutEvent> { event ->
            (event.entity as? ServerPlayer)?.let { ServerStatuses.leave(it); DiceCommands.leave(it) }
        }
        NeoForge.EVENT_BUS.addListener<ServerStartedEvent> { ServerStatuses.clear(); DiceCommands.clear() }
        NeoForge.EVENT_BUS.addListener<ServerStoppedEvent> { ServerStatuses.clear(); DiceCommands.clear() }
        if (FMLEnvironment.dist == Dist.CLIENT) NeoForgeClient.register(bus)
    }
}
