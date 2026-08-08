package com.github.ysbbbbbb.kaleidoscopedoll.client.render;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import java.lang.reflect.Field;

/**
 * 向 vanilla 的物品模型类型注册表注册自定义类型。
 * <p>
 * ItemModels.ID_MAPPER / SpecialModelRenderers.ID_MAPPER 为私有字段，
 * fabric 没有提供官方注册 API，因此使用反射在客户端初始化早期注册。
 */
public final class DollItemModels {
    private static final Identifier DOLL_ENTITY = Identifier.fromNamespaceAndPath("kaleidoscope_doll", "doll_entity");
    private static final Identifier CUSTOM_DOLL = Identifier.fromNamespaceAndPath("kaleidoscope_doll", "custom_doll");
    private static final Identifier CUSTOM_DOLL_ITEM = Identifier.fromNamespaceAndPath("kaleidoscope_doll", "custom_doll_item");

    private DollItemModels() {
    }

    public static void register() {
        register(ItemModels.class, "ID_MAPPER", DOLL_ENTITY, DollEntityItemModel.Unbaked.MAP_CODEC);
        register(ItemModels.class, "ID_MAPPER", CUSTOM_DOLL_ITEM, CustomDollItemModel.Unbaked.MAP_CODEC);
        register(SpecialModelRenderers.class, "ID_MAPPER", CUSTOM_DOLL, DollSpecialModelRenderer.Unbaked.MAP_CODEC);
    }

    @SuppressWarnings("unchecked")
    private static void register(Class<?> owner, String fieldName, Identifier id, MapCodec<?> codec) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<?>> mapper =
                    (ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<?>>) field.get(null);
            mapper.put(id, codec);
            KaleidoscopeDoll.LOGGER.info("Registered item model type {}", id);
        } catch (Exception e) {
            KaleidoscopeDoll.LOGGER.error("Failed to register item model type {}", id, e);
        }
    }
}
