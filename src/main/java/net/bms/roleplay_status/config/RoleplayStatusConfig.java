package net.bms.roleplay_status.config;

import net.bms.roleplay_status.RoleplayStatus;
import net.bms.roleplay_status.StatusCatalog;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.fzzyhmstrs.fzzy_config.annotations.RootConfig;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.util.Translatable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedColor;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@RootConfig
@Version(version = 1)
@Translatable.Name("Roleplay Status")
@Translatable.Desc("Server-owned status labels and colors. Operators with permission level 2 can edit these settings. Accepted changes apply live.")
public class RoleplayStatusConfig extends Config {
    private static Consumer<List<StatusCatalog.Definition>> clientListener = ignored -> {};
    @Translatable.Name("In Character")
    public StatusEntry inCharacter = new StatusEntry("In Character", 0x2EEC23);
    @Translatable.Name("Out of Character")
    public StatusEntry outOfCharacter = new StatusEntry("Out of Character", 0xFF3347);
    @Translatable.Name("Open to RP")
    public StatusEntry openToRoleplay = new StatusEntry("Open to RP", 0xFFC83D);
    @Translatable.Name("Busy / AFK")
    public StatusEntry busyAfk = new StatusEntry("Busy / AFK", 0x8A8A8A);
    @Translatable.Name("Event")
    public StatusEntry event = new StatusEntry("Event", 0xA855F7);
    @Translatable.Name("New")
    public StatusEntry newPlayer = new StatusEntry("New", 0x42D9FF);
    @Translatable.Name("Recording")
    public StatusEntry recording = new StatusEntry("Recording", 0xFF0014);
    @Translatable.Name("Streaming")
    public StatusEntry streaming = new StatusEntry("Streaming", 0x8821FF);

    public RoleplayStatusConfig() {
        super(ResourceLocation.fromNamespaceAndPath(RoleplayStatus.MOD_ID, "statuses"));
    }

    @Override public FileType fileType() { return FileType.JSON; }
    @Override public int defaultPermLevel() { return 2; }

    public static RoleplayStatusConfig register() {
        Path dir = FabricLoader.getInstance().getConfigDir();
        Path target = dir.resolve("roleplay_status/statuses.json");
        boolean migrate = !Files.exists(target)
                && !Files.exists(target.resolveSibling("statuses.toml"))
                && !Files.exists(target.resolveSibling("statuses.json5"))
                && !Files.exists(target.resolveSibling("statuses.jsonc"));
        RoleplayStatusConfig config = new RoleplayStatusConfig();
        Path old = dir.resolve("andromeda_status/statuses.json");
        if (migrate && Files.isRegularFile(old)) {
            try (Reader reader = Files.newBufferedReader(old)) {
                JsonObject legacy = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("statuses");
                if (legacy == null) throw new IllegalArgumentException("Missing statuses object");
                config.inCharacter.copyFrom(legacy, StatusCatalog.IC);
                config.outOfCharacter.copyFrom(legacy, StatusCatalog.OOC);
                config.openToRoleplay.copyFrom(legacy, StatusCatalog.OPEN_RP);
                config.busyAfk.copyFrom(legacy, StatusCatalog.BUSY_AFK);
                config.event.copyFrom(legacy, StatusCatalog.EVENT);
                config.newPlayer.copyFrom(legacy, StatusCatalog.NEW);
                config.recording.copyFrom(legacy, StatusCatalog.RECORDING);
                config.streaming.copyFrom(legacy, StatusCatalog.STREAMING);
                RoleplayStatus.LOGGER.info("Migrated Roleplay Status configuration from {} (original preserved)", old);
            } catch (Exception e) {
                RoleplayStatus.LOGGER.error("Could not migrate {}; original preserved", old, e);
            }
        }
        // Seed synchronously before registering: no race with Fzzy's initial
        // asynchronous save, and no empty file on an immediate server exit.
        if (migrate) config.save();
        return ConfigApiJava.registerAndLoadConfig(RoleplayStatusConfig::new);
    }

    public List<StatusCatalog.Definition> definitions() {
        return List.of(
                inCharacter.definition(StatusCatalog.IC),
                outOfCharacter.definition(StatusCatalog.OOC),
                openToRoleplay.definition(StatusCatalog.OPEN_RP),
                busyAfk.definition(StatusCatalog.BUSY_AFK),
                event.definition(StatusCatalog.EVENT),
                newPlayer.definition(StatusCatalog.NEW),
                recording.definition(StatusCatalog.RECORDING),
                streaming.definition(StatusCatalog.STREAMING)
        );
    }

    public static void setClientListener(Consumer<List<StatusCatalog.Definition>> listener) {
        clientListener = listener;
    }
    @Override public void onSyncClient() { clientListener.accept(definitions()); }
    @Override public void onUpdateClient() { clientListener.accept(definitions()); }

    public static class StatusEntry extends ConfigSection {
        @Translatable.Name("Label")
        @Translatable.Desc("Display name, 1–64 characters. Used by the status screen and its tooltips.")
        public ValidatedString label;
        @Translatable.Name("Color")
        @Translatable.Desc("Status icon and screen swatch color. Choose a color or enter its RGB hex value.")
        public ValidatedColor color;

        public StatusEntry(String name, int rgb) {
            label = new ValidatedString(name, "[^\\p{Cc}\\p{Cf}§]{1,64}");
            color = new ValidatedColor((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255).withDyeColorPresets();
        }

        StatusCatalog.Definition definition(String id) {
            return new StatusCatalog.Definition(id, StatusCatalog.sanitizeLabel(label.get(), StatusCatalog.defaultMap().get(id).label()), color.toInt());
        }

        void copyFrom(JsonObject legacy, String id) {
            String oldId = id.replace("roleplay_status:", "andromeda:");
            if (!legacy.has(oldId) || !legacy.get(oldId).isJsonObject()) return;
            JsonObject entry = legacy.getAsJsonObject(oldId);
            if (entry.has("label") && entry.get("label").isJsonPrimitive()
                    && entry.getAsJsonPrimitive("label").isString()) {
                label.validateAndSet(StatusCatalog.sanitizeLabel(entry.get("label").getAsString(), label.get()));
            }
            if (entry.has("color") && entry.get("color").isJsonPrimitive()
                    && entry.getAsJsonPrimitive("color").isString()) {
                String hex = entry.get("color").getAsString().trim().replaceFirst("^#", "");
                if (hex.matches("[0-9a-fA-F]{6}")) {
                    int rgb = Integer.parseInt(hex, 16);
                    // Avoid ColorHolder.fromInt in Fzzy 0.7.7, which copies green into blue.
                    color.validateAndSet(new ValidatedColor.ColorHolder((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, 255, false));
                }
            }
        }
    }
}

