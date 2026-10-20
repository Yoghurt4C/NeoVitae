package com.breakinblocks.neovitae.client.color;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.block.PillarAccent;

public record PillarAccentItemColor() implements ItemTintSource {

    public static final Identifier ID = NeoVitae.rl("pillar_accent");
    public static final MapCodec<PillarAccentItemColor> MAP_CODEC = MapCodec.unit(new PillarAccentItemColor());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        PillarAccent accent = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(PillarAccent.PROPERTY);
        return 0xFF000000 | (accent == null ? PillarAccent.DEFAULT_COLOR : accent.color());
    }

    @Override
    public MapCodec<PillarAccentItemColor> type() {
        return MAP_CODEC;
    }
}
