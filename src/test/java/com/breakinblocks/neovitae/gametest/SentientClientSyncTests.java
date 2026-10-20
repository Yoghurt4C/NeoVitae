package com.breakinblocks.neovitae.gametest;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.registry.NVRegistries;
import com.breakinblocks.neovitae.common.sentient.SentientEffectComponents;
import com.breakinblocks.neovitae.common.sentient.SentientUpgrade;
import com.breakinblocks.neovitae.gametest.base.NVTestRegistrar;

public final class SentientClientSyncTests {

    private SentientClientSyncTests() {}

    private static SentientUpgrade upgrade(GameTestHelper helper, String name) {
        HolderLookup.RegistryLookup<SentientUpgrade> lookup = helper.getLevel().registryAccess().lookupOrThrow(NVRegistries.Keys.SENTIENT_UPGRADES);
        return lookup.getOrThrow(ResourceKey.create(NVRegistries.Keys.SENTIENT_UPGRADES, NeoVitae.rl(name))).value();
    }

    public static void register(NVTestRegistrar r) {
        r.add("sentient/client_codec_keeps_elytra_from_data_file", 20, helper -> {
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
            JsonElement file = SentientUpgrade.CODEC.encodeStart(ops, upgrade(helper, "elytra")).getOrThrow();
            SentientUpgrade client = SentientUpgrade.CLIENT_CODEC.parse(ops, file).getOrThrow();
            helper.assertTrue(client.effects().has(SentientEffectComponents.ELYTRA.get()),
                    "A client loading the elytra upgrade from its own data must still see the elytra flag");
            helper.succeed();
        });

        r.add("sentient/client_codec_keeps_elytra_over_network", 20, helper -> {
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, helper.getLevel().registryAccess());
            Tag sent = SentientUpgrade.CLIENT_CODEC.encodeStart(ops, upgrade(helper, "elytra")).getOrThrow();
            SentientUpgrade client = SentientUpgrade.CLIENT_CODEC.parse(ops, sent).getOrThrow();
            helper.assertTrue(client.effects().has(SentientEffectComponents.ELYTRA.get()),
                    "A client receiving the elytra upgrade over the network must see the elytra flag");
            helper.succeed();
        });

        r.add("sentient/client_codec_skips_complex_effects", 20, helper -> {
            RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
            JsonElement file = SentientUpgrade.CODEC.encodeStart(ops, upgrade(helper, "speed")).getOrThrow();
            SentientUpgrade client = SentientUpgrade.CLIENT_CODEC.parse(ops, file).getOrThrow();
            helper.assertTrue(client.effects().isEmpty(), "Only on/off flags should reach the client, got " + client.effects());
            helper.succeed();
        });
    }
}
