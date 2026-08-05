package com.github.ysbbbbbb.kaleidoscopedoll.network.message;

import com.github.ysbbbbbb.kaleidoscopedoll.inventory.ComputerMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record ComputerDollClickMessage(ItemStack doll) implements CustomPacketPayload {

    public static final Type<ComputerDollClickMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath("kaleidoscope_doll", "computer_doll_click"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComputerDollClickMessage> CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ComputerDollClickMessage::doll,
            ComputerDollClickMessage::new);

    public static void handle(ComputerDollClickMessage message, ServerPlayer player) {
        if (player == null) {
            return;
        }
        player.resetLastActionTime();
        if (player.containerMenu instanceof ComputerMenu computerMenu
            && !player.isSpectator()
            && computerMenu.clickDollButton(message.doll)) {
            player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
