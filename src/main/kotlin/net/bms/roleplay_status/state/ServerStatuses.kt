package net.bms.roleplay_status.state

import net.bms.roleplay_status.network.StatusPayloads
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

object ServerStatuses {
    private val subscribed = mutableSetOf<UUID>()
    private val lastHello = mutableMapOf<UUID, Int>()
    private val lastChange = mutableMapOf<UUID, Int>()
    lateinit var send: (ServerPlayer, CustomPacketPayload) -> Unit

    private fun data(server: MinecraftServer): StatusData =
        server.overworld().dataStorage.computeIfAbsent(StatusData.FACTORY, "roleplay_status")

    fun clear() { subscribed.clear(); lastChange.clear(); lastHello.clear() }
    fun hello(player: ServerPlayer, protocol: Int) {
        if (player.server.playerList.getPlayer(player.uuid) !== player) return
        if (protocol != StatusPayloads.PROTOCOL) {
            player.connection.disconnect(Component.translatable("disconnect.roleplay_status.protocol"))
            return
        }
        val server = player.server
        val previous = lastHello[player.uuid]
        if (previous != null && server.tickCount - previous < 20) return
        lastHello[player.uuid] = server.tickCount
        val newlySubscribed = subscribed.add(player.uuid)
        send(player, StatusPayloads.Ready())
        for (other in server.playerList.players) send(player, packet(other, data(server).get(other.uuid)))
        if (newlySubscribed) broadcast(server, packet(player, data(server).get(player.uuid)))
    }

    fun change(player: ServerPlayer, status: String, noSleep: Boolean) {
        if (player.server.playerList.getPlayer(player.uuid) !== player) return
        if (player.uuid !in subscribed) return
        val value = PlayerStatus.validated(status, noSleep) ?: return
        val server = player.server
        val store = data(server)
        if (store.get(player.uuid) == value) return
        // Limit client-driven persistence and broadcasts to two updates per second.
        val tick = server.tickCount
        val previous = lastChange[player.uuid]
        if (previous != null && tick - previous < 10) {
            send(player, packet(player, store.get(player.uuid)))
            return
        }
        lastChange[player.uuid] = tick
        store.set(player.uuid, value)
        player.serverLevel().updateSleepingPlayerList()
        broadcast(server, packet(player, value))
    }

    fun join(player: ServerPlayer) {
        broadcast(player.server, packet(player, data(player.server).get(player.uuid)))
    }

    fun leave(player: ServerPlayer) {
        subscribed.remove(player.uuid)
        lastChange.remove(player.uuid)
        lastHello.remove(player.uuid)
        broadcast(player.server, StatusPayloads.Sync(player.uuid, "", false, true))
    }

    @JvmStatic fun excludedFromSleep(player: ServerPlayer): Boolean = data(player.server).get(player.uuid).noSleep
    private fun packet(player: ServerPlayer, state: PlayerStatus) = StatusPayloads.Sync(player.uuid, state.status, state.noSleep, false, player.gameProfile.name.take(16))
    private fun broadcast(server: MinecraftServer, payload: CustomPacketPayload) {
        server.playerList.players.filter { it.uuid in subscribed }.forEach { send(it, payload) }
    }
}
