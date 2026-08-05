package com.github.ysbbbbbb.kaleidoscopedoll.block.entity;

import com.github.ysbbbbbb.kaleidoscopedoll.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DollGiftBoxBlockEntiy extends BlockEntity {
    private static final String NBT_DOLL_ITEMSTACK = "DollItemStack";

    private ItemStack dollItemStack = ItemStack.EMPTY;

    public DollGiftBoxBlockEntiy(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    public DollGiftBoxBlockEntiy(BlockPos pos, BlockState state) {
        this(ModBlocks.DOLL_GIFT_BOX_BE, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!dollItemStack.isEmpty()) {
            output.store(NBT_DOLL_ITEMSTACK, ItemStack.CODEC, dollItemStack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        dollItemStack = input.read(NBT_DOLL_ITEMSTACK, ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    public ItemStack getDollItemStack() {
        return dollItemStack.copyWithCount(1);
    }
}
