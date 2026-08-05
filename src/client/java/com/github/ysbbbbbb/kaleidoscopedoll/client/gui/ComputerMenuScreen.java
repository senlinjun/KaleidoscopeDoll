package com.github.ysbbbbbb.kaleidoscopedoll.client.gui;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.client.custom.CustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.config.GeneralConfig;
import com.github.ysbbbbbb.kaleidoscopedoll.data.custom.ServerCustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.datagen.TagItem;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopedoll.inventory.ComputerMenu;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import com.github.ysbbbbbb.kaleidoscopedoll.client.ClientNetworkHandler;
import com.github.ysbbbbbb.kaleidoscopedoll.network.message.ComputerDollClickMessage;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent.SPECIAL_TOOLTIPS;

public class ComputerMenuScreen extends AbstractContainerScreen<ComputerMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "textures/gui/computer.png");
    private static final int MAX_DOLLS_PER_PAGE = 32;

    private List<ItemStack> dolls = Lists.newArrayList();
    private int currentPage = 0;
    private float scrollOffs;
    private boolean scrolling;

    private EditBox searchField;

    public ComputerMenuScreen(ComputerMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn, 176, 208);
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        this.initSearch();
        this.initDollButtons();
    }

    private void initDollButtons() {
        String searchText = this.searchField.getValue().toLowerCase(Locale.ENGLISH);

        this.dolls.clear();
        if (isEmpty(searchText)) {
            this.getDolls();
        } else {
            this.getSearchDolls(searchText);
        }

        int start = currentPage * MAX_DOLLS_PER_PAGE;
        int end = Math.min(start + MAX_DOLLS_PER_PAGE, this.dolls.size());
        List<ItemStack> dollsToShow = this.dolls.subList(start, end);
        int xPos = this.leftPos + 7;
        int yPos = this.topPos + 17;
        int xOffset = 18;
        int yOffset = 18;
        for (int i = 0; i < dollsToShow.size(); i++) {
            ItemStack item = dollsToShow.get(i);
            int x = xPos + (i % 8) * xOffset;
            int y = yPos + (i / 8) * yOffset;
            this.addRenderableWidget(new DollButton(x, y, item, button -> {
                button.setFocused(!button.isFocused());
                ClientNetworkHandler.sendToServer(new ComputerDollClickMessage(item));
            }));
        }
    }

    private void getDolls() {
        // 自定义玩偶
        ServerCustomDollLoader.getModels().forEach(id -> {
            ItemStack stack = ModItems.CUSTOM_DOLL.getDefaultInstance();
            CustomDollItem.setModelId(stack, id);
            this.dolls.add(stack);
        });

        BuiltInRegistries.ITEM.getTagOrEmpty(TagItem.PLAYER_DOLLS).forEach(holder -> {
            ItemStack stack = holder.value().getDefaultInstance();
            if (filterSponsoredDoll(stack)) {
                this.dolls.add(stack);
            }
        });
    }

    private boolean filterSponsoredDoll(ItemStack item) {
        if (GeneralConfig.ENABLE_SPONSORED_DOLL.get()) {
            return true;
        }
        return !item.is(TagItem.SPONSORED_DOLLS);
    }

    private void getSearchDolls(String searchText) {
        // 自定义玩偶
        ServerCustomDollLoader.getModels().forEach(id -> {
            if (doSearchCustom(id, searchText)) {
                ItemStack stack = ModItems.CUSTOM_DOLL.getDefaultInstance();
                CustomDollItem.setModelId(stack, id);
                this.dolls.add(stack);
            }
        });

        BuiltInRegistries.ITEM.getTagOrEmpty(TagItem.PLAYER_DOLLS).forEach(holder -> {
            Item item = holder.value();
            if (doSearch(item, searchText) && filterSponsoredDoll(item.getDefaultInstance())) {
                this.dolls.add(item.getDefaultInstance());
            }
        });
    }

    private boolean doSearch(Item item, String searchText) {
        // 可以直接搜索物品 id 或者文本描述
        Identifier id = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
        if (id.toString().contains(searchText)) {
            return true;
        }
        String key = SPECIAL_TOOLTIPS.get(id);
        if (key == null) {
            return false;
        }
        // 语言文件的 key 也可作为搜索判断
        if (key.contains(searchText)) {
            return true;
        }
        return I18n.get("tooltip.kaleidoscope_doll.doll." + key).contains(searchText);
    }

    private boolean doSearchCustom(String id, String searchText) {
        // 可以直接搜索物品 id 或者文本描述
        if (id.contains(searchText)) {
            return true;
        }

        String local = Minecraft.getInstance().getLanguageManager().getSelected();
        String language = CustomDollLoader.getLanguage(local, id);
        // 语言文件搜索
        if (language.contains(searchText)) {
            return true;
        }

        // 回退搜索英文
        if (!"en_us".equals(local)) {
            language = CustomDollLoader.getLanguage("en_us", id);
            return language.contains(searchText);
        }

        return false;
    }

    private void initSearch() {
        String perText = "";
        boolean focus = false;
        if (searchField != null) {
            perText = searchField.getValue();
            focus = searchField.isFocused();
        }
        searchField = new EditBox(Minecraft.getInstance().font, leftPos + 8, topPos + 102, 85, 16, Component.literal("Doll Search Box"));
        searchField.setValue(perText);
        searchField.setBordered(false);
        searchField.setMaxLength(32);
        searchField.setTextColor(0x39ff56);
        searchField.setFocused(focus);
        searchField.moveCursorToEnd(false);
        this.addWidget(this.searchField);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.text(this.font, Component.translatable("block.kaleidoscope_doll.computer"),
                this.titleLabelX, this.titleLabelY, 0x404040);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        super.extractRenderState(graphics, x, y, partialTicks);

        int posX = this.leftPos;
        int posY = (this.height - this.imageHeight) / 2;
        graphics.blit(BG, posX, posY, this.imageWidth, this.imageHeight, 0, 0, this.imageWidth, this.imageHeight);

        int uOffset = this.isScrollBarActive() ? 0 : 14;
        int offset = (int) (57 * this.scrollOffs);
        graphics.blit(BG, this.leftPos + 155, this.topPos + 17 + offset,
                12, 15, 177 + uOffset, 0, 12, 15);

        this.searchField.extractWidgetRenderState(graphics, x, y, partialTicks);
        if (!this.searchField.isFocused() && this.searchField.getValue().isEmpty()) {
            graphics.text(font, Component.translatable("gui.kaleidoscope_doll.computer.search")
                            .withStyle(ChatFormatting.GRAY),
                    this.leftPos + 9, this.topPos + 102, 0xFFFFFF);
        }

        if (this.isHovering(108, 98, 16, 16, x, y)
            && this.hoveredSlot != null && !this.hoveredSlot.hasItem()) {
            graphics.setTooltipForNextFrame(Component.translatable("tooltip.kaleidoscope_doll.computer.input"), x, y);
        }

        this.extractTooltip(graphics, x, y);
    }

    @Override
    public void resize(int width, int height) {
        String value = this.searchField.getValue();
        super.resize(width, height);
        this.searchField.setValue(value);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (this.searchField.mouseClicked(event, bl)) {
            this.setFocused(this.searchField);
            return true;
        } else {
            this.searchField.setFocused(false);
        }

        this.scrolling = false;
        double scrollX = this.leftPos + 155;
        double scrollY = this.topPos + 17;
        if (scrollX <= mouseX && mouseX < (scrollX + 12) && scrollY <= mouseY && mouseY < (scrollY + 72)) {
            this.scrolling = true;
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseY = event.y();
        if (this.scrolling && this.isScrollBarActive()) {
            float yOffset = (float) mouseY - this.topPos - 17;
            this.scrollOffs = Mth.clamp(yOffset / 57, 0F, 1F);
            this.currentPage = (int) ((this.scrollOffs * this.getOffscreenRows()) + 0.5);
            this.init();
            return true;
        } else {
            return super.mouseDragged(event, dragX, dragY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (this.isScrollBarActive()) {
            int offscreenRows = this.getOffscreenRows();
            float scrollAmount = (float) deltaY / offscreenRows;
            this.scrollOffs = Mth.clamp(this.scrollOffs - scrollAmount, 0.0F, 1.0F);
            this.currentPage = (int) ((this.scrollOffs * offscreenRows) + 0.5);
            this.init();
        }
        return true;
    }

    private int getOffscreenRows() {
        return (this.dolls.size() + MAX_DOLLS_PER_PAGE - 1) / MAX_DOLLS_PER_PAGE - 1;
    }

    private boolean isScrollBarActive() {
        return this.dolls.size() > MAX_DOLLS_PER_PAGE;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent keyEvent) {
        String preText = this.searchField.getValue();
        if (this.searchField.keyPressed(keyEvent)) {
            if (!Objects.equals(preText, this.searchField.getValue())) {
                currentPage = 0;
                this.init();
            }
            return true;
        } else {
            return this.searchField.isFocused()
                   && this.searchField.isVisible()
                   && keyEvent.key() != InputConstants.KEY_ESCAPE
                   || super.keyPressed(keyEvent);
        }
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent characterEvent) {
        if (searchField == null) {
            return false;
        }
        String perText = this.searchField.getValue();
        if (this.searchField.charTyped(characterEvent)) {
            if (!Objects.equals(perText, this.searchField.getValue())) {
                currentPage = 0;
                this.init();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.searchField.setValue(text);
        } else {
            this.searchField.insertText(text);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
