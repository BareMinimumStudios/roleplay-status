package net.bms.roleplay_status

import java.nio.file.Path
import net.bms.roleplay_status.config.DiceConfig
import net.bms.roleplay_status.config.RoleplayStatusConfig
import org.slf4j.LoggerFactory

object RoleplayStatus {
    const val MOD_ID = "roleplay_status"
    @JvmField val LOGGER = LoggerFactory.getLogger(MOD_ID)
    lateinit var CONFIG: RoleplayStatusConfig
        private set
    lateinit var DICE: DiceConfig
        private set
    fun initialize(configDir: Path) {
        DICE = DiceConfig.register()
        CONFIG = RoleplayStatusConfig.register(configDir)
    }
}
