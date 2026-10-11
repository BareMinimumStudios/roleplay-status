package net.bms.roleplay_status;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusCatalogTest {
    @Test void migratesKnownLegacyIdsAndRejectsUnknownStates() {
        assertEquals(StatusCatalog.IC, StatusCatalog.sanitize("andromeda:ic"));
        assertEquals(StatusCatalog.OPEN_RP, StatusCatalog.sanitize("andromeda:open_rp"));
        assertEquals(StatusCatalog.OOC, StatusCatalog.sanitize("available"));
        assertEquals(StatusCatalog.OOC, StatusCatalog.sanitize(null));
        assertEquals(StatusCatalog.OOC, StatusCatalog.sanitize("andromeda:unknown"));
        assertEquals("recording", StatusCatalog.sanitize("recording"));
        assertEquals("streaming", StatusCatalog.sanitize("streaming"));
    }
    @Test void sanitizesFormattingControlsAndBlankLabels() {
        assertEquals("Hello", StatusCatalog.sanitizeLabel("  He\u0000l\u202El§o  ", "Fallback"));
        assertEquals("Fallback", StatusCatalog.sanitizeLabel("\n\t ", "Fallback"));
        assertEquals("Fallback", StatusCatalog.sanitizeLabel(null, "Fallback"));
    }
    @Test void truncatesWithoutSplittingASurrogatePair() {
        String input = "a".repeat(63) + "\uD83D\uDE00";
        assertEquals("a".repeat(63), StatusCatalog.sanitizeLabel(input, "Fallback"));
        assertEquals(64, StatusCatalog.sanitizeLabel("b".repeat(100), "Fallback").length());
    }
    @Test void definitionsAreOrderedAndReturnedAsIndependentLists() {
        var list = StatusCatalog.defaults();
        assertEquals(8, list.size());
        assertEquals(StatusCatalog.IC, list.getFirst().id());
        list.clear();
        assertEquals(8, StatusCatalog.defaults().size());
    }
}
