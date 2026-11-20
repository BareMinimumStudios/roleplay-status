package net.bms.roleplay_status

import net.bms.roleplay_status.config.RoleplayStatusConfig
import org.slf4j.LoggerFactory
import java.nio.file.Path

object RoleplayStatus {
    const val MOD_ID = "roleplay_status"
    @JvmField val LOGGER = LoggerFactory.getLogger(MOD_ID)
    lateinit var CONFIG: RoleplayStatusConfig
        private set
    lateinit var DICE: net.bms.roleplay_status.config.DiceConfig
        private set
    fun initialize(configDir: Path) {
        DICE = net.bms.roleplay_status.config.DiceConfig.register()
        CONFIG = RoleplayStatusConfig.register(configDir)
    }
}
