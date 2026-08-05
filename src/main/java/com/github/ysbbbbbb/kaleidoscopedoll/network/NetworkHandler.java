package com.github.ysbbbbbb.kaleidoscopedoll.network;

import com.github.ysbbbbbb.kaleidoscopedoll.network.message.ComputerDollClickMessage;
import com.github.ysbbbbbb.kaleidoscopedoll.network.message.CustomDollReloadMessage;
import com.github.ysbbbbbb.kaleidoscopedoll.network.message.DollTweakersMessage;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHandler {
    public static void init() {
        // C2S
        PayloadTypeRegistry.serverboundPlay().register(DollTweakersMessage.TYPE, DollTweakersMessage.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ComputerDollClickMessage.TYPE, ComputerDollClickMessage.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DollTweakersMessage.TYPE, (payload, context) ->
                context.server().execute(() -> DollTweakersMessage.handle(payload, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(ComputerDollClickMessage.TYPE, (payload, context) ->
                context.server().execute(() -> ComputerDollClickMessage.handle(payload, context.player())));

        // S2C
        PayloadTypeRegistry.clientboundPlay().register(CustomDollReloadMessage.TYPE, CustomDollReloadMessage.CODEC);
    }

    public static void sendToClient(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
