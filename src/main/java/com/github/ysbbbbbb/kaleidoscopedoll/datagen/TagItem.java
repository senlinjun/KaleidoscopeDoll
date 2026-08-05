package com.github.ysbbbbbb.kaleidoscopedoll.datagen;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent.SPECIAL_TOOLTIPS;

public class TagItem extends FabricTagsProvider<Item> {
    /**
     * 原版生物玩偶
     */
    public static final TagKey<Item> VANILLA_DOLLS = itemTagKey("vanilla_dolls");
    /**
     * 玩家玩偶
     */
    public static final TagKey<Item> PLAYER_DOLLS = itemTagKey("player_dolls");
    /**
     * 作者玩偶，注意所有的作者玩偶也是玩家玩偶
     */
    public static final TagKey<Item> AUTHOR_DOLLS = itemTagKey("author_dolls");
    /**
     * 赞助玩偶
     */
    public static final TagKey<Item> SPONSORED_DOLLS = itemTagKey("sponsored_dolls");
    /**
     * 自定义玩偶
     */
    public static final TagKey<Item> CUSTOM_DOLLS = itemTagKey("custom_dolls");
    /**
     * 所有本模组玩偶的 tag，因为方块形态玩偶都使用了不同的注册名
     */
    public static final TagKey<Item> ALL_DOLLS = itemTagKey("all_dolls");
    /**
     * 方块玩偶转换成实体玩偶，合成所需的素材
     */
    public static final TagKey<Item> BLOCK_DOLLS_TO_ENTITY_ITEM = itemTagKey("block_dolls_to_entity_item");

    /**
     * 所有等级的代币
     */
    public static final TagKey<Item> MACHINE_TOKENS = itemTagKey("machine_tokens");
    /**
     * 0 级代币，能够在抽奖机中抽取主世界生物玩偶
     */
    public static final TagKey<Item> TIER_0_MACHINE_TOKENS = itemTagKey("tier_0_machine_tokens");
    /**
     * 1 级代币，能够在抽奖机中抽取下界生物玩偶
     */
    public static final TagKey<Item> TIER_1_MACHINE_TOKENS = itemTagKey("tier_1_machine_tokens");
    /**
     * 2 级代币，能够在抽奖机中抽取末地生物玩偶
     */
    public static final TagKey<Item> TIER_2_MACHINE_TOKENS = itemTagKey("tier_2_machine_tokens");

    /**
     * 0 级玩偶，能够通过 0 级代币在抽奖机中抽取
     */
    public static final TagKey<Item> TIER_0_DOLLS = itemTagKey("tier_0_dolls");
    /**
     * 1 级玩偶，能够通过 1 级代币在抽奖机中抽取
     */
    public static final TagKey<Item> TIER_1_DOLLS = itemTagKey("tier_1_dolls");
    /**
     * 2 级玩偶，能够通过 2 级代币在抽奖机中抽取
     */
    public static final TagKey<Item> TIER_2_DOLLS = itemTagKey("tier_2_dolls");

    /**
     * 用电脑合成玩偶时需要的代币
     */
    public static final TagKey<Item> COMPUTER_TOKENS = itemTagKey("computer_tokens");

    /**
     * 可被 Trinkets 穿戴的玩偶（戴在 head/doll 槽位）
     */
    public static final TagKey<Item> WEARABLE_DOLLS = itemTagKey("wearable_dolls");

