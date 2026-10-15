package com.breakinblocks.neovitae.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.breakinblocks.neovitae.common.block.AnchorAura;
import com.breakinblocks.neovitae.common.block.RitualAnchorBlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RitualAnchorBlockEntity extends BaseBlockEntity {

    private static final int AURA_INTERVAL = 20;

    private final Set<BlockPos> links = new LinkedHashSet<>();
    private boolean removing;

    public RitualAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(NVTiles.RITUAL_ANCHOR_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RitualAnchorBlockEntity anchor) {
        if (level.getGameTime() % AURA_INTERVAL != 0) return;
        anchor.refreshAura(level, pos, state);
    }

    public void refreshAura(Level level, BlockPos pos, BlockState state) {
        boolean anyActive = false;
        int mask = 0;
        boolean pruned = false;
        Iterator<BlockPos> it = links.iterator();
        while (it.hasNext()) {
            BlockPos link = it.next();
            if (!level.isLoaded(link)) continue;
            if (!(level.getBlockEntity(link) instanceof MasterRitualStoneBlockEntity mrs) || !pos.equals(mrs.getAnchorPos())) {
                it.remove();
                pruned = true;
                continue;
            }
            if (mrs.isActive() && mrs.getCurrentRitual() != null && !mrs.isSuspended()) {
                anyActive = true;
                mask |= mrs.getRecentSpiritusMask();
            }
        }
        if (pruned) setChanged();
        AnchorAura aura = AnchorAura.fromMask(anyActive, mask);
        if (state.getValue(RitualAnchorBlock.AURA) != aura) {
            level.setBlock(pos, state.setValue(RitualAnchorBlock.AURA, aura), Block.UPDATE_ALL);
        }
    }

    public Set<BlockPos> getLinks() {
        return links;
    }

    public void addLink(BlockPos pos) {
        if (links.add(pos.immutable())) {
            setChanged();
            refreshNow();
        }
    }

    public void removeLink(BlockPos pos) {
        if (links.remove(pos)) {
            setChanged();
            refreshNow();
        }
    }

    public void refreshNow() {
        if (level != null && !level.isClientSide() && !removing) {
            refreshAura(level, worldPosition, getBlockState());
        }
    }

    public static int auraColor(AnchorAura aura) {
        if (aura.type() == null) return 0xFF3A2A;
        return switch (aura.type()) {
            case RAW -> 0x66E6E6;
            case RUINA -> 0x55EE55;
            case NIHILUM -> 0xFF8844;
            case VINDICTA -> 0xBB55EE;
            case INVICTUS -> 0xEEEEEE;
        };
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        removing = true;
        if (level != null && !level.isClientSide()) {
            for (BlockPos link : List.copyOf(links)) {
                if (level.isLoaded(link) && level.getBlockEntity(link) instanceof MasterRitualStoneBlockEntity mrs
                        && pos.equals(mrs.getAnchorPos())) {
                    mrs.anchorRemoved();
                }
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.store("links", BlockPos.CODEC.listOf(), new ArrayList<>(links));
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        links.clear();
        tag.read("links", BlockPos.CODEC.listOf()).ifPresent(links::addAll);
    }
}
