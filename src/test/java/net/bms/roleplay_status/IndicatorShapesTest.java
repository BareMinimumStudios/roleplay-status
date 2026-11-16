package net.bms.roleplay_status;

import net.bms.roleplay_status.client.IndicatorShapes;
import net.bms.roleplay_status.client.TabIndicator;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.BitSet;
import static org.junit.jupiter.api.Assertions.*;

class IndicatorShapesTest {
    @org.junit.jupiter.api.BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }
    @Test void allEightStatusesRemainDistinctWithoutAnyColor() {
        var masks = new HashSet<BitSet>();
        for (var definition : StatusCatalog.defaults()) {
            int shape = IndicatorShapes.forStatus(definition.id(), true);
            var mask = new BitSet(64);
            for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++)
                if (IndicatorShapes.contains(shape, x, y)) mask.set(y * 8 + x);
            assertFalse(mask.isEmpty());
            assertTrue(masks.add(mask), "Duplicate silhouette for " + definition.id());
            assertFalse(IndicatorShapes.contains(shape, -1, 0));
            assertFalse(IndicatorShapes.contains(shape, 8, 7));
        }
        assertEquals(8, masks.size());
    }
    @Test void normalModeKeepsFullSquaresAndActivityHalfSquares() {
        assertEquals(IndicatorShapes.SOLID, IndicatorShapes.forStatus(StatusCatalog.OOC, false));
        assertEquals(IndicatorShapes.RIGHT, IndicatorShapes.forStatus(StatusCatalog.RECORDING, false));
        assertEquals(IndicatorShapes.RIGHT, IndicatorShapes.forStatus(StatusCatalog.STREAMING, false));
    }
    @Test void accessibilityLabelsAndShapeMarkersPreserveCustomNamesAndColors() {
        var name = Component.literal("[DEV] Vaelith").withStyle(style -> style.withColor(0xAA0011));
        var result = TabIndicator.decorate(name, "Streaming", 0x3B82F6, true, IndicatorShapes.LEFT, true);
        assertEquals(IndicatorShapes.LEFT, TabIndicator.shape(result));
        assertTrue(result.getString().endsWith("[DEV] Vaelith [Streaming]"));
        assertSame(name, result.getSiblings().get(1));
        assertEquals(0x3B82F6, TabIndicator.color(result));
        assertEquals("[DEV] Vaelith", name.getString());
        assertFalse(TabIndicator.decorate(name, "Streaming", 0x3B82F6, true).getString().endsWith("[Streaming]"));
    }
}
