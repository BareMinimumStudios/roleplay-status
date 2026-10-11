package net.bms.roleplay_status;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StatusCatalog {

    public static final String IC = "roleplay_status:ic";
    public static final String OOC = "roleplay_status:ooc";
    public static final String OPEN_RP = "roleplay_status:open_rp";
    public static final String BUSY_AFK = "roleplay_status:busy_afk";
    public static final String EVENT = "roleplay_status:event";
    public static final String NEW = "roleplay_status:new";
    public static final String RECORDING = "recording";
    public static final String STREAMING = "streaming";

    private static final LinkedHashMap<String, Definition> DEFAULTS = new LinkedHashMap<>();

    static {
        add(IC, "In Character", 0x2EEC23);
        add(OOC, "Out of Character", 0xFF3347);
        add(OPEN_RP, "Open to RP", 0xFFC83D);
        add(BUSY_AFK, "Busy / AFK", 0x8A8A8A);
        add(EVENT, "Event", 0xA855F7);
        add(NEW, "New", 0x42D9FF);
        add(RECORDING, "Recording", 0xFF0014);
        add(STREAMING, "Streaming", 0x8821FF);
    }

    private static void add(String id, String label, int rgb) {
        DEFAULTS.put(id, new Definition(id, label, rgb));
    }

    public static List<Definition> defaults() {
        return new ArrayList<>(DEFAULTS.values());
    }

    public static Map<String, Definition> defaultMap() {
        return Map.copyOf(DEFAULTS);
    }

    public static boolean isAllowed(String state) {
        return state != null && DEFAULTS.containsKey(state);
    }

    public static String sanitize(String state) {
        if (state != null && state.startsWith("andromeda:")) {
            state = "roleplay_status:" + state.substring("andromeda:".length());
        }
        return isAllowed(state) ? state : OOC;
    }

    public static String sanitizeLabel(String label, String fallback) {
        if (label == null) return fallback;
        StringBuilder clean = new StringBuilder();
        label.codePoints().filter(c -> !Character.isISOControl(c) && Character.getType(c) != Character.FORMAT && c != 0x00A7)
                .forEach(clean::appendCodePoint);
        String value = clean.toString().strip();
        if (value.isEmpty()) return fallback;
        if (value.length() > 64) {
            int end = Character.isHighSurrogate(value.charAt(63)) ? 63 : 64;
            value = value.substring(0, end);
        }
        return value;
    }

    public record Definition(String id, String label, int rgb) {
        public Definition {
            label = sanitizeLabel(label, DEFAULTS.containsKey(id) ? DEFAULTS.get(id).label() : id);
            rgb &= 0xFFFFFF;
        }
    }

    private StatusCatalog() {
    }
}
