package com.github.ysbbbbbb.kaleidoscopedoll.client.render;

import com.github.ysbbbbbb.kaleidoscopedoll.block.entity.CustomDollBlockEntity;
import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.BedrockModel;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.client.resources.CustomDollReloadListener;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CustomDollRender implements BlockEntityRenderer<CustomDollBlockEntity, CustomDollRenderState> {
    public CustomDollRender(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context) {
    }

    @Override
    public CustomDollRenderState createRenderState() {
        return new CustomDollRenderState();
    }

    @Override
    public void extractRenderState(CustomDollBlockEntity blockEntity, CustomDollRenderState state, float partialTick,
                                   net.minecraft.world.phys.Vec3 pos, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, pos, crumblingOverlay);
        state.modelId = blockEntity.getModelId();
        state.rotation = blockEntity.getBlockState().getValue(BlockStateProperties.ROTATION_16);
    }

    @Override
    public void submit(CustomDollRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        String modelId = state.modelId;

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

        // 与烘焙 rot_N 方块模型及实体玩偶保持一致：整体补 180° 后，
        // 任意朝向放置时玩偶正脸都朝向放置者。
        float angle = state.rotation * 22.5f - 180;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(angle));

        BedrockModel finalModel = model;
        Identifier finalTexture = texture;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(finalTexture), (pose, consumer) -> {
            poseStack.last().pose().set(pose.pose());
            poseStack.last().normal().set(pose.normal());
            finalModel.render(poseStack, consumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
        });

        poseStack.popPose();
    }
}
