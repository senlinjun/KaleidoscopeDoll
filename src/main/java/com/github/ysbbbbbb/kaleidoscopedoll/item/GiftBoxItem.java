package com.github.ysbbbbbb.kaleidoscopedoll.item;

import com.github.ysbbbbbb.kaleidoscopedoll.init.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class GiftBoxItem extends BlockItem {
    private static final String NBT_DOLL_ITEMSTACK = "DollItemStack";

    public GiftBoxItem(Block pBlock, Item.Properties properties) {
        super(pBlock, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag isAdvanced) {
        consumer.accept(Component.translatable("tooltip.kaleidoscope_doll.doll_gift_box").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static void setDoll(ItemStack doll, ItemStack giftBox, HolderLookup.Provider registries) {
        TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        output.store(NBT_DOLL_ITEMSTACK, ItemStack.CODEC, doll);
        setBlockEntityData(giftBox, ModBlocks.DOLL_GIFT_BOX_BE, output);
    }
}
