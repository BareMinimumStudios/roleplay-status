package net.bms.roleplay_status.client

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import net.bms.roleplay_status.dice.DiceExpression
import net.bms.roleplay_status.dice.DicePayload
import net.bms.roleplay_status.dice.DieGeometry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

/** Passive HUD only: results are authoritative, and chat is never delayed by this animation. */
object DiceOverlay {
    private data class Entry(val packet: DicePayload, val start: Long)
    private val pending = ArrayDeque<DicePayload>()
    private var active: Entry? = null
    fun clear() { pending.clear(); active=null }
    fun accept(packet: DicePayload) {
        if (!ClientStatusRuntime.diceAnimations()) return
        if (pending.size < 8) pending.addLast(packet)
    }
    fun render(g: GuiGraphics) {
        val mc = Minecraft.getInstance()
        if (!ClientStatusRuntime.diceAnimations() || mc.player == null) { clear(); return }
        val now=System.nanoTime()
        if (active != null && (now-active!!.start)/1e9 >= 3.5) active=null
        if (active == null && pending.isNotEmpty()) active=Entry(pending.removeFirst(),now)
        val entry=active ?: return
        if(mc.options.hideGui || mc.screen != null) return
        val age=(now-entry.start)/1e9
        drawPanel(g, entry.packet, age, DicePanelLayout.bounds(g.guiWidth(), g.guiHeight(),
            ClientStatusRuntime.dicePanelX(), ClientStatusRuntime.dicePanelY(), ClientStatusRuntime.dicePanelScale()))
    }
    fun preview(g: GuiGraphics, x: Int, y: Int, scale: Int): DicePanelLayout.Bounds {
        val bounds=DicePanelLayout.bounds(g.guiWidth(),g.guiHeight(),x,y,scale)
        drawPanel(g, DicePayload("Preview", DiceExpression(1,20,3),listOf(17),"Stealth check"),2.0,bounds)
        return bounds
    }
    private fun drawPanel(g: GuiGraphics, packet: DicePayload, age: Double, bounds: DicePanelLayout.Bounds) {
        val mc=Minecraft.getInstance()
        g.pose().pushPose()
        try {
            g.pose().translate(bounds.x.toFloat(),bounds.y.toFloat(),0f)
            g.pose().scale(bounds.scale,bounds.scale,1f)
            val spin=if(age < 1.2) (1.0-age/1.2).pow(2)*PI*5 else 0.0
            val expression=packet.expression
            val width=DicePanelLayout.WIDTH; val x=0; val y=0
            g.fill(x,y,x+width,y+113,0xDD11151D.toInt())
            g.fill(x,y,x+2,y+113,0xFF68CDD1.toInt())
            g.drawString(mc.font,mc.font.plainSubstrByWidth(packet.player+" rolled "+expression.notation,width-18),x+9,y+8,0xFFFFFF)
            val dice=if(expression.sides==100) listOf(10,10) else List(min(4,expression.count)) { expression.sides }
            val spacing=min(44,(width-16)/dice.size)
            dice.forEachIndexed { i,sides ->
                val cx=x+width/2+(i-(dice.size-1)/2.0)*spacing; val cy=y+48.0
                drawDie(g,sides,spin+i*if(spin==0.0) 0.0 else .4,cx,cy,18.0)
                if(age>=1.2) {
                    val value=if(expression.sides==100) {
                        val v=packet.results[(age.toInt()/2)%packet.results.size]%100
                        if(i==0) "%02d".format((v/10)*10) else (v%10).toString()
                    } else packet.results[i].toString()
                    g.drawCenteredString(mc.font,value,cx.roundToInt(),cy.roundToInt()-4,0xFFFFFF)
                }
                g.drawCenteredString(mc.font,if(expression.sides==100) (if(i==0) "d%" else "d10") else "d$sides",cx.roundToInt(),y+72,0xA8B5C3)
            }
            val total=if(age<1.2) "Rolling..." else "Total: ${expression.total(packet.results)}" + if(expression.count>4 || (expression.sides==100 && expression.count>1)) " (${expression.count} dice)" else ""
            g.drawCenteredString(mc.font,total,x+width/2,y+86,0x68CDD1)
            if(packet.reason.isNotEmpty()) g.drawCenteredString(mc.font,mc.font.plainSubstrByWidth(packet.reason,width-18),x+width/2,y+100,0xBAC3CE)
        } finally { g.pose().popPose() }
    }
    private fun drawDie(g: GuiGraphics,sides:Int,spin:Double,cx:Double,cy:Double,size:Double) {
        DieGeometry.project(sides,spin).forEach { face ->
            val points=face.points.map { Pair(cx+it.first*size,cy+it.second*size) }
            val color=0xFF000000.toInt() or ((38*face.shade).toInt() shl 16) or ((139*face.shade).toInt() shl 8) or (159*face.shade).toInt()
            for(y in floor(points.minOf { it.second }).toInt()..ceil(points.maxOf { it.second }).toInt()) {
                val scan=y+.5
                // Each projected face is convex, so only the outermost crossings matter.
                var left = Double.POSITIVE_INFINITY
                var right = Double.NEGATIVE_INFINITY
                for (i in points.indices) {
                    val a = points[i]
                    val b = points[(i + 1) % points.size]
                    if ((a.second <= scan && b.second > scan) || (b.second <= scan && a.second > scan)) {
                        val crossing = a.first + (scan - a.second) * (b.first - a.first) / (b.second - a.second)
                        left = min(left, crossing)
                        right = max(right, crossing)
                    }
                }
                if (left < right) g.fill(ceil(left).toInt(), y, ceil(right).toInt(), y + 1, color)
            }
            points.indices.forEach { i ->
                val a=points[i];val b=points[(i+1)%points.size];val steps=max(abs(b.first-a.first),abs(b.second-a.second)).roundToInt().coerceAtLeast(1)
                for(step in 0..steps) { val t=step.toDouble()/steps;val x=(a.first+(b.first-a.first)*t).roundToInt();val y=(a.second+(b.second-a.second)*t).roundToInt();g.fill(x,y,x+1,y+1,0xFF8EDDE1.toInt()) }
            }
        }
    }
}
