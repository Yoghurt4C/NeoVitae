package com.breakinblocks.neovitae.client.color;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import com.breakinblocks.neovitae.common.block.RitualAnchorBlock;
import com.breakinblocks.neovitae.common.blockentity.RitualAnchorBlockEntity;

import java.util.Set;

public class RitualAnchorColor implements BlockTintSource {

    @Override
    public int color(BlockState state) {
        if (!state.hasProperty(RitualAnchorBlock.AURA)) return -1;
        return 0xFF000000 | RitualAnchorBlockEntity.auraColor(state.getValue(RitualAnchorBlock.AURA));
    }

    @Override
    public Set<Property<?>> relevantProperties() {
        return Set.of(RitualAnchorBlock.AURA);
    }
}
