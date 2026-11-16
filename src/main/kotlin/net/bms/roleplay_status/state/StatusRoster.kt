package net.bms.roleplay_status.state

import java.util.UUID

/** Resolve authoritative statuses without guessing from nicknames or formatted display text. */
class StatusRoster {
    private data class Entry(val profileName: String, val state: PlayerStatus)
    private val entries = mutableMapOf<UUID, Entry>()
    fun set(id: UUID, profileName: String, state: PlayerStatus) { entries[id] = Entry(profileName, state) }
    fun remove(id: UUID) { entries.remove(id) }
    fun clear() { entries.clear() }
    fun stateFor(id: UUID, profileName: String?): PlayerStatus = find(id, profileName) ?: PlayerStatus()
    fun find(id: UUID, profileName: String?): PlayerStatus? {
        entries[id]?.let { return it.state }
        if (profileName.isNullOrBlank()) return null
        return entries.values.filter { it.profileName.isNotEmpty() && it.profileName.equals(profileName, ignoreCase = true) }
            .singleOrNull()?.state
    }
}
