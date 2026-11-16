package net.bms.roleplay_status;

import net.bms.roleplay_status.client.JoinReminder;
import net.bms.roleplay_status.state.PlayerStatus;
import net.bms.roleplay_status.state.StatusRoster;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JoinReminderTest {
    @Test void automaticallyRemindsOncePerConnectionWithoutAHandshakeOrOptIn() {
        var reminder = new JoinReminder();
        for (int i = 0; i < 19; i++) assertFalse(reminder.tick());
        assertTrue(reminder.tick());
        for (int i = 0; i < 100; i++) assertFalse(reminder.tick());
        reminder.reset();
        for (int i = 0; i < 19; i++) assertFalse(reminder.tick());
        assertTrue(reminder.tick());
    }

    @Test void reminderUsesTheBoundKeyInGreenWithoutColoringTheRest() {
        var text = JoinReminder.message(Component.literal("U"));
        assertEquals("Please make sure to set your status with U!", text.getString());
        var keys = new ArrayList<String>();
        text.visit((style, part) -> {
            if (part.equals("U")) {
                assertEquals(ChatFormatting.GREEN.getColor(), style.getColor().getValue());
                keys.add(part);
            } else if (!part.isEmpty()) {
                assertEquals(ChatFormatting.WHITE.getColor(), style.getColor().getValue());
            }
            return Optional.empty();
        }, Style.EMPTY);
        assertEquals(1, keys.size());
        assertTrue(JoinReminder.message(Component.literal("R")).getString().endsWith("R!"));
    }

    @Test void onlinePlayersReceiveDefaultIndicatorsBeforeSettingAStatus() {
        var roster = new StatusRoster();
        var id = UUID.randomUUID();
        assertEquals(new PlayerStatus(StatusCatalog.OOC, false), roster.stateFor(id, "NewPlayer"));
        roster.set(id, "NewPlayer", new PlayerStatus(StatusCatalog.EVENT, true));
        assertEquals(StatusCatalog.EVENT, roster.stateFor(id, "NewPlayer").getStatus());
    }

    @Test void removesOnlyTheRecentEmptyEntryBeforeOurReminder() {
        var real = new net.minecraft.client.GuiMessage(90, Component.literal("Welcome"), null, null);
        var empty = new net.minecraft.client.GuiMessage(100, Component.empty(), null, null);
        var messages = new ArrayList<net.minecraft.client.GuiMessage>(java.util.List.of(empty, real));
        var lines = new ArrayList<net.minecraft.client.GuiMessage.Line>(java.util.List.of(
                new net.minecraft.client.GuiMessage.Line(100, net.minecraft.util.FormattedCharSequence.EMPTY, null, true)));
        JoinReminder.trimRecentBlank(messages, lines, 120);
        assertEquals(java.util.List.of(real), messages);
        assertTrue(lines.isEmpty());
    }

    @Test void keepsOldBlankSeparatorsAndVisibleRecentChat() {
        var messages = new ArrayList<net.minecraft.client.GuiMessage>(java.util.List.of(
                new net.minecraft.client.GuiMessage(0, Component.empty(), null, null)));
        var lines = new ArrayList<net.minecraft.client.GuiMessage.Line>(java.util.List.of(
                new net.minecraft.client.GuiMessage.Line(0, net.minecraft.util.FormattedCharSequence.EMPTY, null, true)));
        JoinReminder.trimRecentBlank(messages, lines, 200);
        assertEquals(1, messages.size());
        messages.set(0, new net.minecraft.client.GuiMessage(190, Component.literal("Hello"), null, null));
        JoinReminder.trimRecentBlank(messages, lines, 200);
        assertEquals("Hello", messages.getFirst().content().getString());
    }
}
