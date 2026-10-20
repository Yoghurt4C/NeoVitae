package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.breakinblocks.neovitae.common.block.AthanorBlock;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.AthanorBlockEntity;
import com.breakinblocks.neovitae.common.blockentity.AthanorSides;
import com.breakinblocks.neovitae.gametest.base.NVTestRegistrar;

public final class AthanorSideTests {

    private AthanorSideTests() {}

    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private static AthanorBlockEntity place(GameTestHelper helper) {
        helper.setBlock(POS, NVBlocks.ATHANOR_BLOCK.block().get().defaultBlockState().setValue(AthanorBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(POS, AthanorBlockEntity.class);
    }

    private static ResourceHandler<ItemResource> items(GameTestHelper helper, Direction side) {
        return helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(POS), side);
    }

    public static void register(NVTestRegistrar r) {
        r.add("athanor/default_sides_keep_furnace_layout", 20, helper -> {
            place(helper);
            helper.assertTrue(items(helper, Direction.UP).size() == 1, "Top should expose only the tool slot");
            helper.assertTrue(items(helper, Direction.DOWN).size() == AthanorBlockEntity.NUM_OUTPUTS, "Bottom should expose the outputs");
            helper.assertTrue(items(helper, Direction.EAST).size() == AthanorBlockEntity.NUM_INPUTS + 2, "Sides should expose inputs and buckets");
            helper.succeed();
        });

        r.add("athanor/off_side_exposes_nothing", 20, helper -> {
            AthanorBlockEntity athanor = place(helper);
            while (athanor.getSideMode(AthanorSides.Face.FRONT) != AthanorSides.Mode.OFF) {
                athanor.cycleSideMode(AthanorSides.Face.FRONT, 1);
            }
            helper.assertTrue(items(helper, Direction.NORTH) == null, "An Off front should expose no inventory");
            helper.assertTrue(items(helper, Direction.SOUTH) != null, "The back should be unaffected");
            helper.succeed();
        });

        r.add("athanor/both_side_inserts_inputs_and_extracts_outputs", 20, helper -> {
            AthanorBlockEntity athanor = place(helper);
            while (athanor.getSideMode(AthanorSides.Face.BOTTOM) != AthanorSides.Mode.BOTH) {
                athanor.cycleSideMode(AthanorSides.Face.BOTTOM, 1);
            }
            ResourceHandler<ItemResource> handler = items(helper, Direction.DOWN);
            athanor.athanorInv.setStackInSlot(AthanorBlockEntity.OUTPUT_SLOT, new ItemStack(Items.IRON_INGOT, 4));

            int inserted;
            try (Transaction tx = Transaction.openRoot()) {
                inserted = handler.insert(ItemResource.of(Items.RAW_IRON), 3, tx);
                tx.commit();
            }
            int extractedOutput;
            int extractedInput;
            try (Transaction tx = Transaction.openRoot()) {
                extractedOutput = handler.extract(ItemResource.of(Items.IRON_INGOT), 4, tx);
                extractedInput = handler.extract(ItemResource.of(Items.RAW_IRON), 3, tx);
                tx.commit();
            }

            helper.assertTrue(inserted == 3, "Inserting through Inputs & Outputs should fill the inputs, inserted " + inserted);
            helper.assertTrue(athanor.athanorInv.getStackInSlot(AthanorBlockEntity.INPUT_START).is(Items.RAW_IRON), "Raw iron should land in an input slot");
            helper.assertTrue(extractedOutput == 4, "Outputs should be extractable, got " + extractedOutput);
            helper.assertTrue(extractedInput == 0, "Inputs must not be pulled back out, got " + extractedInput);
            helper.succeed();
        });
    }
}
