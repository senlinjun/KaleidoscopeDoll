package com.github.ysbbbbbb.kaleidoscopedoll.datagen;

import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.concurrent.CompletableFuture;

public class LootTableGenerator {
    public static class BlockLootTables extends FabricBlockLootSubProvider {
        public BlockLootTables(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(output, registryLookup);
        }

        @Override
        public void generate() {
            ModRegisterEvent.DOLL_BLOCKS.values().forEach(this::dropSelf);

            LootTable.Builder table = createSinglePropConditionTable(ModBlocks.DOLL_MACHINE, BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
            this.add(ModBlocks.DOLL_MACHINE, table);

            this.dropSelf(ModBlocks.COMPUTER);

            // 礼物盒使用自定义方块实体掉落，不走数据表
            this.excludeFromStrictValidation(ModBlocks.PURPLE_DOLL_GIFT_BOX);
            this.excludeFromStrictValidation(ModBlocks.GREEN_DOLL_GIFT_BOX);
            this.excludeFromStrictValidation(ModBlocks.YELLOW_DOLL_GIFT_BOX);
            this.excludeFromStrictValidation(ModBlocks.CUSTOM_DOLL);
        }
    }
}
