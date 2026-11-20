package net.bms.roleplay_status.dice

import net.bms.roleplay_status.RoleplayStatus
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class DicePayload(val player: String, val expression: DiceExpression, val results: List<Int>, val reason: String) : CustomPacketPayload {
    init {
        require(player.length <= 64 && reason.length <= 128)
        require(DiceExpression.parse(expression.notation) == expression)
        require(results.size == expression.count && results.all { it in 1..expression.sides })
    }
    override fun type() = TYPE
    companion object {
        val TYPE = CustomPacketPayload.Type<DicePayload>(ResourceLocation.fromNamespaceAndPath(RoleplayStatus.MOD_ID,"dice_roll"))
        val CODEC = object : StreamCodec<RegistryFriendlyByteBuf, DicePayload> {
            override fun decode(buf: RegistryFriendlyByteBuf): DicePayload {
                val player = buf.readUtf(64)
                val expression = requireNotNull(DiceExpression.parse(buf.readUtf(20)))
                val results = List(expression.count) { buf.readVarInt() }
                return DicePayload(player, expression, results, buf.readUtf(128))
            }
            override fun encode(buf: RegistryFriendlyByteBuf, value: DicePayload) {
                buf.writeUtf(value.player,64); buf.writeUtf(value.expression.notation,20)
                value.results.forEach { buf.writeVarInt(it) }; buf.writeUtf(value.reason,128)
            }
        }
    }
}
