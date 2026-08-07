package com.github.ysbbbbbb.kaleidoscopedoll.client.render;

import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollEntityItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.joml.Matrix4fc;

/**
 * 实体玩偶物品的动态渲染。
 * <p>
 * 根据物品中保存的玩偶数据，把对应的玩偶方块物品（或自定义玩偶物品）的
 * 模型层附加到渲染状态中，实现物品栏/手持图标随玩偶内容变化。
 */
public class DollEntityItemModel implements ItemModel {

    public static final class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        public Unbaked() {
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc rootTransform) {
            return new DollEntityItemModel();
        }

        @Override
        public void resolveDependencies(net.minecraft.client.resources.model.ResolvableModel.Resolver resolver) {
        }
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, ClientLevel level, ItemOwner owner, int seed) {
        ItemStack target;
        String customDollId = DollEntityItem.getCustomDollIdFromItemStack(stack);
        if (!customDollId.isEmpty()) {
            // 自定义玩偶：委托给 custom_doll 的渲染模型
            target = new ItemStack(ModItems.CUSTOM_DOLL);
            CustomDollItem.setModelId(target, customDollId);
        } else {
            Block block = DollEntityItem.getBlockFromItemStack(stack);
            target = new ItemStack(block.asItem());
        }
        resolver.appendItemLayers(state, target, displayContext, level, owner, seed);
    }
}
