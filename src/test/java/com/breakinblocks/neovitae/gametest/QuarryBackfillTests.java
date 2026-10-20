package com.breakinblocks.neovitae.gametest;

import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.MasterRitualStoneBlockEntity;
import com.breakinblocks.neovitae.gametest.base.NVTestRegistrar;
import com.breakinblocks.neovitae.ritual.NVRituals;
import com.breakinblocks.neovitae.ritual.types.RitualMagnetism;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class QuarryBackfillTests {

    private QuarryBackfillTests() {}

    private static final BlockPos MRS_POS = new BlockPos(2, 4, 2);

    private static RitualMagnetism activeQuarry(GameTestHelper helper, int cursorY) {
        helper.setBlock(MRS_POS, NVBlocks.MASTER_RITUAL_STONE.block().get());
        MasterRitualStoneBlockEntity mrs = helper.getBlockEntity(MRS_POS, MasterRitualStoneBlockEntity.class);
        if (mrs == null) {
            helper.fail("Master ritual stone did not place");
            return null;
        }
        mrs.forceActivateRitual(NVRituals.DEUS_MAGNETISM.get(), null);
        if (!(mrs.getCurrentRitual() instanceof RitualMagnetism quarry)) {
            helper.fail("Expected an active Endless Quarry, got " + mrs.getCurrentRitual());
            return null;
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt("lastPosX", -3);
        tag.putInt("lastPosY", cursorY);
        tag.putInt("lastPosZ", -3);
        quarry.readFromNBT(tag);
        return quarry;
    }

    private static int cursorY(RitualMagnetism quarry) {
        CompoundTag tag = new CompoundTag();
        quarry.writeToNBT(tag);
        return tag.getIntOr("lastPosY", Integer.MIN_VALUE);
    }

    public static void register(NVTestRegistrar r) {
        r.add("quarry/backfill_matches_surrounding_stone", 60, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos hole = new BlockPos(2, 1, 2);

            helper.setBlock(hole.north(), Blocks.DEEPSLATE);
            helper.setBlock(hole.south(), Blocks.DEEPSLATE);

            BlockState filler = RitualMagnetism.fillerFor(level, helper.absolutePos(hole));
            if (!filler.is(Blocks.DEEPSLATE)) {
                helper.fail("Backfill picked " + filler + " instead of the surrounding deepslate");
                return;
            }
            helper.succeed();
        });

        r.add("quarry/backfill_never_copies_player_blocks", 60, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos hole = new BlockPos(2, 1, 2);

            // Valuables and built blocks must never be duplicated into the hole.
            helper.setBlock(hole.north(), Blocks.DIAMOND_BLOCK);
            helper.setBlock(hole.south(), Blocks.CHEST);
            helper.setBlock(hole.east(), Blocks.OAK_PLANKS);

            BlockState filler = RitualMagnetism.fillerFor(level, helper.absolutePos(hole));
            if (!filler.is(Blocks.STONE)) {
                helper.fail("Backfill copied a non-natural neighbour: " + filler);
                return;
            }
            helper.succeed();
        });

        r.add("quarry/meteor_rewinds_passed_scan", 40, helper -> {
            RitualMagnetism quarry = activeQuarry(helper, -40);
            if (quarry == null) return;

            RitualMagnetism.notifyDeposit(helper.getLevel(), helper.absolutePos(MRS_POS.below(3)), 1);
            int cursor = cursorY(quarry);
            if (cursor != -2) {
                helper.fail("A meteor above the scan cursor should rewind it to the meteor's top layer, cursor=" + cursor);
                return;
            }
            helper.succeed();
        });

        r.add("quarry/meteor_below_cursor_keeps_scan", 40, helper -> {
            RitualMagnetism quarry = activeQuarry(helper, -1);
            if (quarry == null) return;

            RitualMagnetism.notifyDeposit(helper.getLevel(), helper.absolutePos(MRS_POS.below(3)), 1);
            int cursor = cursorY(quarry);
            if (cursor != -1) {
                helper.fail("A meteor the scan has not reached yet must not move the cursor, cursor=" + cursor);
                return;
            }
            helper.succeed();
        });

        r.add("quarry/backfill_falls_back_to_stone_in_open_air", 60, helper -> {
            BlockState filler = RitualMagnetism.fillerFor(helper.getLevel(),
                    helper.absolutePos(new BlockPos(2, 3, 2)));
            if (!filler.is(Blocks.STONE)) {
                helper.fail("Expected a plain stone fallback, got " + filler);
                return;
            }
            helper.succeed();
        });
    }
}
