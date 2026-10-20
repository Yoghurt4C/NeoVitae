package com.breakinblocks.neovitae.client.model;

import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.client.helper.LexVitaeFormResolver;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record LexFormProperty() implements RangeSelectItemModelProperty {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(NeoVitae.MODID, "lex_form");
    public static final MapCodec<LexFormProperty> MAP_CODEC = MapCodec.unit(new LexFormProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        return LexVitaeFormResolver.resolve(stack, owner == null ? null : owner.asLivingEntity());
    }

    @Override
    public MapCodec<LexFormProperty> type() {
        return MAP_CODEC;
    }
}
