package com.github.ysbbbbbb.kaleidoscopedoll.item;

import com.github.ysbbbbbb.kaleidoscopedoll.ClientHooks;
import com.github.ysbbbbbb.kaleidoscopedoll.block.entity.CustomDollBlockEntity;
import com.github.ysbbbbbb.kaleidoscopedoll.data.custom.ServerCustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModDataComponent;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CustomDollItem extends BlockItem {
    public CustomDollItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    public static String getModelId(ItemStack doll) {
        return doll.getOrDefault(ModDataComponent.CUSTOM_DOLL_MODEL_ID, "");
    }

    public static void setModelId(ItemStack doll, String modelId) {
        doll.set(ModDataComponent.CUSTOM_DOLL_MODEL_ID, modelId);
    }

    public static void addCreativeTab(CreativeModeTab.Output output) {
        output.accept(new ItemStack(ModItems.CUSTOM_DOLL));
        ServerCustomDollLoader.getModels().forEach(modelId -> {
            ItemStack dollStack = new ItemStack(ModItems.CUSTOM_DOLL);
            setModelId(dollStack, modelId);
            output.accept(dollStack);
        });
    }

    public static ItemStack getDefaultItemStack() {
        ItemStack dollStack = new ItemStack(ModItems.CUSTOM_DOLL);
        ServerCustomDollLoader.getModels().stream().findFirst().ifPresent(modelId -> {
            setModelId(dollStack, modelId);
        });
        return dollStack;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!result) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CustomDollBlockEntity customDollBlockEntity) {
                String modelId = getModelId(stack);
                if (!modelId.isEmpty()) {
                    customDollBlockEntity.setModelId(modelId);
                }
            }
        }
        return result;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("block.kaleidoscope_doll.doll");
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag flag) {
        String modelId = getModelId(stack);
        if (!modelId.isEmpty()) {
            ClientHooks.CUSTOM_DOLL_TOOLTIP.apply(stack).forEach(consumer);
        } else {
            consumer.accept(Component.translatable("tooltip.kaleidoscope_doll.custom.unknown").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
