package com.github.ysbbbbbb.kaleidoscopedoll.client.render;

import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.BedrockModel;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.client.resources.CustomDollReloadListener;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 自定义玩偶物品模型：方块式斜视 + 物品栏自动适配。
 * <p>
 * 结构参照 {@code SpecialModelWrapper}：base 模型提供各显示上下文的 display
 * （斜视旋转、手持/地面等），特殊渲染器负责绘制 bedrock 网格。
 * 与普通 wrapper 不同的是，物品栏（GUI）下会按每个玩偶的实际渲染包围盒
 * 自动缩放并居中：最长轴缩放到 1 格（16 模型单位），避免海螺这类深模型过大、
 * 小酒狐这类矮模型过小；其它上下文保持自然尺寸，不做自动缩放。
 */
public class CustomDollItemModel implements ItemModel {

    private final SpecialModelRenderer<ItemStack> specialRenderer;
    private final ModelRenderProperties properties;
    private final Supplier<Vector3fc[]> extents;

    public CustomDollItemModel(SpecialModelRenderer<ItemStack> specialRenderer, ModelRenderProperties properties) {
        this.specialRenderer = specialRenderer;
        this.properties = properties;
        this.extents = Suppliers.memoize(() -> {
            Set<Vector3fc> set = new HashSet<>();
            specialRenderer.getExtents(set::add);
            return set.toArray(Vector3fc[]::new);
        });
    }

    public record Unbaked(Identifier base, SpecialModelRenderer.Unbaked<?> specialModel) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base),
                SpecialModelRenderers.CODEC.fieldOf("model").forGetter(Unbaked::specialModel)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc rootTransform) {
            SpecialModelRenderer<?> renderer = specialModel.bake(context);
            if (renderer == null) {
                return context.missingItemModel(rootTransform);
            }
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel resolved = baker.getModel(base);
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(
                    baker, resolved, resolved.getTopTextureSlots());
            return new CustomDollItemModel((SpecialModelRenderer<ItemStack>) renderer, properties);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(base);
        }
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, ClientLevel level, ItemOwner owner, int seed) {
        state.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layer = state.newLayer();

        if (stack.hasFoil()) {
            ItemStackRenderState.FoilType foilType = ItemStackRenderState.FoilType.STANDARD;
            layer.setFoilType(foilType);
            state.setAnimated();
            state.appendModelIdentityElement(foilType);
        }

        ItemStack argument = specialRenderer.extractArgument(stack);
        layer.setExtents(extents);
        layer.setLocalTransform(guiTransform(stack, displayContext));
        layer.setupSpecialModel(specialRenderer, argument);
        if (argument != null) {
            state.appendModelIdentityElement(argument);
        }

        // base 模型的 display 负责各上下文的旋转/平移/缩放（物品栏斜视等）
        properties.applyToLayer(layer, displayContext);
    }

    /**
     * 物品栏上下文：T(c)·S(s)·T(-c)，按玩偶实际包围盒把最长轴缩放到 16 模型单位（1 格），
     * 并以 c 为中心。矩阵作用于 DollSpecialModelRenderer 平移/翻转之后的渲染空间
     * （y 向上、脚底在 y=0），因此 x/z 取包围盒在该空间的中心（≈0.5），
     * y 取缩放后高度的一半，保证玩偶像方块玩偶一样站在物品原点。
     * 其它上下文返回恒等变换，保持自然尺寸。
     */
    private Matrix4f guiTransform(ItemStack stack, ItemDisplayContext displayContext) {
        Matrix4f transform = new Matrix4f();
        if (displayContext != ItemDisplayContext.GUI) {
            return transform;
        }

        BedrockModel model = resolveModel(stack);
        if (model == null) {
            return transform;
        }

        AABB box = model.getActualRenderBoundingBox();
        float width = (float) box.getXsize();
        float height = (float) box.getYsize();
        float depth = (float) box.getZsize();
        float max = Math.max(width, Math.max(height, depth));
        if (max <= 1.0E-4F) {
            return transform;
        }

        float scale = 16.0F / max;
        float centerX = 0.5F + (float) ((box.minX + box.maxX) / 2.0 / 16.0);
        float centerZ = 0.5F - (float) ((box.minZ + box.maxZ) / 2.0 / 16.0);
        float centerY = height * scale / 32.0F;
        return transform.translation(centerX, centerY, centerZ)
                .scale(scale)
                .translate(-centerX, -centerY, -centerZ);
    }

    private BedrockModel resolveModel(ItemStack stack) {
        String modelId = CustomDollItem.getModelId(stack);
        BedrockModel model = modelId.isEmpty() ? null : CustomDollLoader.getModel(modelId);
        if (model == null) {
            model = CustomDollReloadListener.DFAULT_DOLL_MODEL;
        }
        return model;
    }
}
