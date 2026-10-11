package net.bms.roleplay_status

object StatusCatalog {
    const val IC = "roleplay_status:ic"
    const val OOC = "roleplay_status:ooc"
    const val OPEN_RP = "roleplay_status:open_rp"
    const val BUSY_AFK = "roleplay_status:busy_afk"
    const val EVENT = "roleplay_status:event"
    const val NEW = "roleplay_status:new"
    const val RECORDING = "recording"
    const val STREAMING = "streaming"

    private val defaults = linkedMapOf(
        IC to Definition(IC, "In Character", 0x2EEC23),
        OOC to Definition(OOC, "Out of Character", 0xFF3347),
        OPEN_RP to Definition(OPEN_RP, "Open to RP", 0xFFC83D),
        BUSY_AFK to Definition(BUSY_AFK, "Busy / AFK", 0x8A8A8A),
        EVENT to Definition(EVENT, "Event", 0xA855F7),
        NEW to Definition(NEW, "New", 0x42D9FF),
        RECORDING to Definition(RECORDING, "Recording", 0xFF0014),
        STREAMING to Definition(STREAMING, "Streaming", 0x8821FF)
    )

    @JvmStatic fun defaults(): MutableList<Definition> = ArrayList(defaults.values)
    @JvmStatic fun defaultMap(): Map<String, Definition> = java.util.Map.copyOf(defaults)
    @JvmStatic fun isAllowed(state: String?): Boolean = state != null && defaults.containsKey(state)

    @JvmStatic fun sanitize(state: String?): String {
        val migrated = if (state?.startsWith("andromeda:") == true) {
            "roleplay_status:" + state.removePrefix("andromeda:")
        } else state
        return migrated?.takeIf(::isAllowed) ?: OOC
    }

    @JvmStatic fun sanitizeLabel(label: String?, fallback: String): String {
        if (label == null) return fallback
        val clean = StringBuilder()
        label.codePoints().filter {
            !Character.isISOControl(it) && Character.getType(it) != Character.FORMAT.toInt() && it != 0xA7
        }.forEach { clean.appendCodePoint(it) }
        val value = clean.toString().trim { Character.isWhitespace(it) }
        if (value.isEmpty()) return fallback
        if (value.length <= 64) return value
        return value.substring(0, if (Character.isHighSurrogate(value[63])) 63 else 64)
    }

    // Independent of the defaults map so construction is safe during object initialization.
    private fun defaultLabel(id: String): String = when (id) {
        IC -> "In Character"
        OOC -> "Out of Character"
        OPEN_RP -> "Open to RP"
        BUSY_AFK -> "Busy / AFK"
        EVENT -> "Event"
        NEW -> "New"
        RECORDING -> "Recording"
        STREAMING -> "Streaming"
        else -> id
    }

    class Definition(val id: String, label: String?, rgb: Int) {
        val label = sanitizeLabel(label, defaultLabel(id))
        val rgb = rgb and 0xFFFFFF
        fun id() = id
        fun label() = label
        fun rgb() = rgb
        override fun equals(other: Any?): Boolean = other is Definition &&
            id == other.id && label == other.label && rgb == other.rgb
        override fun hashCode(): Int = 31 * (31 * id.hashCode() + label.hashCode()) + rgb
        override fun toString() = "Definition(id=$id, label=$label, rgb=$rgb)"
    }
}
