package com.breakinblocks.neovitae.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import com.breakinblocks.neovitae.NeoVitae;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SanguineCrafterAnchorData extends SavedData {
    public static final String ID = "sanguine_crafter_anchors";

    public record Anchor(UUID owner, BlockPos center) {}

    private record Tracked(GlobalPos pos, UUID owner, BlockPos center) {
        static final Codec<Tracked> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Tracked::pos),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Tracked::owner),
                BlockPos.CODEC.fieldOf("center").forGetter(Tracked::center)
        ).apply(builder, Tracked::new));
    }

    public static final Codec<SanguineCrafterAnchorData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Tracked.CODEC.listOf().optionalFieldOf("anchors", List.of()).forGetter(SanguineCrafterAnchorData::tracked)
    ).apply(builder, SanguineCrafterAnchorData::new));

    public static final SavedDataType<SanguineCrafterAnchorData> TYPE =
            new SavedDataType<>(NeoVitae.rl(ID), SanguineCrafterAnchorData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<GlobalPos, Anchor> anchors = new LinkedHashMap<>();

    public SanguineCrafterAnchorData() {
    }

    private SanguineCrafterAnchorData(List<Tracked> tracked) {
        for (Tracked t : tracked) {
            anchors.put(t.pos(), new Anchor(t.owner(), t.center()));
        }
    }

    private List<Tracked> tracked() {
        List<Tracked> out = new ArrayList<>(anchors.size());
        for (Map.Entry<GlobalPos, Anchor> e : anchors.entrySet()) {
            out.add(new Tracked(e.getKey(), e.getValue().owner(), e.getValue().center()));
        }
        return out;
    }

    public Map<GlobalPos, Anchor> anchors() {
        return anchors;
    }

    public int countFor(UUID owner) {
        int count = 0;
        for (Anchor anchor : anchors.values()) {
            if (anchor.owner().equals(owner)) count++;
        }
        return count;
    }

    public void put(GlobalPos pos, Anchor anchor) {
        if (!anchor.equals(anchors.put(pos, anchor))) {
            setDirty();
        }
    }

    public boolean remove(GlobalPos pos) {
        if (anchors.remove(pos) != null) {
            setDirty();
            return true;
        }
        return false;
    }
}
