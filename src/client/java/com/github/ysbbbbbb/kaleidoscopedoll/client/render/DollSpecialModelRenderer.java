package com.github.ysbbbbbb.kaleidoscopedoll.client.render;

import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.BedrockModel;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.client.resources.CustomDollReloadListener;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * 自定义玩偶物品的动态渲染（等价 forge 版 BEWLR 直接绘制 bedrock 网格）。
 * <p>
 * 在提交阶段通过 submitCustomGeometry 直接绘制 BedrockModel 网格，
 * 贴图使用运行时注册的自定义贴图 Identifier。
 */
public class DollSpecialModelRenderer implements SpecialModelRenderer<ItemStack> {

    public static final class Unbaked implements SpecialModelRenderer.Unbaked<ItemStack> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        public Unbaked() {
        }

        @Override
        public SpecialModelRenderer<ItemStack> bake(BakingContext context) {
            return new DollSpecialModelRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<ItemStack>> type() {
            return MAP_CODEC;
        }
    }

    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack;
    }

    @Override
    public void submit(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean bl, int seed) {
        String modelId = CustomDollItem.getModelId(stack);

        BedrockModel model;
        Identifier texture;

        if (modelId.isEmpty()) {
            model = CustomDollReloadListener.DFAULT_DOLL_MODEL;
            texture = CustomDollReloadListener.DEFAULT_TEXTURE_ID;
        } else {
            model = CustomDollLoader.getModel(modelId);
            if (model == null) {
                model = CustomDollReloadListener.DFAULT_DOLL_MODEL;
                texture = CustomDollReloadListener.DEFAULT_TEXTURE_ID;
            } else {
                texture = CustomDollLoader.getTexture(modelId);
                if (texture == null) {
                    texture = MissingTextureAtlasSprite.getLocation();
                }
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);

        BedrockModel finalModel = model;
        Identifier finalTexture = texture;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(finalTexture), (pose, consumer) -> {
            poseStack.last().pose().set(pose.pose());
            poseStack.last().normal().set(pose.normal());
            finalModel.render(poseStack, consumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
        });

        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
    }
}
