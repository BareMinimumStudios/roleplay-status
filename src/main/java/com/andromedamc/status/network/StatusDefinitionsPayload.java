package com.andromedamc.status.network;

import com.andromedamc.status.AndromedaStatus;
import com.andromedamc.status.StatusCatalog;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record StatusDefinitionsPayload(List<StatusCatalog.Definition> definitions) implements CustomPacketPayload {

    public static final Type<StatusDefinitionsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AndromedaStatus.MOD_ID, "definitions")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StatusDefinitionsPayload> CODEC = new StreamCodec<>() {
        @Override
        public StatusDefinitionsPayload decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            if (size < 0 || size > 32) {
                throw new IllegalArgumentException("Invalid status definition count: " + size);
            }
            List<StatusCatalog.Definition> entries = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                String id = buf.readUtf(64);
                String label = buf.readUtf(64);
                int rgb = buf.readInt() & 0xFFFFFF;
                entries.add(new StatusCatalog.Definition(id, label, rgb));
            }
            return new StatusDefinitionsPayload(entries);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, StatusDefinitionsPayload payload) {
            List<StatusCatalog.Definition> entries = payload.definitions();
            if (entries.size() > 32) {
                throw new IllegalArgumentException("Too many status definitions: " + entries.size());
            }
            buf.writeVarInt(entries.size());
            for (StatusCatalog.Definition entry : entries) {
                buf.writeUtf(entry.id(), 64);
                buf.writeUtf(entry.label(), 64);
                buf.writeInt(entry.rgb());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
