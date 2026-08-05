package com.github.ysbbbbbb.kaleidoscopedoll.datagen;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent;
import com.github.ysbbbbbb.kaleidoscopedoll.init.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;

import static com.github.ysbbbbbb.kaleidoscopedoll.event.ModRegisterEvent.SPECIAL_TOOLTIPS;

public class AdvancementGenerator extends FabricAdvancementProvider {
    protected AdvancementGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> saver) {
        AdvancementHolder root = make(item("doll_52"), "root")
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("tick", PlayerTrigger.TriggerInstance.tick())
                .save(saver, id("doll/root").toString());

        AdvancementHolder dollMachine = make(ModItems.DOLL_MACHINE, "doll_machine").parent(root)
                .addCriterion("doll_machine", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.DOLL_MACHINE))
                .save(saver, id("doll/doll_machine").toString());

        make(ModItems.PURPLE_DOLL_GIFT_BOX, "purple_doll_gift_box").parent(dollMachine)
                .addCriterion("purple_doll_gift_box", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.PURPLE_DOLL_GIFT_BOX))
                .save(saver, id("doll/purple_doll_gift_box").toString());
        make(ModItems.GREEN_DOLL_GIFT_BOX, "green_doll_gift_box").parent(dollMachine)
                .addCriterion("green_doll_gift_box", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.GREEN_DOLL_GIFT_BOX))
                .save(saver, id("doll/green_doll_gift_box").toString());
        AdvancementHolder yellowGiftBox = make(ModItems.YELLOW_DOLL_GIFT_BOX, "yellow_doll_gift_box").parent(dollMachine)
                .addCriterion("yellow_doll_gift_box", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.YELLOW_DOLL_GIFT_BOX))
                .save(saver, id("doll/yellow_doll_gift_box").toString());

        make(item("doll_0"), "credits").parent(root)
                .addCriterion("credits", PlayerTrigger.TriggerInstance.tick())
                .save(saver, id("doll/credits").toString());

        Advancement.Builder fullCollection = makeChallenge(item("doll_5"), "full_collection").parent(yellowGiftBox)
                .requirements(AdvancementRequirements.Strategy.AND);
        ModRegisterEvent.DOLL_ITEMS.stream()
                .filter(item -> !SPECIAL_TOOLTIPS.containsKey(BuiltInRegistries.ITEM.getKey(item)))
                .forEach(item -> fullCollection.addCriterion(BuiltInRegistries.ITEM.getKey(item).getPath(),
                        InventoryChangeTrigger.TriggerInstance.hasItems(item)));
        fullCollection.rewards(AdvancementRewards.Builder.experience(100))
                .save(saver, id("doll/full_collection").toString());
    }

    private static Advancement.Builder make(ItemLike item, String key) {
        Component title = Component.translatable(String.format("advancements.kaleidoscope_doll.doll.%s.title", key));
        Component desc = Component.translatable(String.format("advancements.kaleidoscope_doll.doll.%s.description", key));

        return Advancement.Builder.advancement().display(item, title, desc,
                Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "textures/advancement/background.png"),
                AdvancementType.TASK, false, false, false);
    }

    private static Advancement.Builder makeChallenge(ItemLike item, String key) {
        Component title = Component.translatable(String.format("advancements.kaleidoscope_doll.doll.%s.title", key));
        Component desc = Component.translatable(String.format("advancements.kaleidoscope_doll.doll.%s.description", key));

        return Advancement.Builder.advancement().display(item, title, desc,
                Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, "textures/advancement/background.png"),
                AdvancementType.CHALLENGE, true, true, false);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(KaleidoscopeDoll.MOD_ID, path);
    }

    private static net.minecraft.world.item.Item item(String path) {
        return BuiltInRegistries.ITEM.get(id(path)).orElseThrow().value();
    }
}
