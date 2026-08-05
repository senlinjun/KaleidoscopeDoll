package com.github.ysbbbbbb.kaleidoscopedoll.compat.trinkets;

import eu.pb4.trinkets.api.component.TrinketDataComponents;
import eu.pb4.trinkets.api.component.TrinketEquippable;
import net.minecraft.world.item.Item;

public final class TrinketsCompatImpl {
    public static final String SLOT = "head/doll";

    private TrinketsCompatImpl() {
    }

    public static void applyDollComponent(Item.Properties properties) {
        properties.component(TrinketDataComponents.EQUIPMENT,
                TrinketEquippable.DEFAULT.withSlots(SLOT).withEquipOnInteract(true));
    }
}
