package com.github.ysbbbbbb.kaleidoscopedoll;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public class ClientHooks {
    public static Runnable OPEN_TWEAKS_TOOL_SCREEN = () -> {
    };

    public static Function<ItemStack, List<Component>> CUSTOM_DOLL_TOOLTIP = stack -> List.of();

    private ClientHooks() {
    }
}
