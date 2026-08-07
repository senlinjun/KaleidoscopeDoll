package com.github.ysbbbbbb.kaleidoscopedoll.client.render;

import com.github.ysbbbbbb.kaleidoscopedoll.client.bedrock.BedrockModel;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.client.resources.CustomDollReloadListener;
import com.github.ysbbbbbb.kaleidoscopedoll.entity.DollEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class DollEntityRender extends EntityRenderer<DollEntity, DollEntityRenderState> {
    private final BlockModelResolver blockModelResolver;
    private final ItemModelResolver itemModelResolver;

    public DollEntityRender(EntityRendererProvider.Context context) {
        super(context);
        this.blockModelResolver = context.getBlockModelResolver();
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public DollEntityRenderState createRenderState() {
        return new DollEntityRenderState();
    }

    @Override
    public void extractRenderState(DollEntity dollEntity, DollEntityRenderState state, float partialTick) {
        super.extractRenderState(dollEntity, state, partialTick);
        state.displayBlockState = dollEntity.getDisplayBlockState();
        state.customDollId = dollEntity.getCustomDollId();
        this.itemModelResolver.updateForNonLiving(state.holdItemRenderState, dollEntity.getHoldItem(), ItemDisplayContext.GROUND, dollEntity);
        state.displayScale = dollEntity.getDisplayScale();
        state.displayTranslation = dollEntity.getDisplayTranslation();
        state.itemScale = dollEntity.getItemScale();
        state.itemTranslation = dollEntity.getItemTranslation();
        state.itemRotation = dollEntity.getItemRotation();
        state.bounceTime = dollEntity.getBounceTime();

        Entity vehicle = dollEntity.getVehicle();
        float yaw;
        if (vehicle != null) {
            yaw = Mth.lerp(partialTick, vehicle.yRotO, vehicle.getYRot());
        } else {
            yaw = Mth.lerp(partialTick, dollEntity.yRotO, dollEntity.getYRot());
        }
        if (dollEntity.getCustomDollId().isEmpty()) {
            // 普通方块玩偶：渲染烘焙方块模型，其正面朝 -Z（原版实体约定），使用 180 - yaw。
            state.yRot = 180.0F - yaw;
        } else {
            // 自定义玩偶：模型读取阶段已在 BedrockModel.render 内统一翻转 Y/Z，
            // 模型正面朝 +Z，因此直接用 -yaw 让玩偶朝向玩家。
            state.yRot = -yaw;
        }
        state.xRot = Mth.lerp(partialTick, dollEntity.xRotO, dollEntity.getXRot());
    }

    @Override
    public void submit(DollEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        boolean hasBody = !state.customDollId.isEmpty() || (state.displayBlockState != null && !state.displayBlockState.isAir());
        boolean hasHoldItem = !state.holdItemRenderState.isEmpty();

        if (!hasBody && !hasHoldItem) {
            return;
        }

        poseStack.pushPose();

        // 应用位移变换
        Vector3fc translation = state.displayTranslation;
        poseStack.translate(translation.x(), translation.y(), translation.z());

        // 应用旋转
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot));

        // 应用缩放变换
        Vector3fc scale = state.displayScale;
        poseStack.scale(scale.x(), scale.y(), scale.z());

        // 如果是弹跳时间，那么应用弹跳效果
        long time = state.bounceTime - System.currentTimeMillis();
        if (time > 0) {
            float dampingFactor = 0.6f;
            float frequency = 6f;
            float bounceStrength = 0.6f;

            float bounceProgress = 1.0f - ((float) time / 500.0f);
            float dampedAmplitude = (float) Math.exp(-dampingFactor * bounceProgress * 8.0f);
            float compressionIntensity = dampedAmplitude * bounceStrength;

            float groundContact = (float) Math.max(0, -Math.sin(bounceProgress * frequency * Math.PI));
            float scaleY = 1.0f - groundContact * compressionIntensity;
            float scaleXZ = 1.0f + groundContact * compressionIntensity;

            poseStack.scale(scaleXZ, scaleY, scaleXZ);
        }

        // 将方块中心对齐到实体位置
        poseStack.translate(-0.5, 0, -0.5);

        // 渲染逻辑
        if (!state.customDollId.isEmpty()) {
            renderCustom(state.customDollId, poseStack, collector);
        } else if (state.displayBlockState != null && !state.displayBlockState.isAir()) {
            renderBlock(state.displayBlockState, poseStack, collector);
        }

        renderHoldItem(state, poseStack, collector);

        poseStack.popPose();
    }

    private void renderHoldItem(DollEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        if (state.holdItemRenderState.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        Vector3fc itemTranslation = state.itemTranslation;
        poseStack.translate(itemTranslation.x(), itemTranslation.y(), itemTranslation.z());

        Vector3fc itemScale = state.itemScale;
        poseStack.scale(itemScale.x(), itemScale.y(), itemScale.z());

        Vector3fc itemRotation = state.itemRotation;
        poseStack.mulPose(Axis.XP.rotationDegrees(itemRotation.x()));
        poseStack.mulPose(Axis.YP.rotationDegrees(itemRotation.y()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(itemRotation.z()));

        state.holdItemRenderState.submit(poseStack, collector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    private void renderCustom(String modelId, PoseStack poseStack, SubmitNodeCollector collector) {
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

        poseStack.translate(0.5, 1.5, 0.5);

        BedrockModel finalModel = model;
        Identifier finalTexture = texture;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(finalTexture), (pose, consumer) -> {
            poseStack.last().pose().set(pose.pose());
            poseStack.last().normal().set(pose.normal());
            finalModel.render(poseStack, consumer, 0xF000F0, OverlayTexture.NO_OVERLAY);
        });
    }

    private void renderBlock(BlockState blockState, PoseStack poseStack, SubmitNodeCollector collector) {
        MovingBlockRenderState movingBlockState = new MovingBlockRenderState();
        movingBlockState.blockState = blockState;
        movingBlockState.blockPos = net.minecraft.core.BlockPos.ZERO;
        movingBlockState.randomSeedPos = net.minecraft.core.BlockPos.ZERO;
        if (net.minecraft.client.Minecraft.getInstance().level instanceof ClientLevel clientLevel) {
            movingBlockState.biome = clientLevel.getBiome(movingBlockState.blockPos);
            movingBlockState.cardinalLighting = clientLevel.cardinalLighting();
            movingBlockState.lightEngine = clientLevel.getLightEngine();
        }
        collector.submitMovingBlock(poseStack, movingBlockState, 0);
    }
}
