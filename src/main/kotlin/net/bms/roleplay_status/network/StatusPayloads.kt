package net.bms.roleplay_status.network

import net.bms.roleplay_status.RoleplayStatus
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

object StatusPayloads {
    const val PROTOCOL = 2
    private fun <T : CustomPacketPayload> type(path: String) =
        CustomPacketPayload.Type<T>(ResourceLocation.fromNamespaceAndPath(RoleplayStatus.MOD_ID, path))
    val HELLO = type<Hello>("hello")
    val CHANGE = type<Change>("change")
    val READY = type<Ready>("ready")
    val SYNC = type<Sync>("sync")

    val HELLO_CODEC = codec({ Hello(it.readVarInt()) }, { buf, value -> buf.writeVarInt(value.protocol) })
    val CHANGE_CODEC = codec({ Change(it.readUtf(64), it.readBoolean()) }, { buf, value ->
        buf.writeUtf(value.status, 64); buf.writeBoolean(value.noSleep)
    })
    val READY_CODEC = codec({ Ready(it.readVarInt()) }, { buf, value -> buf.writeVarInt(value.protocol) })
    val SYNC_CODEC = codec({ Sync(it.readUUID(), it.readUtf(64), it.readBoolean(), it.readBoolean()) }, { buf, value ->
        buf.writeUUID(value.player); buf.writeUtf(value.status, 64)
        buf.writeBoolean(value.noSleep); buf.writeBoolean(value.removed)
    })

    private fun <T : Any> codec(read: (RegistryFriendlyByteBuf) -> T, write: (RegistryFriendlyByteBuf, T) -> Unit) =
        object : StreamCodec<RegistryFriendlyByteBuf, T> {
            override fun decode(buf: RegistryFriendlyByteBuf): T = read(buf)
            override fun encode(buf: RegistryFriendlyByteBuf, value: T) = write(buf, value)
        }

    data class Hello(val protocol: Int = PROTOCOL) : CustomPacketPayload { override fun type() = HELLO }
    data class Change(val status: String, val noSleep: Boolean) : CustomPacketPayload { override fun type() = CHANGE }
    data class Ready(val protocol: Int = PROTOCOL) : CustomPacketPayload { override fun type() = READY }
    data class Sync(val player: UUID, val status: String, val noSleep: Boolean, val removed: Boolean = false) : CustomPacketPayload {
        override fun type() = SYNC
    }
}
