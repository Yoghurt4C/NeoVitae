package com.breakinblocks.neovitae.client.color;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import com.breakinblocks.neovitae.common.block.PillarAccent;

import java.util.Set;

public class PillarAccentColor implements BlockTintSource {

    @Override
    public int color(BlockState state) {
        if (!state.hasProperty(PillarAccent.PROPERTY)) return -1;
        return 0xFF000000 | state.getValue(PillarAccent.PROPERTY).color();
    }

    @Override
    public Set<Property<?>> relevantProperties() {
        return Set.of(PillarAccent.PROPERTY);
    }
}
