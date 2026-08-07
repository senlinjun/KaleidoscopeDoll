package com.github.ysbbbbbb.kaleidoscopedoll.datagen;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopedoll.client.render.DollEntityItemModel;
import com.github.ysbbbbbb.kaleidoscopedoll.client.render.DollSpecialModelRenderer;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Optional;

public class BlockStateGenerator extends FabricModelProvider {
    public BlockStateGenerator(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generator) {
        ModRegisterEvent.DOLL_BLOCKS.values().forEach(block -> {
            Identifier key = BuiltInRegistries.BLOCK.getKey(block);
            PropertyDispatch<VariantMutator> dispatch = PropertyDispatch.modify(BlockStateProperties.ROTATION_16)
                    .generate(rotation -> VariantMutator.MODEL.withValue(
                            modLoc("block/rot_doll/" + key.getPath() + "/rot_" + rotation)));
            generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,
                    new MultiVariant(WeightedList.of(new Variant(modLoc("block/doll/" + key.getPath())))))
                    .with(dispatch));
        });

        PropertyDispatch<VariantMutator> facingDispatch = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                .generate(facing -> switch (facing) {
                    case EAST -> BlockModelGenerators.Y_ROT_90;
                    case SOUTH -> BlockModelGenerators.Y_ROT_180;
                    case WEST -> BlockModelGenerators.Y_ROT_270;
                    default -> BlockModelGenerators.NOP;
                });
        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.COMPUTER,
                new MultiVariant(WeightedList.of(new Variant(modLoc("block/computer")))))
                .with(facingDispatch));

        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.CUSTOM_DOLL,
                new MultiVariant(WeightedList.of(new Variant(modLoc("block/custom_doll"))))));
    }

    @Override
    public void generateItemModels(ItemModelGenerators generator) {
        ModRegisterEvent.DOLL_ITEMS.forEach(item -> {
            Identifier key = BuiltInRegistries.ITEM.getKey(item);
            generator.itemModelOutput.accept(item, new CuboidItemModelWrapper.Unbaked(
                    modLoc("block/doll/" + key.getPath()), Optional.empty(), List.of()));
        });

        generator.itemModelOutput.accept(ModItems.COMPUTER, new CuboidItemModelWrapper.Unbaked(
                modLoc("block/computer"), Optional.empty(), List.of()));

        generator.itemModelOutput.accept(ModItems.DOLL_MACHINE, new CuboidItemModelWrapper.Unbaked(
                modLoc("item/doll_machine"), Optional.empty(), List.of()));

        generator.itemModelOutput.accept(ModItems.TWEAKS_TOOL, new CuboidItemModelWrapper.Unbaked(
                modLoc("item/tweaks_tool"), Optional.empty(), List.of()));

        generator.itemModelOutput.accept(ModItems.PURPLE_DOLL_GIFT_BOX, new CuboidItemModelWrapper.Unbaked(
                modLoc("item/purple_doll_gift_box"), Optional.empty(), List.of()));

        generator.itemModelOutput.accept(ModItems.GREEN_DOLL_GIFT_BOX, new CuboidItemModelWrapper.Unbaked(
                modLoc("item/green_doll_gift_box"), Optional.empty(), List.of()));

        generator.itemModelOutput.accept(ModItems.YELLOW_DOLL_GIFT_BOX, new CuboidItemModelWrapper.Unbaked(
                modLoc("item/yellow_doll_gift_box"), Optional.empty(), List.of()));

        generator.itemModelOutput.accept(ModItems.DOLL_ENTITY_ITEM, new DollEntityItemModel.Unbaked());

        generator.itemModelOutput.accept(ModItems.CUSTOM_DOLL, new SpecialModelWrapper.Unbaked(
                Identifier.withDefaultNamespace("item/generated"), Optional.empty(),
                new DollSpecialModelRenderer.Unbaked()));
    }

    private Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, path);
    }
}
