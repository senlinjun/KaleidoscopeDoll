package com.github.ysbbbbbb.kaleidoscopedoll.init;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.config.GeneralConfig;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollEntityItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;

import static com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent.AUTHOR_DOLLS;
import static com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent.SPECIAL_TOOLTIPS;

public class ModCreativeTabs {
    private static final Identifier VANILLA_ICON_ID = Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "doll_52");
    private static final Identifier AUTHOR_ICON_ID = Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "doll_0");
    private static final Identifier PLAYER_ICON_ID = Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "computer");
    private static final Identifier ENTITY_ICON_ID = Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "doll_5");

    private static final ResourceKey<CreativeModeTab> VANILLA_DOLL_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "vanilla_doll"));
    private static final ResourceKey<CreativeModeTab> PLAYER_DOLL_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "player_doll"));
    private static final ResourceKey<CreativeModeTab> AUTHOR_DOLL_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "author_doll"));
    private static final ResourceKey<CreativeModeTab> CUSTOM_DOLL_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "custom_doll"));
    private static final ResourceKey<CreativeModeTab> ENTITY_DOLL_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "entity_doll"));

    public static void registerTabs() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, VANILLA_DOLL_TAB, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("item_group.kaleidoscope_doll.vanilla_doll.name"))
                .icon(() -> BuiltInRegistries.ITEM.get(VANILLA_ICON_ID).orElseThrow().value().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    output.accept(ModItems.DOLL_MACHINE);
                    output.accept(ModItems.COMPUTER);
                    output.accept(ModItems.TWEAKS_TOOL);
                    output.accept(ModItems.PURPLE_DOLL_GIFT_BOX);
                    output.accept(ModItems.GREEN_DOLL_GIFT_BOX);
                    output.accept(ModItems.YELLOW_DOLL_GIFT_BOX);
                    ModRegisterEvent.DOLL_ITEMS.stream()
                            .filter(item -> !SPECIAL_TOOLTIPS.containsKey(BuiltInRegistries.ITEM.getKey(item)))
                            .forEach(output::accept);
                })
                .build());

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, PLAYER_DOLL_TAB, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                .title(Component.translatable("item_group.kaleidoscope_doll.player_doll.name"))
                .icon(() -> BuiltInRegistries.ITEM.get(PLAYER_ICON_ID).orElseThrow().value().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    if (!GeneralConfig.ENABLE_SPONSORED_DOLL.get()) {
                        output.accept(Items.BARRIER);
                    } else {
                        ModRegisterEvent.DOLL_ITEMS.stream()
                                .filter(item -> SPECIAL_TOOLTIPS.containsKey(BuiltInRegistries.ITEM.getKey(item))
                                        && !AUTHOR_DOLLS.contains(BuiltInRegistries.ITEM.getKey(item)))
                                .forEach(output::accept);
                    }
                })
                .build());

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, AUTHOR_DOLL_TAB, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2)
                .title(Component.translatable("item_group.kaleidoscope_doll.author_doll.name"))
                .icon(() -> BuiltInRegistries.ITEM.get(AUTHOR_ICON_ID).orElseThrow().value().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    ModRegisterEvent.DOLL_ITEMS.stream()
                            .filter(item -> AUTHOR_DOLLS.contains(BuiltInRegistries.ITEM.getKey(item)))
                            .forEach(output::accept);
                })
                .build());

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_DOLL_TAB, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3)
                .title(Component.translatable("item_group.kaleidoscope_doll.custom_doll.name"))
                .icon(CustomDollItem::getDefaultItemStack)
                .displayItems((parameters, output) -> {
                    CustomDollItem.addCreativeTab(output);
                })
                .build());

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ENTITY_DOLL_TAB, CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4)
                .title(Component.translatable("item_group.kaleidoscope_doll.entity_doll.name"))
                .icon(() -> BuiltInRegistries.ITEM.get(ENTITY_ICON_ID).orElseThrow().value().getDefaultInstance())
                .displayItems((parameters, output) -> {
                    DollEntityItem.addCreativeTab(output);
                })
                .build());
    }

    public static void rebuildCustomDollTabs(net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters parameters) {
        // vanilla 的创造标签内容在打开时实时构建，无需手动刷新
    }
}
