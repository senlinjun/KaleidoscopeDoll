package com.github.ysbbbbbb.kaleidoscopedoll.init;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.block.ComputerBlock;
import com.github.ysbbbbbb.kaleidoscopedoll.block.CustomDollBlock;
import com.github.ysbbbbbb.kaleidoscopedoll.block.DollGiftBoxBlock;
import com.github.ysbbbbbb.kaleidoscopedoll.block.DollMachineBlock;
import com.github.ysbbbbbb.kaleidoscopedoll.block.entity.CustomDollBlockEntity;
import com.github.ysbbbbbb.kaleidoscopedoll.block.entity.DollGiftBoxBlockEntiy;
import com.github.ysbbbbbb.kaleidoscopedoll.block.entity.DollMachineBlockEntity;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class ModBlocks {
    public static final Block DOLL_MACHINE = new DollMachineBlock(props("doll_machine")
            .instrument(NoteBlockInstrument.BASS)
            .sound(SoundType.COPPER)
            .strength(8f, 10f)
            .lightLevel(s -> 2)
            .pushReaction(PushReaction.BLOCK)
            .noOcclusion());
    public static final Block PURPLE_DOLL_GIFT_BOX = new DollGiftBoxBlock(props("purple_doll_gift_box")
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.WOOL)
            .strength(0f, 10f)
            .noOcclusion());
    public static final Block GREEN_DOLL_GIFT_BOX = new DollGiftBoxBlock(props("green_doll_gift_box")
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.WOOL)
            .strength(0f, 10f)
            .noOcclusion());
    public static final Block YELLOW_DOLL_GIFT_BOX = new DollGiftBoxBlock(props("yellow_doll_gift_box")
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.WOOL)
            .strength(0f, 10f)
            .noOcclusion());
    public static final Block COMPUTER = new ComputerBlock(props("computer")
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.METAL)
            .strength(1.25F, 4.2F)
            .noOcclusion());
    public static final Block CUSTOM_DOLL = new CustomDollBlock(props("custom_doll")
            .instrument(NoteBlockInstrument.BASEDRUM)
            .sound(SoundType.WOOL)
            .strength(0f, 10f)
            .noOcclusion());

    private static BlockBehaviour.Properties props(String path) {
        return BlockBehaviour.Properties.of().setId(
                ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, path)));
    }

    public static final BlockEntityType<DollGiftBoxBlockEntiy> DOLL_GIFT_BOX_BE = new BlockEntityType<>(
            DollGiftBoxBlockEntiy::new, Set.of(PURPLE_DOLL_GIFT_BOX, GREEN_DOLL_GIFT_BOX, YELLOW_DOLL_GIFT_BOX));

    public static final BlockEntityType<DollMachineBlockEntity> DOLL_MACHINE_BE = new BlockEntityType<>(
            DollMachineBlockEntity::new, Set.of(DOLL_MACHINE));

    public static final BlockEntityType<CustomDollBlockEntity> CUSTOM_DOLL_BE = new BlockEntityType<>(
            CustomDollBlockEntity::new, Set.of(CUSTOM_DOLL));

    public static void registerBlocks() {
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "doll_machine"), DOLL_MACHINE);
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "purple_doll_gift_box"), PURPLE_DOLL_GIFT_BOX);
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "green_doll_gift_box"), GREEN_DOLL_GIFT_BOX);
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "yellow_doll_gift_box"), YELLOW_DOLL_GIFT_BOX);
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "computer"), COMPUTER);
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "custom_doll"), CUSTOM_DOLL);
        ModRegisterEvent.registerBlocks();
    }

    public static void registerBlockEntities() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "doll_gift_box"), DOLL_GIFT_BOX_BE);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "doll_machine"), DOLL_MACHINE_BE);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "custom_doll"), CUSTOM_DOLL_BE);
    }
}
