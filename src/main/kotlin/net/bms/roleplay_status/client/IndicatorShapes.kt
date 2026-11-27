package net.bms.roleplay_status.client

import kotlin.math.abs
import net.bms.roleplay_status.StatusCatalog

/** Eight distinct silhouettes; no status requires color recognition in accessibility mode. */
object IndicatorShapes {
    const val SIZE = 6
    const val SOLID = 0
    const val OUTLINE = 1
    const val PLUS = 2
    const val DASH = 3
    const val DIAMOND = 4
    const val TRIANGLE = 5
    const val RIGHT = 6
    const val LEFT = 7

    @JvmStatic fun forStatus(id: String, colorblind: Boolean): Int {
        if (!colorblind) return if (id == StatusCatalog.RECORDING || id == StatusCatalog.STREAMING) RIGHT else SOLID
        return when (id) {
            StatusCatalog.IC -> SOLID
            StatusCatalog.OPEN_RP -> PLUS
            StatusCatalog.BUSY_AFK -> DASH
            StatusCatalog.EVENT -> DIAMOND
            StatusCatalog.NEW -> TRIANGLE
            StatusCatalog.RECORDING -> RIGHT
            StatusCatalog.STREAMING -> LEFT
            else -> OUTLINE
        }
    }

    @JvmStatic fun contains(shape: Int, x: Int, y: Int): Boolean {
        if (x !in 0 until SIZE || y !in 0 until SIZE) return false
        return when (shape) {
            OUTLINE -> x == 0 || x == SIZE - 1 || y == 0 || y == SIZE - 1
            PLUS -> x in 2..3 || y in 2..3
            DASH -> y in 2..3
            DIAMOND -> abs(2 * x - 5) + abs(2 * y - 5) <= 6
            TRIANGLE -> abs(2 * x - 5) <= y + 1
            RIGHT -> x >= 3
            LEFT -> x < 3
            else -> true
        }
    }
}
