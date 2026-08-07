package com.github.ysbbbbbb.kaleidoscopedoll.event;

import com.github.ysbbbbbb.kaleidoscopedoll.config.GeneralConfig;
import com.github.ysbbbbbb.kaleidoscopedoll.entity.DollEntity;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopedoll.item.DollEntityItem;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class DollClickEntityEvent {
    private static final int MAX_RIDING_LAYERS = 5;

    public static void register() {
        UseEntityCallback.EVENT.register(DollClickEntityEvent::onUseEntity);
    }

    private static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        ItemStack mainHandItem = player.getMainHandItem();

        if (mainHandItem.is(ModItems.DOLL_ENTITY_ITEM) && GeneralConfig.DOLL_CAN_MOUNT_ENTITIES.get()) {
            // 客户端只负责把交互包发给服务端并阻止原版交互，绝不创建实体。
            // 若在客户端创建并骑乘一个未加入世界的实体（id 为 0），下一 tick 会因
            // EntityTickList.contains -> getId() 抛 "Tried to access entity ID before ID assignment" 崩溃。
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            // 检查骑乘层数
            int ridingLayers = getRidingLayers(entity);
            if (ridingLayers >= MAX_RIDING_LAYERS) {
                // 如果骑乘层数达到最大值，取消操作
                return InteractionResult.PASS;
            }

            DollEntity dollEntity = DollEntityItem.getDollEntity(level, mainHandItem);
            dollEntity.setPos(entity.getX(), entity.getY() + 1.5, entity.getZ());
            dollEntity.setYRot(player.getYRot() - 180);
            if (dollEntity.startRiding(entity)) {
                level.addFreshEntity(dollEntity);
                entity.playSound(SoundEvents.WOOL_PLACE, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    mainHandItem.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    /**
     * 计算实体的骑乘层数
     *
     * @param entity 要检查的实体
     * @return 骑乘层数（包括当前实体）
     */
    private static int getRidingLayers(Entity entity) {
        // 当前实体算作第1层
        int layers = 1;
        Entity current = entity;

        // 向上遍历所有乘坐的实体
        while (current.getVehicle() != null) {
            current = current.getVehicle();
            layers++;
        }

        return layers;
    }
}
