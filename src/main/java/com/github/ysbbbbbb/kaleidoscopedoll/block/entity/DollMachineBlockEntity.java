package com.github.ysbbbbbb.kaleidoscopedoll.block.entity;

import com.github.ysbbbbbb.kaleidoscopedoll.datagen.TagItem;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModBlocks;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Collections;
import java.util.List;

public class DollMachineBlockEntity extends BlockEntity {
    private static final String NBT_KEY_TIER = "Tier";
    private static final String NBT_KEY_AVAILABLE_DOLLS = "AvailableDolls";
    private static final Codec<List<Identifier>> DOLLS_CODEC = Codec.list(Identifier.CODEC);

    private Int2ObjectMap<List<Identifier>> availableDolls = new Int2ObjectOpenHashMap<>();
    private int tier = 0;

    public DollMachineBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    public DollMachineBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlocks.DOLL_MACHINE_BE, pos, state);
    }

    public void onTokensClicked(ItemStack itemStack, Level level) {
        if (itemStack.is(TagItem.TIER_0_MACHINE_TOKENS)) {
            tier = 0;
        } else if (itemStack.is(TagItem.TIER_1_MACHINE_TOKENS)) {
            tier = 1;
        } else if (itemStack.is(TagItem.TIER_2_MACHINE_TOKENS)) {
            tier = 2;
        }
        List<Identifier> list = availableDolls.getOrDefault(tier, Lists.newArrayList());
        if (list.isEmpty()) {
            if (tier == 0) {
                shuffleDolls(TagItem.TIER_0_DOLLS, list);
            } else if (tier == 1) {
                shuffleDolls(TagItem.TIER_1_DOLLS, list);
            } else if (tier == 2) {
                shuffleDolls(TagItem.TIER_2_DOLLS, list);
            }
        }
        itemStack.shrink(1);
        this.setChanged();
    }

    public ItemStack onFinishLottery() {
        ItemStack output = ItemStack.EMPTY;
        List<Identifier> list = availableDolls.getOrDefault(tier, Lists.newArrayList());
        if (!list.isEmpty()) {
            // 取出第一个
            Identifier doll = list.remove(0);
            Item item = BuiltInRegistries.ITEM.getValue(doll);
            if (item != null) {
                output = new ItemStack(item);
            }
            availableDolls.put(tier, list);
            this.setChanged();
        }
        return output;
    }

    private void shuffleDolls(TagKey<Item> tagKey, List<Identifier> list) {
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
            Identifier key = BuiltInRegistries.ITEM.getKey(holder.value());
            if (key != null) {
                list.add(key);
            }
        }
        Collections.shuffle(list);
        availableDolls.put(tier, list);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(NBT_KEY_TIER, tier);

        for (int i = 0; i < availableDolls.size(); i++) {
            List<Identifier> dolls = availableDolls.getOrDefault(i, Lists.newArrayList());
            if (!dolls.isEmpty()) {
                output.store(NBT_KEY_AVAILABLE_DOLLS + "_" + i, DOLLS_CODEC, dolls);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.tier = input.getIntOr(NBT_KEY_TIER, 0);

        availableDolls.clear();
        for (int i = 0; i < 3; i++) {
            List<Identifier> dolls = input.read(NBT_KEY_AVAILABLE_DOLLS + "_" + i, DOLLS_CODEC).orElse(Lists.newArrayList());
            if (!dolls.isEmpty()) {
                availableDolls.put(i, dolls);
            }
        }
    }

    public int getTier() {
        return tier;
    }
}
