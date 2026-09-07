package com.andromedamc.status.config;

import com.andromedamc.status.AndromedaStatus;
import com.andromedamc.status.StatusCatalog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AndromedaStatusConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Map<String, Entry> statuses = new LinkedHashMap<>();

    public static AndromedaStatusConfig defaults() {
        AndromedaStatusConfig config = new AndromedaStatusConfig();
        for (StatusCatalog.Definition definition : StatusCatalog.defaults()) {
            config.statuses.put(definition.id(), new Entry(definition.label(), String.format("#%06X", definition.rgb())));
        }
        return config;
    }

    public static AndromedaStatusConfig load(Path path) {
        AndromedaStatusConfig defaults = defaults();
        if (!Files.exists(path)) {
            defaults.save(path);
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            AndromedaStatusConfig loaded = GSON.fromJson(reader, AndromedaStatusConfig.class);
            if (loaded == null || loaded.statuses == null) {
                return defaults;
            }

            AndromedaStatusConfig merged = defaults();
            for (String id : StatusCatalog.defaultMap().keySet()) {
                Entry candidate = loaded.statuses.get(id);
                if (candidate == null) {
                    continue;
                }
                Entry fallback = merged.statuses.get(id);
                String label = sanitizeLabel(candidate.label, fallback.label);
                String color = parseColor(candidate.color) == null ? fallback.color : normalizeColor(candidate.color);
                merged.statuses.put(id, new Entry(label, color));
            }
            merged.save(path);
            return merged;
        } catch (Exception e) {
            AndromedaStatus.LOGGER.error("Failed to read {}, using defaults", path, e);
            return defaults;
        }
    }

    public List<StatusCatalog.Definition> definitions() {
        List<StatusCatalog.Definition> definitions = new ArrayList<>();
        for (StatusCatalog.Definition fallback : StatusCatalog.defaults()) {
            Entry entry = statuses.get(fallback.id());
            if (entry == null) {
                definitions.add(fallback);
                continue;
            }
            Integer color = parseColor(entry.color);
            definitions.add(new StatusCatalog.Definition(
                    fallback.id(),
                    sanitizeLabel(entry.label, fallback.label()),
                    color == null ? fallback.rgb() : color
            ));
        }
        return definitions;
    }

    public void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            AndromedaStatus.LOGGER.error("Failed to write {}", path, e);
        }
    }

    private static String sanitizeLabel(String label, String fallback) {
        String value = label == null || label.isBlank() ? fallback : label.trim();
        StringBuilder clean = new StringBuilder(value.length());
        value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .forEach(clean::appendCodePoint);
        String result = clean.toString().trim();
        if (result.isBlank()) {
            result = fallback;
        }
        return result.length() <= 64 ? result : result.substring(0, 64);
    }

    private static Integer parseColor(String color) {
        if (color == null) {
            return null;
        }
        String normalized = color.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.matches("[0-9A-Fa-f]{6}")) {
            return null;
        }
        return Integer.parseInt(normalized, 16);
    }

    private static String normalizeColor(String color) {
        Integer parsed = parseColor(color);
        return parsed == null ? "#FFFFFF" : String.format("#%06X", parsed);
    }

    public static final class Entry {
        public String label;
        public String color;

        public Entry() {
        }

        public Entry(String label, String color) {
            this.label = label;
            this.color = color;
        }
    }
}
