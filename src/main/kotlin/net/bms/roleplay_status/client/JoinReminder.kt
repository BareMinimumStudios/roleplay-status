package net.bms.roleplay_status.client

import net.minecraft.ChatFormatting
import net.minecraft.client.GuiMessage
import net.minecraft.network.chat.Component

/** Once per connection, after the world has had a second to finish joining. */
class JoinReminder {
    private var ticks = 0
    private var shown = false
    fun tick(): Boolean {
        if (shown || ++ticks < 20) return false
        shown = true
        return true
    }
    fun reset() { ticks = 0; shown = false }
    companion object {
        /** Remove one recent blank entry directly before our reminder, never real chat or old separators. */
        @JvmStatic fun trimRecentBlank(messages: MutableList<GuiMessage>, lines: MutableList<GuiMessage.Line>, now: Int) {
            val message = messages.firstOrNull() ?: return
            val line = lines.firstOrNull() ?: return
            if (!message.content.string.isBlank() || now - message.addedTime !in 0..100) return
            if (line.addedTime != message.addedTime || !line.endOfEntry) return
            var blank = true
            line.content.accept { _, _, codePoint ->
                if (!Character.isWhitespace(codePoint)) blank = false
                true
            }
            if (!blank) return
            messages.removeAt(0)
            lines.removeAt(0)
        }

        @JvmStatic fun message(key: Component): Component = Component.translatableWithFallback(
            "message.roleplay_status.join", "Please make sure to set your status with %s!",
            key.copy().withStyle(ChatFormatting.GREEN)
        ).withStyle(ChatFormatting.WHITE)
    }
}
