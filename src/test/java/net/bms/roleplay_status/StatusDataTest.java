package net.bms.roleplay_status;

import net.bms.roleplay_status.state.PlayerStatus;
import net.bms.roleplay_status.state.StatusData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class StatusDataTest {
    @Test void savesAndRestoresEachPlayersStatusAndSleepSetting() {
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        var data = new StatusData();
        var busy = new PlayerStatus(StatusCatalog.BUSY_AFK, true);
        var event = new PlayerStatus(StatusCatalog.EVENT, false);
        data.set(first, busy);
        data.set(second, event);
        assertTrue(data.isDirty());
        var saved = data.save(new CompoundTag(), RegistryAccess.EMPTY);
        var loaded = StatusData.Companion.getFACTORY().deserializer().apply(saved, RegistryAccess.EMPTY);
        assertEquals(busy, loaded.get(first));
        assertEquals(event, loaded.get(second));
        assertEquals(new PlayerStatus(StatusCatalog.OOC, false), loaded.get(UUID.randomUUID()));
    }

    @Test void ignoresMalformedIdsAndNormalizesLegacyAndUnknownSavedStates() {
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        var entries = new CompoundTag();
        var legacy = new CompoundTag();
        legacy.putString("status", "andromeda:ic");
        legacy.putBoolean("noSleep", true);
        entries.put(first.toString(), legacy);
        var unknown = new CompoundTag();
        unknown.putString("status", "removed:state");
        entries.put(second.toString(), unknown);
        entries.put("not-a-uuid", legacy);
        var tag = new CompoundTag();
        tag.put("players", entries);
        var loaded = StatusData.Companion.getFACTORY().deserializer().apply(tag, RegistryAccess.EMPTY);
        assertEquals(new PlayerStatus(StatusCatalog.IC, true), loaded.get(first));
        assertEquals(new PlayerStatus(StatusCatalog.OOC, false), loaded.get(second));
        assertEquals(2, loaded.save(new CompoundTag(), RegistryAccess.EMPTY).getCompound("players").size());
    }

    @Test void rejectsUnknownClientRequestsInsteadOfChangingThemToFallbacks() {
        assertNull(PlayerStatus.Companion.validated("arbitrary", true));
        assertNull(PlayerStatus.Companion.validated("andromeda:ic", false));
        for (var definition : StatusCatalog.defaults()) {
            assertNotNull(PlayerStatus.Companion.validated(definition.id(), true));
        }
    }

    @Test void reloadsCompressedWorldDataFromDisk(@org.junit.jupiter.api.io.TempDir java.nio.file.Path dir) throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        var id = UUID.randomUUID();
        var expected = new PlayerStatus(StatusCatalog.EVENT, true);
        var data = new StatusData();
        data.set(id, expected);
        var wrapper = new CompoundTag();
        wrapper.put("data", data.save(new CompoundTag(), RegistryAccess.EMPTY));
        net.minecraft.nbt.NbtUtils.addCurrentDataVersion(wrapper);
        net.minecraft.nbt.NbtIo.writeCompressed(wrapper, dir.resolve("roleplay_status.dat"));
        var storage = new net.minecraft.world.level.storage.DimensionDataStorage(dir.toFile(),
                net.minecraft.util.datafix.DataFixers.getDataFixer(), RegistryAccess.EMPTY);
        var loaded = storage.computeIfAbsent(StatusData.Companion.getFACTORY(), "roleplay_status");
        assertEquals(expected, loaded.get(id));
    }
}
