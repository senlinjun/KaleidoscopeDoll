package com.github.ysbbbbbb.kaleidoscopedoll.init;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.item.ComputerBlockItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollMachineBlockItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollEntityItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.GiftBoxItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.TweaksToolItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public final class ModItems {
    public static final Item DOLL_MACHINE = new DollMachineBlockItem(ModBlocks.DOLL_MACHINE, blockItemProps("doll_machine"));
    public static final Item PURPLE_DOLL_GIFT_BOX = new GiftBoxItem(ModBlocks.PURPLE_DOLL_GIFT_BOX, blockItemProps("purple_doll_gift_box"));
    public static final Item GREEN_DOLL_GIFT_BOX = new GiftBoxItem(ModBlocks.GREEN_DOLL_GIFT_BOX, blockItemProps("green_doll_gift_box"));
    public static final Item YELLOW_DOLL_GIFT_BOX = new GiftBoxItem(ModBlocks.YELLOW_DOLL_GIFT_BOX, blockItemProps("yellow_doll_gift_box"));
    public static final Item COMPUTER = new ComputerBlockItem(ModBlocks.COMPUTER, blockItemProps("computer"));
    public static final Item TWEAKS_TOOL = new TweaksToolItem(itemProps("tweaks_tool"));
    public static final Item DOLL_ICON = new Item(itemProps("doll_icon"));
    public static final Item DOLL_ENTITY_ITEM = new DollEntityItem(itemProps("doll_entity_item"));
    public static final Item CUSTOM_DOLL = new CustomDollItem(ModBlocks.CUSTOM_DOLL, dollItemProps("custom_doll"));

    private static Item.Properties itemProps(String path) {
        return new Item.Properties().setId(
                ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, path)));
    }

    private static Item.Properties blockItemProps(String path) {
        return itemProps(path).useBlockDescriptionPrefix();
    }

    private static Item.Properties dollItemProps(String path) {
        Item.Properties properties = itemProps(path);
        com.github.ysbbbbbb.kaleidoscopedoll.compat.trinkets.TrinketsCompat.applyDollComponent(properties);
        return properties;
    }

    public static void registerItems() {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "doll_machine"), DOLL_MACHINE);
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "purple_doll_gift_box"), PURPLE_DOLL_GIFT_BOX);
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "green_doll_gift_box"), GREEN_DOLL_GIFT_BOX);
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "yellow_doll_gift_box"), YELLOW_DOLL_GIFT_BOX);
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "computer"), COMPUTER);
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "tweaks_tool"), TWEAKS_TOOL);
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "doll_icon"), DOLL_ICON);
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "doll_entity_item"), DOLL_ENTITY_ITEM);
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "custom_doll"), CUSTOM_DOLL);
    }
}
