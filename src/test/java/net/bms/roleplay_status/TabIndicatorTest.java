package net.bms.roleplay_status;

import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import net.bms.roleplay_status.client.TabIndicator;
import net.bms.roleplay_status.state.PlayerStatus;
import net.bms.roleplay_status.state.StatusRoster;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TabIndicatorTest {
    @BeforeAll static void bootstrapMinecraftText() {
        net.minecraft.SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
    @Test void reservesBadgeSpaceWithoutChangingTabFormattingOrCustomFonts() {
        var original = Component.literal("[DEV] Vaelith").withStyle(style -> style.withColor(0xAA0011)
                .withFont(ResourceLocation.fromNamespaceAndPath("server", "custom")));
        var decorated = TabIndicator.decorate(original, "In Character", 0x2EEC23);
        assertEquals(0x2EEC23, TabIndicator.color(decorated));
        assertEquals(original, decorated.getSiblings().get(1));
        assertEquals("[DEV] Vaelith", original.getString());
        assertEquals(ResourceLocation.fromNamespaceAndPath("server", "custom"), original.getStyle().getFont());
        assertNull(decorated.getStyle().getColor());
        assertSame(decorated, TabIndicator.decorate(decorated, "In Character", 0x2EEC23));
        assertNull(TabIndicator.color(original));
    }

    @Test void resolvesChangedTabUuidByAuthoritativeAccountNameAndCleansUp() {
        var roster = new StatusRoster();
        var actual = UUID.randomUUID();
        var fake = UUID.randomUUID();
        var state = new PlayerStatus(StatusCatalog.IC, false);
        roster.set(actual, "Poke097", state);
        assertEquals(state, roster.find(fake, "poke097"));
        assertEquals(state, roster.find(actual, "different_display_name"));
        assertNull(roster.find(fake, "Vaelith"));
        roster.remove(actual);
        assertNull(roster.find(fake, "Poke097"));
    }

    @Test void neverGuessesForAmbiguousNamesOrFakeLayoutSlots() {
        var roster = new StatusRoster();
        roster.set(UUID.randomUUID(), "same", new PlayerStatus(StatusCatalog.IC, false));
        roster.set(UUID.randomUUID(), "SAME", new PlayerStatus(StatusCatalog.OOC, true));
        assertNull(roster.find(UUID.randomUUID(), "same"));
        assertNull(roster.find(UUID.randomUUID(), ""));
        roster.clear();
        assertNull(roster.find(UUID.randomUUID(), "same"));
    }

    @Test void decoratesAfterANameModReplacesTheWholeComponent() {
        // Simulate a competing RETURN injector discarding our previous component.
        var original = Component.literal("Poke097");
        var before = TabIndicator.decorate(original, "In Character", 0x2EEC23);
        var nameModReplacement = Component.literal("[DEV] Vaelith").withStyle(style -> style.withColor(0xAA0011));
        assertNull(TabIndicator.color(nameModReplacement));
        var after = TabIndicator.decorate(nameModReplacement, "In Character", 0x2EEC23);
        assertEquals(0x2EEC23, TabIndicator.color(after));
        assertSame(nameModReplacement, after.getSiblings().get(1));
        assertNotEquals(before.getString(), after.getString());
    }

    @Test void distinguishesActivityHalfSquaresFromFullRoleplaySquares() {
        var name = Component.literal("Player");
        var roleplay = TabIndicator.decorate(name, "Out of Character", 0xFF3347, false);
        var recording = TabIndicator.decorate(name, "Recording", 0xFF7A18, true);
        var streaming = TabIndicator.decorate(name, "Streaming", 0x3B82F6, true);
        assertFalse(TabIndicator.isActivity(roleplay));
        assertTrue(TabIndicator.isActivity(recording));
        assertTrue(TabIndicator.isActivity(streaming));
        assertEquals(0xFF7A18, TabIndicator.color(recording));
        assertEquals(0x3B82F6, TabIndicator.color(streaming));
        assertSame(name, recording.getSiblings().get(1));
        assertNotEquals(StatusCatalog.defaultMap().get(StatusCatalog.OOC).rgb(),
                StatusCatalog.defaultMap().get(StatusCatalog.RECORDING).rgb());
        assertNotEquals(StatusCatalog.defaultMap().get(StatusCatalog.EVENT).rgb(),
                StatusCatalog.defaultMap().get(StatusCatalog.STREAMING).rgb());
    }
}
