package com.github.ysbbbbbb.kaleidoscopedoll.compat.jei;

import com.github.ysbbbbbb.kaleidoscopedoll.item.DollEntityItem;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class EntityDollSubtype implements ISubtypeInterpreter<ItemStack> {
    @Override
    public String getSubtypeData(ItemStack itemStack, UidContext context) {
        // 自定义玩偶
        String dollId = DollEntityItem.getCustomDollIdFromItemStack(itemStack);
        if (!dollId.isEmpty()) {
            return dollId;
        }

        // 普通玩偶
        Block block = DollEntityItem.getBlockFromItemStack(itemStack);
        Identifier key = BuiltInRegistries.BLOCK.getKey(block);
        if (key == null) {
            return "";
        }
        return key.toString();
    }
}
