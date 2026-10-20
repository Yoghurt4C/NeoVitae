package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.breakinblocks.neovitae.common.block.AthanorBlock;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.AthanorBlockEntity;
import com.breakinblocks.neovitae.gametest.base.NVTestRegistrar;

public final class AthanorSideTests {

    private AthanorSideTests() {}

    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private static AthanorBlockEntity place(GameTestHelper helper) {
        helper.setBlock(POS, NVBlocks.ATHANOR_BLOCK.block().get().defaultBlockState().setValue(AthanorBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(POS, AthanorBlockEntity.class);
    }

    private static ResourceHandler<ItemResource> items(GameTestHelper helper, BlockPos pos, Direction side) {
        return helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), side);
    }

    private static int insert(ResourceHandler<ItemResource> handler, Item item, int amount) {
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(ItemResource.of(item), amount, tx);
            tx.commit();
            return inserted;
        }
    }

    private static int extract(ResourceHandler<ItemResource> handler, Item item, int amount, boolean commit) {
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(ItemResource.of(item), amount, tx);
            if (commit) tx.commit();
            return extracted;
        }
    }

    public static void register(NVTestRegistrar r) {
        r.add("athanor/default_sides_keep_furnace_layout", 20, helper -> {
            AthanorBlockEntity athanor = place(helper);
            athanor.athanorInv.setStackInSlot(AthanorBlockEntity.OUTPUT_SLOT, new ItemStack(Items.IRON_INGOT, 4));

            helper.assertTrue(insert(items(helper, POS, Direction.UP), Items.RAW_IRON, 3) == 0, "The top should not accept inputs");
            int sideInserted = insert(items(helper, POS, Direction.EAST), Items.RAW_IRON, 3);
            helper.assertTrue(sideInserted == 3, "The sides should accept inputs, inserted " + sideInserted);
            helper.assertTrue(insert(items(helper, POS, Direction.DOWN), Items.RAW_IRON, 3) == 0, "The bottom should not accept inputs");

            ResourceHandler<ItemResource> bottom = items(helper, POS, Direction.DOWN);
            int pulledOutputs = extract(bottom, Items.IRON_INGOT, 64, true);
            int pulledInputs = extract(bottom, Items.RAW_IRON, 64, true);
            helper.assertTrue(pulledOutputs == 4, "The bottom should give the 4 outputs, gave " + pulledOutputs);
            helper.assertTrue(pulledInputs == 0, "Inputs must stay in place, pulled " + pulledInputs);
            helper.succeed();
        });

        r.add("athanor/toggled_sides_follow_the_slot_groups", 20, helper -> {
            AthanorBlockEntity athanor = place(helper);
            athanor.getSideConfig().setAllowed(AthanorBlockEntity.GROUP_INPUT, Direction.DOWN, true);
            athanor.getSideConfig().setAllowed(AthanorBlockEntity.GROUP_INPUT, Direction.NORTH, false);
            athanor.athanorInv.setStackInSlot(AthanorBlockEntity.OUTPUT_SLOT, new ItemStack(Items.IRON_INGOT, 2));

            helper.assertTrue(insert(items(helper, POS, Direction.NORTH), Items.RAW_IRON, 3) == 0, "A side closed to the inputs should refuse them");
            int bottomInserted = insert(items(helper, POS, Direction.DOWN), Items.RAW_IRON, 3);
            helper.assertTrue(bottomInserted == 3, "The bottom opened to the inputs should accept them, inserted " + bottomInserted);
            helper.assertTrue(athanor.athanorInv.getStackInSlot(AthanorBlockEntity.OUTPUT_SLOT).getCount() == 2, "Inputs must not land in the outputs");
            helper.succeed();
        });

        r.add("athanor/outputs_are_pushed_out_of_the_chosen_side", 60, helper -> {
            AthanorBlockEntity athanor = place(helper);
            helper.setBlock(POS.east(), Blocks.CHEST);
            athanor.setOutputFace(Direction.EAST);
            athanor.athanorInv.setStackInSlot(AthanorBlockEntity.OUTPUT_SLOT + 1, new ItemStack(Items.IRON_INGOT, 5));
            athanor.athanorInv.setStackInSlot(AthanorBlockEntity.INPUT_START, new ItemStack(Items.RAW_IRON, 2));

            helper.succeedWhen(() -> {
                ResourceHandler<ItemResource> chest = items(helper, POS.east(), Direction.WEST);
                int ingots = extract(chest, Items.IRON_INGOT, 64, false);
                int raw = extract(chest, Items.RAW_IRON, 64, false);
                helper.assertTrue(ingots == 5, "Expected 5 pushed ingots, found " + ingots);
                helper.assertTrue(raw == 0, "Inputs must not be pushed");
            });
        });
    }
}
