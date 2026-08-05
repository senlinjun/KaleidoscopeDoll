package com.github.ysbbbbbb.kaleidoscopedoll.item.crafting;

import com.github.ysbbbbbb.kaleidoscopedoll.datagen.TagItem;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollEntityItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class DollEntityCraftingRecipe extends CustomRecipe {
    public static final MapCodec<DollEntityCraftingRecipe> MAP_CODEC = CraftingBookCategory.CODEC
            .fieldOf("category")
            .xmap(DollEntityCraftingRecipe::new, DollEntityCraftingRecipe::category);

    public static final StreamCodec<RegistryFriendlyByteBuf, DollEntityCraftingRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> CraftingBookCategory.STREAM_CODEC.encode(buf, recipe.category()),
            buf -> new DollEntityCraftingRecipe(CraftingBookCategory.STREAM_CODEC.decode(buf)));

    public static final RecipeSerializer<DollEntityCraftingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public DollEntityCraftingRecipe(CraftingBookCategory category) {
        super();
        this.category = category;
    }

    private final CraftingBookCategory category;

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasDollItem = false;
        boolean hasBlockToEntityItem = false;
        int itemCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() instanceof DollItem) {
                    hasDollItem = true;
                } else if (stack.is(ModItems.CUSTOM_DOLL)) {
                    String modelId = CustomDollItem.getModelId(stack);
                    hasDollItem = !modelId.isEmpty();
                } else if (stack.is(TagItem.BLOCK_DOLLS_TO_ENTITY_ITEM)) {
                    hasBlockToEntityItem = true;
                } else {
                    return false;
                }
            }
        }

        return itemCount == 2 && hasDollItem && hasBlockToEntityItem;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack dollItemStack = ItemStack.EMPTY;
        boolean isCustomDoll = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof DollItem) {
                dollItemStack = stack;
                break;
            } else if (stack.is(ModItems.CUSTOM_DOLL)) {
                String modelId = CustomDollItem.getModelId(stack);
                if (!modelId.isEmpty()) {
                    dollItemStack = stack;
                    isCustomDoll = true;
                    break;
                }
            }
        }

        if (dollItemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (isCustomDoll) {
            String modelId = CustomDollItem.getModelId(dollItemStack);
            return DollEntityItem.createItemWithCustomDollId(modelId);
        } else {
            DollItem dollItem = (DollItem) dollItemStack.getItem();
            return DollEntityItem.createItemWithBlockState(dollItem.getBlock().defaultBlockState());
        }
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.DOLL_ENTITY_CRAFTING;
    }
}
