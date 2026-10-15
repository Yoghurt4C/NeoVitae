package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.common.datacomponent.Anima;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.gametest.base.NVTestRegistrar;
import com.breakinblocks.neovitae.ritual.EnumRuneType;
import com.breakinblocks.neovitae.ritual.NVRituals;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.breakinblocks.neovitae.ritual.RitualComponent;
import com.breakinblocks.neovitae.spiritus.WorldSpiritusHandler;
import com.breakinblocks.neovitae.util.helper.AnimaHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RitualCraftingTests {

    private RitualCraftingTests() {}

    private static final BlockPos MRS_POS = new BlockPos(12, 2, 12);

    private static void placeMrs(GameTestHelper helper, Ritual ritual) {
        helper.setBlock(MRS_POS, NVBlocks.MASTER_RITUAL_STONE.block().get().defaultBlockState());
        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);
        for (RitualComponent component : components) {
            helper.setBlock(MRS_POS.offset(component.offset()), runeBlock(component.runeType()).defaultBlockState());
        }
        MasterRitualStoneBlockEntity mrs = helper.getBlockEntity(MRS_POS, MasterRitualStoneBlockEntity.class);
        UUID owner = UUID.randomUUID();
        Anima anima = AnimaHelper.getAnima(owner);
        anima.add(AnimaTicket.create(1000000), 10000000);
        mrs.forceActivateRitual(ritual, null);
        mrs.setOwner(owner);
    }

    private static Block runeBlock(EnumRuneType type) {
        return switch (type) {
            case BLANK -> NVBlocks.BLANK_RITUAL_STONE.block().get();
            case WATER -> NVBlocks.WATER_RITUAL_STONE.block().get();
            case FIRE -> NVBlocks.FIRE_RITUAL_STONE.block().get();
            case EARTH -> NVBlocks.EARTH_RITUAL_STONE.block().get();
            case AIR -> NVBlocks.AIR_RITUAL_STONE.block().get();
            case TENEBRAE -> NVBlocks.TENEBRAE_RITUAL_STONE.block().get();
            case DEUS -> NVBlocks.DEUS_RITUAL_STONE.block().get();
        };
    }

    private static BarrelBlockEntity placeBarrel(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.BARREL.defaultBlockState());
        return helper.getBlockEntity(pos, BarrelBlockEntity.class);
    }

    private static BarrelBlockEntity setUpCrystallarium(GameTestHelper helper, BarrelBlockEntity input) {
        input.setItem(0, new ItemStack(NVBlocks.HELLFIRE_FORGE.item().get()));
        input.setItem(1, new ItemStack(Items.DEEPSLATE));
        input.setItem(2, new ItemStack(Items.LAPIS_LAZULI));
        input.setItem(3, new ItemStack(Items.GLASS));
        WorldSpiritusHandler.addSpiritusToChunk(helper.getLevel(), helper.absolutePos(MRS_POS), SpiritusType.INVICTUS, 50.0);
        return placeBarrel(helper, MRS_POS.below());
    }

    public static void register(NVTestRegistrar r) {
        r.addIsolated("ritual_crafting/forge_recipe_paid_by_gem_in_fifth_slot", 200, helper -> {
            BarrelBlockEntity input = placeBarrel(helper, MRS_POS.above());
            BarrelBlockEntity output = setUpCrystallarium(helper, input);
            ItemStack gem = new ItemStack(NVItems.SPIRITUS_GEM_GREATER.get());
            gem.set(NVDataComponents.SPIRITUS_AMOUNT, 600.0);
            input.setItem(4, gem);
            placeMrs(helper, NVRituals.CRAFTING.get());

            helper.succeedWhen(() -> {
                helper.assertTrue(output.getItem(0).is(NVBlocks.CRYSTALLARIUM_MALEFICUM.item().get()),
                        "Expected Crystallarium Maleficum in the output barrel, got " + output.getItem(0));
                helper.assertTrue(input.getItem(1).isEmpty(), "Ingredients should be consumed, slot 1 holds " + input.getItem(1));
                double left = input.getItem(4).getOrDefault(NVDataComponents.SPIRITUS_AMOUNT, 0D);
                helper.assertTrue(left == 500.0, "Gem should hold 500 Spiritus after a 100 drain, holds " + left);
            });
        });

        r.addIsolated("ritual_crafting/forge_recipe_without_gem_does_not_craft", 120, helper -> {
            BarrelBlockEntity input = placeBarrel(helper, MRS_POS.above());
            BarrelBlockEntity output = setUpCrystallarium(helper, input);
            placeMrs(helper, NVRituals.CRAFTING.get());

            helper.runAfterDelay(100, () -> {
                helper.assertTrue(output.isEmpty(), "Nothing should craft without a gem, output holds " + output.getItem(0));
                helper.assertTrue(input.getItem(1).is(Items.DEEPSLATE), "Ingredients should stay, slot 1 holds " + input.getItem(1));
                helper.succeed();
            });
        });
    }
}
