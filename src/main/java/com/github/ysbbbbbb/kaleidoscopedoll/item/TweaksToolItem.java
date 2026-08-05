package com.github.ysbbbbbb.kaleidoscopedoll.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class TweaksToolItem extends Item {
    public TweaksToolItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag isAdvanced) {
        consumer.accept(Component.translatable("tooltip.kaleidoscope_doll.tweaks_tool.1").withStyle(ChatFormatting.DARK_GRAY));
        consumer.accept(Component.translatable("tooltip.kaleidoscope_doll.tweaks_tool.2").withStyle(ChatFormatting.DARK_GRAY));
    }
}
