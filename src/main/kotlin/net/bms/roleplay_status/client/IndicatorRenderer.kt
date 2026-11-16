package net.bms.roleplay_status.client

import net.minecraft.client.gui.GuiGraphics

object IndicatorRenderer {
    /** A six-pixel symbol centered inside the eight-pixel player-head/swatch frame. */
    @JvmStatic fun draw(graphics: GuiGraphics, x: Int, y: Int, color: Int, shape: Int) {
        val left = x + 1
        val top = y + 1
        when (shape) {
            IndicatorShapes.SOLID -> graphics.fill(left, top, left + 6, top + 6, color)
            IndicatorShapes.RIGHT -> graphics.fill(left + 3, top, left + 6, top + 6, color)
            IndicatorShapes.LEFT -> graphics.fill(left, top, left + 3, top + 6, color)
            else -> for (py in 0 until IndicatorShapes.SIZE) for (px in 0 until IndicatorShapes.SIZE) {
                if (IndicatorShapes.contains(shape, px, py)) graphics.fill(left + px, top + py, left + px + 1, top + py + 1, color)
            }
        }
    }
}
