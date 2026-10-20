package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.breakinblocks.neovitae.api.routing.IFilterKey;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.common.routing.FilterMatchRules;
import com.breakinblocks.neovitae.common.routing.FilterMatchRules.ItemProperty;
import com.breakinblocks.neovitae.gametest.base.NVTestRegistrar;

import java.util.Set;

public final class RoutingFilterRuleTests {

    private RoutingFilterRuleTests() {}

    public static void register(NVTestRegistrar r) {
        r.add("routing_filter/plain_slot_matches_exact_item", 20, helper -> {
            IFilterKey key = FilterMatchRules.buildKey(new ItemStack(Items.STONE), Set.of(), 1);
            helper.assertTrue(key.doesStackMatch(new ItemStack(Items.STONE)), "Stone should match stone");
            helper.assertTrue(!key.doesStackMatch(new ItemStack(Items.DIRT)), "Dirt should not match stone");
            helper.succeed();
        });

        r.add("routing_filter/any_item_non_stackable", 20, helper -> {
            IFilterKey key = FilterMatchRules.buildKey(new ItemStack(Items.STONE),
                    Set.of(FilterMatchRules.ANY_ITEM, ItemProperty.NON_STACKABLE.id()), 1);
            helper.assertTrue(key.doesStackMatch(new ItemStack(Items.DIAMOND_SWORD)), "A sword is non-stackable");
            helper.assertTrue(!key.doesStackMatch(new ItemStack(Items.COBBLESTONE)), "Cobblestone stacks");
            helper.succeed();
        });

        r.add("routing_filter/tag_rule_matches_tag_members", 20, helper -> {
            IFilterKey key = FilterMatchRules.buildKey(new ItemStack(Items.OAK_LOG),
                    Set.of(FilterMatchRules.tagRule(ItemTags.LOGS)), 1);
            helper.assertTrue(key.doesStackMatch(new ItemStack(Items.BIRCH_LOG)), "Birch log is in #minecraft:logs");
            helper.assertTrue(!key.doesStackMatch(new ItemStack(Items.STONE)), "Stone is not a log");
            helper.succeed();
        });

        r.add("routing_filter/same_mod_matches_namespace", 20, helper -> {
            IFilterKey key = FilterMatchRules.buildKey(new ItemStack(Items.STONE), Set.of(FilterMatchRules.SAME_MOD), 1);
            helper.assertTrue(key.doesStackMatch(new ItemStack(Items.DIRT)), "Dirt is from minecraft");
            helper.assertTrue(!key.doesStackMatch(new ItemStack(NVItems.LEX_VITAE.get())), "The Lex Vitae is not from minecraft");
            helper.succeed();
        });

        r.add("routing_filter/tag_rule_round_trips", 20, helper -> {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/iron"));
            helper.assertTrue(FilterMatchRules.tagFromRule(FilterMatchRules.tagRule(tag)).orElse(null) != null
                            && FilterMatchRules.tagFromRule(FilterMatchRules.tagRule(tag)).get().equals(tag),
                    "A nested tag path should survive the rule encoding");
            helper.succeed();
        });
    }
}
