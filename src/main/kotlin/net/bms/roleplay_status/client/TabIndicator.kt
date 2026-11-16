package net.bms.roleplay_status.client

import net.bms.roleplay_status.RoleplayStatus
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.resources.ResourceLocation

/** The private font reserves ten pixels; the actual badge is drawn as a rectangle. */
object TabIndicator {
    private val FONT = ResourceLocation.fromNamespaceAndPath(RoleplayStatus.MOD_ID, "indicator")
    private const val SPACER = "\ue000"

    private const val ACTIVITY_SPACER = "\ue001"

    @JvmStatic @JvmOverloads fun decorate(original: Component, label: String, rgb: Int, activity: Boolean = false, shapeOverride: Int = -1, showLabel: Boolean = false): Component {
        if (color(original) != null) return original
        val marker = if (shapeOverride in 0..7) (0xE010 + shapeOverride).toChar().toString()
            else if (activity) ACTIVITY_SPACER else SPACER
        val spacer = Component.literal(marker).withStyle {
            it.withFont(FONT).withColor(rgb and 0xFFFFFF)
                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(label)))
        }
        // A neutral root prevents our font/color from leaking into TAB's formatted name.
        val result = Component.empty().append(spacer).append(original)
        if (showLabel) result.append(Component.literal(" [$label]").withStyle { it.withColor(0xFFFFFF).withFont(net.minecraft.network.chat.Style.DEFAULT_FONT) })
        return result
    }

    @JvmStatic fun shape(text: Component): Int {
        val marker = text.siblings.firstOrNull()?.string ?: return IndicatorShapes.SOLID
        if (marker == ACTIVITY_SPACER) return IndicatorShapes.RIGHT
        return if (marker.length == 1 && marker[0].code in 0xE010..0xE017) marker[0].code - 0xE010 else IndicatorShapes.SOLID
    }

    @JvmStatic fun isActivity(text: Component): Boolean =
        color(text) != null && shape(text) in IndicatorShapes.RIGHT..IndicatorShapes.LEFT

    @JvmStatic fun color(text: Component): Int? {
        val spacer = text.siblings.firstOrNull() ?: return null
        val marker = spacer.string
        val valid = marker == SPACER || marker == ACTIVITY_SPACER || (marker.length == 1 && marker[0].code in 0xE010..0xE017)
        if (spacer.style.font != FONT || !valid) return null
        return spacer.style.color?.value
    }
}
