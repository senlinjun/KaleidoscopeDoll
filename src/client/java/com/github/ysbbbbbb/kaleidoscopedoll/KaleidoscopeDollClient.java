package com.github.ysbbbbbb.kaleidoscopedoll;

import com.github.ysbbbbbb.kaleidoscopedoll.client.ClientNetworkHandler;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.client.gui.ComputerMenuScreen;
import com.github.ysbbbbbb.kaleidoscopedoll.client.gui.TweaksToolScreen;
import com.github.ysbbbbbb.kaleidoscopedoll.client.render.CustomDollRender;
import com.github.ysbbbbbb.kaleidoscopedoll.client.render.DollItemModels;
import com.github.ysbbbbbb.kaleidoscopedoll.client.render.DollEntityRender;
import com.github.ysbbbbbb.kaleidoscopedoll.client.resources.CustomDollReloadListener;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModContainer;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModEntities;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

import java.util.Arrays;
import java.util.List;

public class KaleidoscopeDollClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DollItemModels.register();

        EntityRendererRegistry.register(ModEntities.DOLL, DollEntityRender::new);
        BlockEntityRendererRegistry.register(ModBlocks.CUSTOM_DOLL_BE, CustomDollRender::new);

        MenuScreens.register(ModContainer.COMPUTER_CONTAINER, ComputerMenuScreen::new);

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new CustomDollReloadListener());

        ClientNetworkHandler.register();

        ClientHooks.OPEN_TWEAKS_TOOL_SCREEN = TweaksToolScreen::openScreen;
        ClientHooks.CUSTOM_DOLL_TOOLTIP = stack -> {
            String modelId = CustomDollItem.getModelId(stack);
            if (modelId.isEmpty()) {
                return List.of();
            }
            String locale = Minecraft.getInstance().getLanguageManager().getSelected();
            // 按照换行进行分割
            String[] lines = CustomDollLoader.getLanguage(locale, modelId).split("\n");
            return Arrays.stream(lines)
                    .map(line -> (Component) Component.literal(line).withStyle(ChatFormatting.DARK_GRAY))
                    .toList();
        };
    }
}
