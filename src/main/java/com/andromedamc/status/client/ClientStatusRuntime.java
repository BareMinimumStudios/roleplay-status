package com.andromedamc.status.client;

import com.andromedamc.status.StatusCatalog;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientStatusRuntime {

    private static final LinkedHashMap<String, StatusCatalog.Definition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<String, ResourceLocation> ICONS = new LinkedHashMap<>();
    private static boolean iconsDirty = true;

    static {
        for (StatusCatalog.Definition definition : StatusCatalog.defaults()) {
            DEFINITIONS.put(definition.id(), definition);
        }
    }

    public static void applyDefinitions(List<StatusCatalog.Definition> incoming) {
        LinkedHashMap<String, StatusCatalog.Definition> merged = new LinkedHashMap<>();
        for (StatusCatalog.Definition fallback : StatusCatalog.defaults()) {
            StatusCatalog.Definition replacement = incoming.stream()
                    .filter(entry -> entry.id().equals(fallback.id()))
                    .findFirst()
                    .orElse(fallback);
            merged.put(fallback.id(), replacement);
        }

        DEFINITIONS.clear();
        DEFINITIONS.putAll(merged);
        iconsDirty = true;
    }

    public static void resetToDefaults() {
        releaseIcons();
        applyDefinitions(StatusCatalog.defaults());
    }

    public static List<StatusCatalog.Definition> definitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static StatusCatalog.Definition definition(String id) {
        return DEFINITIONS.get(id);
    }

    public static ResourceLocation icon(String id) {
        return ICONS.get(id);
    }

    public static void rebuildIconsIfNeeded() {
        if (!iconsDirty) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getTextureManager() == null) {
            return;
        }
        rebuildIcons();
        iconsDirty = false;
    }

    private static void rebuildIcons() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getTextureManager() == null) {
            return;
        }

        releaseIcons();

        int index = 0;
        for (StatusCatalog.Definition definition : DEFINITIONS.values()) {
            NativeImage image = makeIcon(definition);
            DynamicTexture texture = new DynamicTexture(image);
            String path = "status_" + index++ + "_" + definition.id().replace(':', '_');
            ResourceLocation location = minecraft.getTextureManager().register("andromeda_status_" + path, texture);
            ICONS.put(definition.id(), location);
        }
    }

    private static void releaseIcons() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getTextureManager() == null) {
            ICONS.clear();
            return;
        }
        for (ResourceLocation location : ICONS.values()) {
            minecraft.getTextureManager().release(location);
        }
        ICONS.clear();
    }

    private static NativeImage makeIcon(StatusCatalog.Definition definition) {
        NativeImage image = new NativeImage(8, 8, true);
        int rgb = definition.rgb();
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int abgr = (0xFF << 24) | (b << 16) | (g << 8) | r;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                image.setPixelRGBA(x, y, 0);
            }
        }

        boolean legacyActivity = definition.id().equals(StatusCatalog.RECORDING)
                || definition.id().equals(StatusCatalog.STREAMING);

        if (legacyActivity) {
            for (int y = 0; y < 8; y++) {
                for (int x = 4; x < 8; x++) {
                    image.setPixelRGBA(x, y, abgr);
                }
            }
        } else {
            for (int y = 1; y < 7; y++) {
                for (int x = 1; x < 7; x++) {
                    image.setPixelRGBA(x, y, abgr);
                }
            }
        }
        return image;
    }

    private ClientStatusRuntime() {
    }
}
