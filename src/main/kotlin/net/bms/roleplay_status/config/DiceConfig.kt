package net.bms.roleplay_status.config

import me.fzzyhmstrs.fzzy_config.annotations.RootConfig
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava
import me.fzzyhmstrs.fzzy_config.api.FileType
import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.util.Translatable
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean
import net.bms.roleplay_status.RoleplayStatus
import net.minecraft.resources.ResourceLocation

@RootConfig
@Translatable.Name("Dice rolls")
class DiceConfig : Config(ResourceLocation.fromNamespaceAndPath(RoleplayStatus.MOD_ID, "dice")) {
    @Translatable.Name("Enable dice rolls")
    var enabled = ValidatedBoolean(true)
    @Translatable.Name("Sharing range (blocks)")
    @Translatable.Desc("Players within this distance in the same dimension receive the result and optional animation.")
    var range = ValidatedInt(25, 256, 1)
    @Translatable.Name("Cooldown (ticks)")
    @Translatable.Desc("Minimum time between rolls per player. 20 ticks is one second.")
    var cooldownTicks = ValidatedInt(20, 1200, 0)
    override fun defaultPermLevel() = 2
    override fun fileType() = FileType.JSON
    companion object { fun register(): DiceConfig = ConfigApiJava.registerAndLoadConfig(::DiceConfig) }
}
