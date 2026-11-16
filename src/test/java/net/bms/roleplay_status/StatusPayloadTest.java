package net.bms.roleplay_status;

import io.netty.buffer.Unpooled;
import net.bms.roleplay_status.network.StatusPayloads;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class StatusPayloadTest {
    @Test void roundTripsPlayerIdentityStateAndRemovalOnTheWire() {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            var payload = new StatusPayloads.Sync(UUID.randomUUID(), StatusCatalog.STREAMING, true, false);
            StatusPayloads.INSTANCE.getSYNC_CODEC().encode(buf, payload);
            assertEquals(payload, StatusPayloads.INSTANCE.getSYNC_CODEC().decode(buf));
            var removed = new StatusPayloads.Sync(payload.getPlayer(), "", false, true);
            StatusPayloads.INSTANCE.getSYNC_CODEC().encode(buf, removed);
            assertEquals(removed, StatusPayloads.INSTANCE.getSYNC_CODEC().decode(buf));
        } finally { buf.release(); }
    }

    @Test void boundsClientProvidedStatusStrings() {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            buf.writeUtf("x".repeat(65));
            buf.writeBoolean(false);
            assertThrows(io.netty.handler.codec.DecoderException.class,
                    () -> StatusPayloads.INSTANCE.getCHANGE_CODEC().decode(buf));
        } finally { buf.release(); }
    }

    @Test void roundTripsClientSelectionAndProtocolHandshake() {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            var change = new StatusPayloads.Change(StatusCatalog.OPEN_RP, true);
            StatusPayloads.INSTANCE.getCHANGE_CODEC().encode(buf, change);
            assertEquals(change, StatusPayloads.INSTANCE.getCHANGE_CODEC().decode(buf));
            var hello = new StatusPayloads.Hello(StatusPayloads.PROTOCOL);
            StatusPayloads.INSTANCE.getHELLO_CODEC().encode(buf, hello);
            assertEquals(hello, StatusPayloads.INSTANCE.getHELLO_CODEC().decode(buf));
        } finally { buf.release(); }
    }
}
