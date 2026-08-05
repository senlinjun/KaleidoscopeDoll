package com.github.ysbbbbbb.kaleidoscopedoll.compat.jei;

import com.github.ysbbbbbb.kaleidoscopedoll.data.custom.ServerCustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.datagen.TagItem;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollEntityItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollItem;
import com.github.ysbbbbbb.kaleidoscopedoll.utils.Md5Utils;
import com.google.common.collect.Lists;
import mezz.jei.api.constants.ModIds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class EntityDollRecipeMaker {
    private static final String GROUP = "jei.doll.entity";

    public static List<RecipeHolder<CraftingRecipe>> createRecipes() {
        List<RecipeHolder<CraftingRecipe>> recipes = Lists.newArrayList();

        List<Ingredient> blockToEntityIngredients = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TagItem.BLOCK_DOLLS_TO_ENTITY_ITEM)) {
            blockToEntityIngredients.add(Ingredient.of(holder.value()));
        }
        Ingredient blockToEntityItem = blockToEntityIngredients.get(0);

        // 普通玩偶
        ModRegisterEvent.DOLL_ITEMS.forEach(item -> {
            Identifier key = BuiltInRegistries.ITEM.getKey(item);
            if (!(item instanceof DollItem dollItem)) {
                return;
            }

            Ingredient inputDoll = Ingredient.of(item);
            List<Ingredient> inputs = List.of(inputDoll, blockToEntityItem);

            BlockState dollState = dollItem.getBlock().defaultBlockState();
            ItemStack output = DollEntityItem.createItemWithBlockState(dollState);

            Identifier recipeId = Identifier.fromNamespaceAndPath(ModIds.MINECRAFT_ID, GROUP + "." + key.getPath());
            ShapelessRecipe recipe = new ShapelessRecipe(
                    new Recipe.CommonInfo(true),
                    new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, GROUP),
                    ItemStackTemplate.fromNonEmptyStack(output),
                    inputs);

            recipes.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeId), recipe));
        });

        // 自定义玩偶
        ServerCustomDollLoader.getModels().forEach(id -> {
            ItemStack stack = ModItems.CUSTOM_DOLL.getDefaultInstance();
            CustomDollItem.setModelId(stack, id);

            Ingredient inputDoll = Ingredient.of(ModItems.CUSTOM_DOLL);
            List<Ingredient> inputs = List.of(inputDoll, blockToEntityItem);

            ItemStack output = DollEntityItem.createItemWithCustomDollId(id);

            // 将 id 转换为 md5 字符串
            String hashId = Md5Utils.md5Hex(id);
            Identifier recipeId = Identifier.fromNamespaceAndPath(ModIds.MINECRAFT_ID, GROUP + ".custom." + hashId);
            ShapelessRecipe recipe = new ShapelessRecipe(
                    new Recipe.CommonInfo(true),
                    new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, GROUP),
                    ItemStackTemplate.fromNonEmptyStack(output),
                    inputs);

            recipes.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeId), recipe));
        });

        return recipes;
    }

    private EntityDollRecipeMaker() {
    }
}
