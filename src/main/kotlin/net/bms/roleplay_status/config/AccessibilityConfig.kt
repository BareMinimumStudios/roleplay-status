package net.bms.roleplay_status.config

import me.fzzyhmstrs.fzzy_config.annotations.RootConfig
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava
import me.fzzyhmstrs.fzzy_config.api.FileType
import me.fzzyhmstrs.fzzy_config.api.RegisterType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.util.Translatable
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean
import net.bms.roleplay_status.RoleplayStatus
import net.minecraft.resources.ResourceLocation

@RootConfig
@Translatable.Name("Accessibility")
@Translatable.Desc("Your display preferences. These never change server settings or other players' displays.")
class AccessibilityConfig : Config(ResourceLocation.fromNamespaceAndPath(RoleplayStatus.MOD_ID, "accessibility")) {
    @Translatable.Name("Colorblind mode")
    @Translatable.Desc("Use a different shape for every status and show its label beside player names. Applies immediately on your client only.")
    var colorblindMode = ValidatedBoolean(false)
    override fun fileType() = FileType.JSON

    companion object {
        fun register(): AccessibilityConfig = ConfigApiJava.registerAndLoadConfig(::AccessibilityConfig, RegisterType.CLIENT)
    }
}
