package com.github.ysbbbbbb.kaleidoscopedoll.datagen;

import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent.DOLL_ITEMS;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Mod Recipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                List<Item> dollItems = Lists.newArrayList(DOLL_ITEMS);
                // 原版似乎有 bug，切石机配方数量超过一定值，会不能合成
                // 所以每 64 个配方分一组
                int count = 0;
                for (int i = 0; i < dollItems.size(); i++) {
                    if (2 <= i && i <= 66) {
                        continue;
                    }
                    Item doll = dollItems.get(i);
                    Item wool = getWoolFromIndex(count / 64);
                    stonecutterResultFromBase(RecipeCategory.DECORATIONS, doll, wool, 1);
                    count++;
                }

                ShapedRecipeBuilder.shaped(registries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM), RecipeCategory.REDSTONE, ModItems.COMPUTER)
                        .pattern("III")
                        .pattern("IRI")
                        .pattern("SSS")
                        .define('I', Items.IRON_INGOT)
                        .define('R', Items.REDSTONE)
                        .define('S', Items.SMOOTH_STONE)
                        .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                        .save(output);

                ShapedRecipeBuilder.shaped(registries.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM), RecipeCategory.TOOLS, ModItems.TWEAKS_TOOL)
                        .pattern(" W ")
                        .pattern(" W ")
                        .pattern(" S ")
                        .define('W', ItemTags.WOOL)
                        .define('S', Items.STICK)
                        .unlockedBy("has_wool", has(ItemTags.WOOL))
                        .save(output);
            }

            private Item getWoolFromIndex(int number) {
                return switch (number) {
                    case 0 -> Items.WOOL.white();
                    case 1 -> Items.WOOL.orange();
                    case 2 -> Items.WOOL.magenta();
                    case 3 -> Items.WOOL.lightBlue();
                    case 4 -> Items.WOOL.yellow();
                    case 5 -> Items.WOOL.lime();
                    case 6 -> Items.WOOL.pink();
                    case 7 -> Items.WOOL.gray();
                    case 8 -> Items.WOOL.lightGray();
                    case 9 -> Items.WOOL.cyan();
                    case 10 -> Items.WOOL.purple();
                    case 11 -> Items.WOOL.blue();
                    case 12 -> Items.WOOL.brown();
                    case 13 -> Items.WOOL.green();
                    case 14 -> Items.WOOL.red();
                    case 15 -> Items.WOOL.black();
                    default -> throw new IllegalArgumentException("Invalid wool index: " + number);
                };
            }
        };
    }
}
