package com.github.ysbbbbbb.kaleidoscopedoll.inventory;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.data.custom.ServerCustomDollLoader;
import com.github.ysbbbbbb.kaleidoscopedoll.datagen.TagItem;
import com.github.ysbbbbbb.kaleidoscopedoll.item.CustomDollItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;



public class ComputerMenu extends AbstractContainerMenu {
    public static final MenuType<ComputerMenu> TYPE = new MenuType<>(ComputerMenu::new, FeatureFlagSet.of());
    private static final Identifier OUTPUT_SLOT = Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "slot/computer_output_slot");
    private final Container input = new SimpleContainer(1) {
        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return stack.is(TagItem.COMPUTER_TOKENS);
        }
    };
    private final Container output = new SimpleContainer(1) {
        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return false;
        }
    };

    public ComputerMenu(int id, Inventory inventory) {
        super(TYPE, id);

        // 输入槽
        this.addSlot(new Slot(input, 0, 108, 98) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                // 清空输出槽
                output.setItem(0, ItemStack.EMPTY);
            }
        });

        // 输出槽
        this.addSlot(new Slot(output, 0, 151, 98) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                ItemStack inputStack = input.getItem(0);
                if (!inputStack.isEmpty()) {
                    // 输入槽物品数量减 1
                    inputStack.shrink(1);
                    input.setItem(0, inputStack);
                }
            }

            @Override
            public Identifier getNoItemIcon() {
                return OUTPUT_SLOT;
            }
        });

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 184));
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 126 + i * 18));
            }
        }
    }

    public boolean clickDollButton(ItemStack doll) {
        ItemStack stack = this.input.getItem(0);
        // 防止作弊，必须检查一次
        if (stack.is(TagItem.COMPUTER_TOKENS)) {
            // 要么是玩家玩偶
            if (doll.is(TagItem.PLAYER_DOLLS)) {
                // 玩家玩偶不需要任何组件，故直接创建新的 stack 对象
                ItemStack newStack = new ItemStack(doll.getItem());
                this.output.setItem(0, newStack);
                return true;
            }

            // 要么是自定义玩偶
            if (doll.is(TagItem.CUSTOM_DOLLS)) {
                // 自定义的 ID 必须在服务端存在
                String id = CustomDollItem.getModelId(doll);
                if (id.isEmpty() || !ServerCustomDollLoader.getModels().contains(id)) {
                    return false;
                }
                // 自定义玩偶仅允许 TAG_CUSTOM_DOLL_ID 组件
                ItemStack newStack = new ItemStack(doll.getItem());
                CustomDollItem.setModelId(newStack, id);
                this.output.setItem(0, newStack);
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            itemStack = slotItem.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(slotItem, 2, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
                // 点击的就是输出槽，并且转移成功了，此时输入槽扣除一个
                if (index == 1) {
                    ItemStack inputStack = input.getItem(0);
                    if (!inputStack.isEmpty()) {
                        // 输入槽物品数量减 1
                        inputStack.shrink(1);
                        input.setItem(0, inputStack);
                    }
                }
            } else if (!this.moveItemStackTo(slotItem, 0, 2, true)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            player.getInventory().placeItemBackInInventory(input.getItem(0));
        }
    }
}
