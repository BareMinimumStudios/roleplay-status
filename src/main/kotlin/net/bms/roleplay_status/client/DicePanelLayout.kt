package net.bms.roleplay_status.client

import kotlin.math.roundToInt

/** GUI-coordinate layout, independent of GUI scale and render state. */
object DicePanelLayout {
    const val WIDTH = 222
    const val HEIGHT = 113
    const val MARGIN = 8
    data class Bounds(val x: Int, val y: Int, val width: Int, val height: Int, val scale: Float) {
        fun contains(mx: Double, my: Double) = mx >= x && mx < x+width && my >= y && my < y+height
    }
    @JvmStatic fun bounds(screenWidth: Int, screenHeight: Int, x: Int, y: Int, percent: Int): Bounds {
        val availableWidth = (screenWidth-2*MARGIN).coerceAtLeast(1)
        val availableHeight = (screenHeight-2*MARGIN).coerceAtLeast(1)
        val scale = minOf(percent.coerceIn(50,150)/100f, availableWidth.toFloat()/WIDTH, availableHeight.toFloat()/HEIGHT)
        val width = (WIDTH*scale).roundToInt().coerceAtMost(availableWidth)
        val height = (HEIGHT*scale).roundToInt().coerceAtMost(availableHeight)
        val marginX = minOf(MARGIN,(screenWidth-width).coerceAtLeast(0)/2)
        val marginY = minOf(MARGIN,(screenHeight-height).coerceAtLeast(0)/2)
        val px = marginX + ((screenWidth-width-2*marginX).coerceAtLeast(0)*x.coerceIn(0,100)/100.0).roundToInt()
        val py = marginY + ((screenHeight-height-2*marginY).coerceAtLeast(0)*y.coerceIn(0,100)/100.0).roundToInt()
        return Bounds(px,py,width,height,scale)
    }
    @JvmStatic fun position(pixel: Double, screenSize: Int, panelSize: Int): Int {
        val margin = minOf(MARGIN,(screenSize-panelSize).coerceAtLeast(0)/2)
        val travel = screenSize-panelSize-2*margin
        if(travel <= 0) return 0
        return ((pixel-margin)*100/travel).roundToInt().coerceIn(0,100)
    }
}
