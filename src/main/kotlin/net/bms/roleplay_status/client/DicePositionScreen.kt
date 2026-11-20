package net.bms.roleplay_status.client

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

/** Edits a local draft; only Save commits it. Escape and Cancel discard changes. */
class DicePositionScreen(private val parent: Screen) : Screen(Component.translatable("gui.roleplay_status.dice_position")) {
    private var x = ClientStatusRuntime.dicePanelX()
    private var y = ClientStatusRuntime.dicePanelY()
    private var scale = ClientStatusRuntime.dicePanelScale()
    private var draggingPanel = false
    private var offsetX = 0.0
    private var offsetY = 0.0
    private fun bounds() = DicePanelLayout.bounds(width,height,x,y,scale)
    override fun init() {
        draggingPanel=false
        val panelWidth=minOf(300,width-16)
        val left=(width-panelWidth)/2
        addRenderableWidget(object : AbstractSliderButton(left,height-56,panelWidth,20,Component.empty(),(scale-50)/100.0) {
            init { updateMessage() }
            override fun updateMessage() { message=Component.translatable("gui.roleplay_status.dice_size",(50+value*100).roundToInt()) }
            override fun applyValue() { scale=(50+value*100).roundToInt() }
        })
        val gap=4;val bw=(panelWidth-gap*2)/3
        addRenderableWidget(Button.builder(Component.translatable("controls.reset")) { x=0;y=0;scale=100;rebuildWidgets() }.bounds(left,height-30,bw,20).build())
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel")) { onClose() }.bounds(left+bw+gap,height-30,bw,20).build())
        addRenderableWidget(Button.builder(Component.translatable("gui.roleplay_status.save")) {
            ClientStatusRuntime.saveDicePanel(x,y,scale)
            onClose()
        }.bounds(left+2*(bw+gap),height-30,bw,20).build())
    }
    override fun renderBackground(g: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        // Keep the world and other HUD mods visible for placement; omit the normal screen blur.
    }
    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        g.fill(0,height-82,width,height,0xDD11151D.toInt())
        DiceOverlay.preview(g,x,y,scale)
        g.drawCenteredString(font,Component.translatable("gui.roleplay_status.dice_drag"),width/2,height-77,0xFFFFFF)
        super.render(g,mouseX,mouseY,delta)
    }
    override fun mouseClicked(mx: Double, my: Double, button: Int): Boolean {
        if(super.mouseClicked(mx,my,button)) return true
        val b=bounds()
        if(button==0 && b.contains(mx,my)) {
            draggingPanel=true;offsetX=mx-b.x;offsetY=my-b.y
            return true
        }
        return false
    }
    override fun mouseDragged(mx: Double,my: Double,button: Int,dx: Double,dy: Double): Boolean {
        if(draggingPanel && button==0) {
            val b=bounds()
            x=DicePanelLayout.position(mx-offsetX,width,b.width)
            y=DicePanelLayout.position(my-offsetY,height,b.height)
            return true
        }
        return super.mouseDragged(mx,my,button,dx,dy)
    }
    override fun mouseReleased(mx: Double,my: Double,button: Int): Boolean {
        val wasDragging=draggingPanel
        if(button==0) draggingPanel=false
        return super.mouseReleased(mx,my,button) || wasDragging
    }
    override fun onClose() { minecraft?.setScreen(parent) }
    override fun isPauseScreen() = false
}
