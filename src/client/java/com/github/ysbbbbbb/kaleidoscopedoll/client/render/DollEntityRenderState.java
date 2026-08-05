package com.github.ysbbbbbb.kaleidoscopedoll.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public class DollEntityRenderState extends EntityRenderState {
    public BlockState displayBlockState;
    public String customDollId = "";
    public ItemStack holdItem = ItemStack.EMPTY;
    public Vector3fc displayScale = new org.joml.Vector3f(1.0f);
    public Vector3fc displayTranslation = new org.joml.Vector3f();
    public Vector3fc itemScale = new org.joml.Vector3f(1.0f);
    public Vector3fc itemTranslation = new org.joml.Vector3f();
    public Vector3fc itemRotation = new org.joml.Vector3f();
    public long bounceTime = 0;
    public float yRot;
    public float xRot;
}