    public TagItem(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ITEM, lookupProvider);
    }

    private static TagKey<Item> itemTagKey(String key) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, key));
    }

    private static ResourceKey<Item> dollItem(String key) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, key));
    }

    private static ResourceKey<Item> itemKey(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }

    @Override
    @SuppressWarnings("all")
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(VANILLA_DOLLS).addAll(ModRegisterEvent.DOLL_ITEMS.stream()
                .filter(item -> !SPECIAL_TOOLTIPS.containsKey(BuiltInRegistries.ITEM.getKey(item)))
                .map(TagItem::itemKey));

        this.tag(PLAYER_DOLLS).addAll(ModRegisterEvent.DOLL_ITEMS.stream()
                .filter(item -> SPECIAL_TOOLTIPS.containsKey(BuiltInRegistries.ITEM.getKey(item)))
                .map(TagItem::itemKey));

        this.tag(AUTHOR_DOLLS).addAll(ModRegisterEvent.DOLL_ITEMS.stream()
                .filter(item -> ModRegisterEvent.AUTHOR_DOLLS.contains(BuiltInRegistries.ITEM.getKey(item)))
                .map(TagItem::itemKey));

        this.tag(SPONSORED_DOLLS).addAll(ModRegisterEvent.DOLL_ITEMS.stream()
                .filter(item -> SPECIAL_TOOLTIPS.containsKey(BuiltInRegistries.ITEM.getKey(item))
                                && !ModRegisterEvent.AUTHOR_DOLLS.contains(BuiltInRegistries.ITEM.getKey(item)))
                .map(TagItem::itemKey));

        this.tag(CUSTOM_DOLLS).add(itemKey(ModItems.CUSTOM_DOLL));

        this.tag(ALL_DOLLS).addAll(ModRegisterEvent.DOLL_ITEMS.stream().map(TagItem::itemKey)).add(itemKey(ModItems.CUSTOM_DOLL));

        this.tag(BLOCK_DOLLS_TO_ENTITY_ITEM).add(itemKey(Items.SLIME_BALL), itemKey(Items.CLAY_BALL));

        this.tag(TIER_0_MACHINE_TOKENS).add(itemKey(Items.IRON_INGOT));
        this.tag(TIER_1_MACHINE_TOKENS).add(itemKey(Items.GOLD_INGOT));
        this.tag(TIER_2_MACHINE_TOKENS).add(itemKey(Items.DIAMOND));
        this.tag(MACHINE_TOKENS).addTag(TIER_0_MACHINE_TOKENS)
                .addTag(TIER_1_MACHINE_TOKENS)
                .addTag(TIER_2_MACHINE_TOKENS);

        List<ResourceKey<Item>> tier1Dolls = Lists.newArrayList(
                dollItem("doll_2"),
                dollItem("doll_10"),
                dollItem("doll_12"),
                dollItem("doll_15"),
                dollItem("doll_16"),
                dollItem("doll_17"),
                dollItem("doll_34"),
                dollItem("doll_35"),
                dollItem("doll_66"),
                dollItem("doll_326")
        );
        this.tag(TIER_1_DOLLS).add(tier1Dolls.toArray(new ResourceKey[0]));

        List<ResourceKey<Item>> tier2Dolls = Lists.newArrayList(
                dollItem("doll_3"),
                dollItem("doll_11"),
                dollItem("doll_21"),
                dollItem("doll_327")
        );
        this.tag(TIER_2_DOLLS).add(tier2Dolls.toArray(new ResourceKey[0]));

        // 0 级玩偶是除了 1 级和 2 级玩偶之外的所有玩偶
        this.tag(TIER_0_DOLLS).addAll(ModRegisterEvent.DOLL_ITEMS.stream()
                .filter(item -> !SPECIAL_TOOLTIPS.containsKey(BuiltInRegistries.ITEM.getKey(item)))
                .map(TagItem::itemKey)
                .filter(k -> !tier1Dolls.contains(k) && !tier2Dolls.contains(k)));

        this.tag(WEARABLE_DOLLS).addAll(ModRegisterEvent.DOLL_ITEMS.stream().map(TagItem::itemKey))
                .add(itemKey(ModItems.CUSTOM_DOLL));

        this.tag(COMPUTER_TOKENS).add(
                itemKey(Items.WOOL.white()),
                itemKey(Items.WOOL.orange()),
                itemKey(Items.WOOL.magenta()),
                itemKey(Items.WOOL.lightBlue()),
                itemKey(Items.WOOL.yellow()),
                itemKey(Items.WOOL.lime()),
                itemKey(Items.WOOL.pink()),
                itemKey(Items.WOOL.gray()),
                itemKey(Items.WOOL.lightGray()),
                itemKey(Items.WOOL.cyan()),
                itemKey(Items.WOOL.purple()),
                itemKey(Items.WOOL.blue()),
                itemKey(Items.WOOL.brown()),
                itemKey(Items.WOOL.green()),
                itemKey(Items.WOOL.red()),
                itemKey(Items.WOOL.black()));
    }
}
