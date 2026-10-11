package net.bms.roleplay_status.client

import com.mojang.blaze3d.platform.NativeImage
import net.bms.roleplay_status.StatusCatalog
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation

object ClientStatusRuntime {
    private val definitions = StatusCatalog.defaults().associateByTo(linkedMapOf()) { it.id }
    private val icons = linkedMapOf<String, ResourceLocation>()
    private var iconsDirty = true

    fun applyDefinitions(incoming: List<StatusCatalog.Definition>) {
        val merged = StatusCatalog.defaults().associateTo(linkedMapOf()) { fallback ->
            fallback.id to (incoming.firstOrNull { it.id == fallback.id } ?: fallback)
        }
        if (definitions == merged) return
        definitions.clear()
        definitions.putAll(merged)
        iconsDirty = true
    }

    fun resetToDefaults() {
        releaseIcons()
        applyDefinitions(StatusCatalog.defaults())
        iconsDirty = true
    }

    fun definitions(): List<StatusCatalog.Definition> = java.util.List.copyOf(definitions.values)
    @JvmStatic fun icon(id: String): ResourceLocation? = icons[id]

    fun rebuildIconsIfNeeded() {
        if (!iconsDirty) return
        val manager = Minecraft.getInstance()?.textureManager ?: return
        releaseIcons()
        definitions.values.forEachIndexed { index, definition ->
            val texture = DynamicTexture(makeIcon(definition))
            val path = "roleplay_status_status_${index}_${definition.id.replace(':', '_')}"
            icons[definition.id] = manager.register(path, texture)
        }
        iconsDirty = false
    }

    private fun releaseIcons() {
        val manager = Minecraft.getInstance()?.textureManager
        if (manager != null) icons.values.forEach(manager::release)
        icons.clear()
    }

    private fun makeIcon(definition: StatusCatalog.Definition): NativeImage {
        val image = NativeImage(8, 8, true)
        val rgb = definition.rgb
        val abgr = (0xFF shl 24) or ((rgb and 255) shl 16) or
            (rgb and 0xFF00) or ((rgb ushr 16) and 255)
        for (y in 0..7) for (x in 0..7) image.setPixelRGBA(x, y, 0)
        val activity = definition.id == StatusCatalog.RECORDING || definition.id == StatusCatalog.STREAMING
        val ys = if (activity) 0..7 else 1..6
        val xs = if (activity) 4..7 else 1..6
        for (y in ys) for (x in xs) image.setPixelRGBA(x, y, abgr)
        return image
    }
}
