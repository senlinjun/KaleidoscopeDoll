package com.github.ysbbbbbb.kaleidoscopedoll.client.resources;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.BedrockModel;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollResourceLoader;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.IOException;
import java.io.InputStream;

public class CustomDollReloadListener implements ResourceManagerReloadListener, net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener {

    @Override
    public net.minecraft.resources.Identifier getFabricId() {
        return net.minecraft.resources.Identifier.fromNamespaceAndPath("kaleidoscope_doll", "custom_doll");
    }
    public static BedrockModel DFAULT_DOLL_MODEL;
    public static final Identifier DEFAULT_TEXTURE_ID = Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "textures/block/default_custom_doll.png");

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        try {
            if (DFAULT_DOLL_MODEL == null) {
                readDefaultModel(manager);
            }
            CustomDollResourceLoader.init(manager);
            CustomDollLoader.init();
        } catch (IOException e) {
            KaleidoscopeDoll.LOGGER.error("Failed to reload custom dolls", e);
        }
    }

    private static void readDefaultModel(ResourceManager manager) {
        manager.getResource(Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "models/bedrock/default_custom_doll.json")).ifPresent(res -> {
            try (InputStream stream = res.open()) {
                DFAULT_DOLL_MODEL = new BedrockModel(stream);
            } catch (Exception e) {
                KaleidoscopeDoll.LOGGER.error("Failed to load default custom doll model", e);
            }
        });
        manager.getResource(DEFAULT_TEXTURE_ID).ifPresent(res -> {
            try (InputStream stream = res.open()) {
                NativeImage image = NativeImage.read(stream);
                DynamicTexture texture = new DynamicTexture(() -> "kaleidoscope_doll_default", image);
                Minecraft.getInstance().getTextureManager().register(DEFAULT_TEXTURE_ID, texture);
            } catch (Exception e) {
                KaleidoscopeDoll.LOGGER.error("Failed to load default custom doll texture", e);
            }
        });
    }
}
