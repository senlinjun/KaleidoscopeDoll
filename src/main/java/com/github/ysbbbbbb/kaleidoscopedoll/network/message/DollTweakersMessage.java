package com.github.ysbbbbbb.kaleidoscopedoll.network.message;

import com.github.ysbbbbbb.kaleidoscopedoll.entity.DollEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.joml.Vector2f;
import org.joml.Vector3f;

public record DollTweakersMessage(int entityId, Vector3f scale, Vector3f translation, Vector2f rotation, CompoundTag itemDisplay)
        implements CustomPacketPayload {

    public static final Type<DollTweakersMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath("kaleidoscope_doll", "doll_tweakers"));

    private static final StreamCodec<io.netty.buffer.ByteBuf, Vector3f> VECTOR3F_CODEC =
            ByteBufCodecs.VECTOR3F.map(Vector3f::new, v -> v);

    public static final StreamCodec<RegistryFriendlyByteBuf, DollTweakersMessage> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DollTweakersMessage::entityId,
            VECTOR3F_CODEC, DollTweakersMessage::scale,
            VECTOR3F_CODEC, DollTweakersMessage::translation,
            StreamCodec.of((buf, rotation) -> {
                buf.writeFloat(rotation.x);
                buf.writeFloat(rotation.y);
            }, buf -> new Vector2f(buf.readFloat(), buf.readFloat())), DollTweakersMessage::rotation,
            ByteBufCodecs.COMPOUND_TAG, DollTweakersMessage::itemDisplay,
            DollTweakersMessage::new);

    public DollTweakersMessage {
        limitValues(scale, 0.1f, 10f);
        limitValues(translation, -5f, 5f);
        rotation.x = Math.max(-90f, Math.min(90f, rotation.x));
        rotation.y = ((rotation.y % 360) + 360) % 360;
    }

    private static void limitValues(Vector3f vec, float min, float max) {
        vec.x = Math.max(min, Math.min(max, vec.x));
        vec.y = Math.max(min, Math.min(max, vec.y));
        vec.z = Math.max(min, Math.min(max, vec.z));
    }

    public static void handle(DollTweakersMessage message, ServerPlayer player) {
        if (player == null) {
            return;
        }
        if (player.level().getEntity(message.entityId) instanceof DollEntity dollEntity) {
            dollEntity.setDisplayScale(message.scale);
            dollEntity.setDisplayTranslation(message.translation);
            dollEntity.setYRot(message.rotation.y);
            dollEntity.setXRot(message.rotation.x);
            dollEntity.setItemScale(readVector3f(message.itemDisplay.getCompoundOrEmpty("item_scale")));
            dollEntity.setItemTranslation(readVector3f(message.itemDisplay.getCompoundOrEmpty("item_translation")));
            dollEntity.setItemRotation(readVector3f(message.itemDisplay.getCompoundOrEmpty("item_rotation")));
        }
    }

    private static Vector3f readVector3f(CompoundTag tag) {
        return new Vector3f(tag.getFloatOr("x", 0), tag.getFloatOr("y", 0), tag.getFloatOr("z", 0));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
