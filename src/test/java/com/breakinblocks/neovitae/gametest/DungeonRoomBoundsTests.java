package com.breakinblocks.neovitae.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.gametest.base.NVTestRegistrar;
import com.breakinblocks.neovitae.structures.DungeonRoom;
import com.breakinblocks.neovitae.structures.DungeonRoomRegistry;
import com.breakinblocks.neovitae.structures.RoomBoundsProcessor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DungeonRoomBoundsTests {

    private DungeonRoomBoundsTests() {}

    public static void register(NVTestRegistrar r) {
        r.add("dungeon/rotated_room_bounds_match_template", 40, helper -> {
            int checked = 0;
            for (Map.Entry<Identifier, DungeonRoom> entry : DungeonRoomRegistry.getDungeonRoomMap().entrySet()) {
                DungeonRoom room = entry.getValue();
                if (room.getStructureMap().size() != 1) continue;
                StructurePlaceSettings unrotated = new StructurePlaceSettings();
                List<AreaDescriptor> base = room.getAreaDescriptors(unrotated, BlockPos.ZERO);
                if (base.size() != 1 || !(base.get(0) instanceof AreaDescriptor.Rectangle baseRect)) continue;

                String structure = room.getStructureMap().keySet().iterator().next();
                Optional<StructureTemplate> template = helper.getLevel().getStructureManager().get(Identifier.parse(structure));
                if (template.isEmpty()) continue;
                if (!baseRect.getMinimumOffset().equals(BlockPos.ZERO)
                        || !baseRect.getMaximumOffset().equals(new BlockPos(template.get().getSize()))) continue;

                for (Rotation rotation : Rotation.values()) {
                    StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(rotation);
                    AreaDescriptor.Rectangle rect = (AreaDescriptor.Rectangle) room.getAreaDescriptors(settings, BlockPos.ZERO).get(0);
                    BoundingBox placed = template.get().getBoundingBox(settings, BlockPos.ZERO);
                    BlockPos placedMin = new BlockPos(placed.minX(), placed.minY(), placed.minZ());
                    BlockPos placedMax = new BlockPos(placed.maxX() + 1, placed.maxY() + 1, placed.maxZ() + 1);
                    helper.assertTrue(rect.getMinimumOffset().equals(placedMin) && rect.getMaximumOffset().equals(placedMax),
                            entry.getKey() + " rotated " + rotation + " declares " + rect.getMinimumOffset() + ".."
                                    + rect.getMaximumOffset() + " but its blocks span " + placedMin + ".." + placedMax);
                }
                checked++;
            }
            helper.assertTrue(checked > 0, "No box-shaped dungeon rooms were checked");
            helper.succeed();
        });

        r.add("dungeon/room_air_stays_inside_its_bounds", 20, helper -> {
            RoomBoundsProcessor processor = new RoomBoundsProcessor(
                    List.of(new AreaDescriptor.Rectangle(BlockPos.ZERO, new BlockPos(2, 2, 2))));
            StructurePlaceSettings settings = new StructurePlaceSettings();
            StructureTemplate.StructureBlockInfo insideAir = new StructureTemplate.StructureBlockInfo(new BlockPos(1, 1, 1), Blocks.AIR.defaultBlockState(), null);
            StructureTemplate.StructureBlockInfo edgeAir = new StructureTemplate.StructureBlockInfo(new BlockPos(2, 0, 0), Blocks.AIR.defaultBlockState(), null);
            StructureTemplate.StructureBlockInfo outsideStone = new StructureTemplate.StructureBlockInfo(new BlockPos(5, 5, 5), Blocks.STONE.defaultBlockState(), null);

            helper.assertTrue(processor.process(helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, insideAir, insideAir, settings, null) != null,
                    "Air inside the room should still be placed");
            helper.assertTrue(processor.process(helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, edgeAir, edgeAir, settings, null) == null,
                    "Air just past the room's edge must not be placed");
            helper.assertTrue(processor.process(helper.getLevel(), BlockPos.ZERO, BlockPos.ZERO, outsideStone, outsideStone, settings, null) != null,
                    "Solid blocks are left to the room's own data");
            helper.succeed();
        });
    }
}
