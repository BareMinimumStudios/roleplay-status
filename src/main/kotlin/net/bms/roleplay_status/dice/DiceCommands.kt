package net.bms.roleplay_status.dice

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.bms.roleplay_status.RoleplayStatus
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.server.level.ServerPlayer
import java.security.SecureRandom
import java.util.UUID

object DiceCommands {
    private val random = SecureRandom()
    private val lastRoll = HashMap<UUID, Long>()
    var sendAnimation: (ServerPlayer, DicePayload) -> Unit = { _, _ -> }
    fun clear() = lastRoll.clear()
    fun leave(player: ServerPlayer) { lastRoll.remove(player.uuid) }
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("roll")
            .executes { roll(it.source, "d20", "") }
            .then(Commands.argument("dice", StringArgumentType.word())
                .suggests { _, builder -> listOf("d2","d4","d6","d8","d10","d12","d20","d100","2d6","1d20+3").forEach { builder.suggest(it) }; builder.buildFuture() }
                .executes { roll(it.source, StringArgumentType.getString(it,"dice"), "") }
                .then(Commands.argument("reason", StringArgumentType.greedyString()).executes {
                    roll(it.source,StringArgumentType.getString(it,"dice"),StringArgumentType.getString(it,"reason"))
                })))
    }
    private fun roll(source: CommandSourceStack, notation: String, rawReason: String): Int {
        val player = source.playerOrException
        fun fail(text: String): Int { source.sendFailure(Component.literal(text)); return 0 }
        val config = RoleplayStatus.DICE
        if (!config.enabled.get()) return fail("Dice rolls are disabled on this server.")
        val expression = DiceExpression.parse(notation) ?: return fail("Use /roll [1-20]d[2,4,6,8,10,12,20,100][+/-modifier] [optional reason]. Modifier limit: 1000.")
        val level = player.serverLevel()
        val now = level.gameTime
        val previous = lastRoll[player.uuid]
        if (previous != null && now - previous < config.cooldownTicks.get()) return fail("Please wait before rolling again.")
        val reason = DiceExpression.reason(rawReason)
        val results = expression.roll(random)
        val total = expression.total(results)
        val range = config.range.get()
        lastRoll[player.uuid] = now
        val packet = DicePayload(player.gameProfile.name,expression,results,reason)
        val message = Component.literal("[Roll] ").withStyle(ChatFormatting.GOLD)
            .append((player.displayName ?: player.name).copy().withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" rolled ${expression.notation}: ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(total.toString()).withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" [${results.joinToString(", ")}${if(expression.modifier != 0) "; modifier ${expression.modifier}" else ""}]").withStyle(ChatFormatting.GRAY))
        if (reason.isNotEmpty()) message.append(Component.literal(" - $reason").withStyle(ChatFormatting.GRAY))
        level.players().forEach { recipient ->
            if (DiceExpression.withinRange(true, recipient.distanceToSqr(player), range)) {
                recipient.sendSystemMessage(message)
                sendAnimation(recipient,packet)
            }
        }
        return total
    }
}
