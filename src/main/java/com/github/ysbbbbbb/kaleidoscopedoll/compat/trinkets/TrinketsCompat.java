package com.github.ysbbbbbb.kaleidoscopedoll.compat.trinkets;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;

import java.lang.reflect.Method;

/**
 * Trinkets(Updated) 软依赖桥接。
 * <p>
 * 仅当安装了 trinkets_updated 时才通过反射调用 TrinketsCompatImpl，
 * 未安装时本类不会触碰任何 trinkets 类，避免类加载失败。
 */
public final class TrinketsCompat {
    private static final String IMPL_CLASS = "com.github.ysbbbbbb.kaleidoscopedoll.compat.trinkets.TrinketsCompatImpl";
    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("trinkets_updated");
    private static final Method APPLY;

    static {
        Method method = null;
        if (LOADED) {
            try {
                method = Class.forName(IMPL_CLASS).getMethod("applyDollComponent", Item.Properties.class);
            } catch (Exception e) {
                KaleidoscopeDoll.LOGGER.error("Failed to init Trinkets compatibility", e);
            }
        }
        APPLY = method;
    }

    private TrinketsCompat() {
    }

    public static void applyDollComponent(Item.Properties properties) {
        if (APPLY != null) {
            try {
                APPLY.invoke(null, properties);
            } catch (Exception e) {
                KaleidoscopeDoll.LOGGER.error("Failed to apply trinket equipment component", e);
            }
        }
    }
}
