package com.github.ysbbbbbb.kaleidoscopedoll;

import com.github.ysbbbbbb.kaleidoscopedoll.config.GeneralConfig;
import com.github.ysbbbbbb.kaleidoscopedoll.event.DollClickEntityEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.event.EnterServerEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.event.PhantomSpawnEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.init.*;
import com.github.ysbbbbbb.kaleidoscopedoll.init.registry.CommandRegistry;
import com.github.ysbbbbbb.kaleidoscopedoll.init.registry.DatapackRegistry;
import com.github.ysbbbbbb.kaleidoscopedoll.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

public class KaleidoscopeDoll implements ModInitializer {
    public static final String MOD_ID = "kaleidoscope_doll";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, GeneralConfig.init());
        ModBlocks.registerBlocks();
        ModBlocks.registerBlockEntities();
        ModItems.registerItems();
        ModCreativeTabs.registerTabs();
        ModSounds.registerSounds();
        ModEntities.registerEntities();
        ModRecipes.registerRecipe();
        ModDataComponent.registerDataComponent();
        ModContainer.registerContainer();

        DollClickEntityEvent.register();
        PhantomSpawnEvent.register();
        EnterServerEvent.register();
        CommandRegistry.register();
        DatapackRegistry.register();
        NetworkHandler.init();
    }
}
