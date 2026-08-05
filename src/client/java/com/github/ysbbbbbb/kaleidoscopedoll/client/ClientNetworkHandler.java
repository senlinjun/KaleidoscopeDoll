package com.github.ysbbbbbb.kaleidoscopedoll.client;

import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.data.custom.ServerCustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.network.message.CustomDollReloadMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.io.IOException;
import java.util.Set;

public final class ClientNetworkHandler {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CustomDollReloadMessage.TYPE, (payload, context) ->
                context.client().execute(() -> CustomDollReloadClientHandler.handle(payload.getModelIds())));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    private ClientNetworkHandler() {
    }

    private static final class CustomDollReloadClientHandler {
        static void handle(Set<String> modelIds) {
            try {
                CustomDollLoader.init();

                // 如自己不是主机，则添加这些模型 ID
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (!mc.isLocalServer()) {
                    ServerCustomDollLoader.getModels().clear();
                    ServerCustomDollLoader.getModels().addAll(modelIds);
                }
            } catch (IOException e) {
                KaleidoscopeDoll.LOGGER.error("Failed to reload custom dolls", e);
            }
        }
    }
}
