package com.github.ysbbbbbb.kaleidoscopedoll.network.message;

import com.google.common.collect.Sets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.Set;

public class CustomDollReloadMessage implements CustomPacketPayload {
    public static final Type<CustomDollReloadMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath("kaleidoscope_doll", "custom_doll_reload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CustomDollReloadMessage> CODEC = StreamCodec.of(
            (buf, message) -> {
                buf.writeVarInt(message.modelIds.size());
                for (String modelId : message.modelIds) {
                    buf.writeUtf(modelId);
                }
            },
            buf -> {
                int size = buf.readVarInt();
                Set<String> modelIds = Sets.newHashSet();
                for (int i = 0; i < size; i++) {
                    modelIds.add(buf.readUtf());
                }
                return new CustomDollReloadMessage(modelIds);
            });

    private final Set<String> modelIds;

    public CustomDollReloadMessage(Set<String> modelIds) {
        this.modelIds = modelIds;
    }

    public Set<String> getModelIds() {
        return modelIds;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
