package net.bms.roleplay_status.state

import net.bms.roleplay_status.RoleplayStatus
import net.bms.roleplay_status.StatusCatalog
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.util.datafix.DataFixTypes
import java.util.UUID

class StatusData : SavedData() {
    private val players = mutableMapOf<UUID, PlayerStatus>()
    fun get(id: UUID): PlayerStatus = players[id] ?: PlayerStatus()
    fun set(id: UUID, value: PlayerStatus) {
        if (get(id) == value) return
        players[id] = value
        setDirty()
    }
    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        val entries = CompoundTag()
        players.forEach { (id, state) ->
            entries.put(id.toString(), CompoundTag().apply {
                putString("status", state.status)
                putBoolean("noSleep", state.noSleep)
            })
        }
        tag.putInt("schema", 1)
        tag.put("players", entries)
        return tag
    }
    companion object {
        val FACTORY = Factory(::StatusData, { tag, _ -> load(tag) }, DataFixTypes.LEVEL)
        private fun load(tag: CompoundTag): StatusData = StatusData().apply {
            val entries = tag.getCompound("players")
            for (id in entries.allKeys) {
                try {
                    val entry = entries.getCompound(id)
                    players[UUID.fromString(id)] = PlayerStatus(
                        StatusCatalog.sanitize(entry.getString("status")), entry.getBoolean("noSleep"))
                } catch (e: IllegalArgumentException) {
                    RoleplayStatus.LOGGER.warn("Ignoring invalid saved player id {}", id)
                }
            }
        }
    }
}
