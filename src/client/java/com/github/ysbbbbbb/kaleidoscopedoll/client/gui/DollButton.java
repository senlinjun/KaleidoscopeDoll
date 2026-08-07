package com.github.ysbbbbbb.kaleidoscopedoll.client.gui;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class DollButton extends ImageButton {
    private static final Identifier BG = Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "textures/gui/computer.png");
    private static final WidgetSprites SPRITES = new WidgetSprites(BG);
    private final ItemStack doll;

    public DollButton(int pX, int pY, ItemStack doll, OnPress onPress) {
        super(pX, pY, 18, 18, SPRITES, onPress, Component.empty());
        this.doll = doll;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int yTex = 16;
        if (this.isFocused()) {
            yTex += 18;
        } else if (this.isHovered()) {
            yTex += 36;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, BG, this.getX(), this.getY(), 177.0F, yTex, 18, 18, 256, 256);
        graphics.fakeItem(this.doll, this.getX() + 1, this.getY() + 1);
        if (this.isHovered()) {
            graphics.setTooltipForNextFrame(this.doll.getHoverName(), pMouseX, pMouseY);
        }
    }
}
