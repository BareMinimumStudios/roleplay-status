package net.bms.roleplay_status.state

import net.bms.roleplay_status.StatusCatalog

data class PlayerStatus(val status: String = StatusCatalog.OOC, val noSleep: Boolean = false) {
    companion object {
        fun validated(status: String, noSleep: Boolean): PlayerStatus? =
            if (StatusCatalog.isAllowed(status)) PlayerStatus(status, noSleep) else null
    }
}
