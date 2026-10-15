package com.breakinblocks.neovitae.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.fluid.NVFluids;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public final class NVFluidClientExtensions implements IClientFluidTypeExtensions {

    private final Identifier overlay;
    private final float red;
    private final float green;
    private final float blue;
    private final float fogEnd;

    private NVFluidClientExtensions(String name, float red, float green, float blue, float fogEnd) {
        this.overlay = NeoVitae.rl("textures/misc/" + name + "_overlay.png");
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.fogEnd = fogEnd;
    }

    public static void register(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new NVFluidClientExtensions("essentia_vitae", 0.42F, 0.02F, 0.02F, 10.0F),
                NVFluids.ESSENTIA_VITAE_TYPE.get());
        event.registerFluidType(new NVFluidClientExtensions("animated_spiritus", 0.04F, 0.3F, 0.42F, 20.0F),
                NVFluids.ANIMATED_SPIRITUS_TYPE.get());
        event.registerFluidType(new NVFluidClientExtensions("liquified_experience", 0.32F, 0.44F, 0.04F, 20.0F),
                NVFluids.LIQUIFIED_EXPERIENCE_TYPE.get());
    }

    @Override
    public Identifier getRenderOverlayTexture(Minecraft mc) {
        return overlay;
    }

    @Override
    public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                               float darkenWorldAmount, Vector4f fluidFogColor) {
        float brightness = Math.max(0.2F,
                Lightmap.getBrightness(level.dimensionType(), level.getMaxLocalRawBrightness(camera.blockPosition())));
        fluidFogColor.set(red * brightness, green * brightness, blue * brightness, fluidFogColor.w());
    }

    @Override
    public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance,
                                float partialTick, FogData fogData) {
        float end = camera.entity().isSpectator() ? renderDistance * 0.5F : fogEnd;
        fogData.environmentalStart = -2.0F;
        fogData.environmentalEnd = end;
        fogData.skyEnd = end;
        fogData.cloudEnd = end;
    }

    public static boolean isEyeInNeoVitaeFluid(Player player) {
        return fluidAtEyes(player) != null;
    }

    public static void renderOverlay(Minecraft minecraft, MultiBufferSource bufferSource) {
        Player player = minecraft.player;
        if (player == null || player.isSpectator()) {
            return;
        }
        FluidState fluid = fluidAtEyes(player);
        if (fluid != null) {
            IClientFluidTypeExtensions.of(fluid).renderOverlay(minecraft, new PoseStack(), bufferSource);
        }
    }

    private static @Nullable FluidState fluidAtEyes(Player player) {
        Level level = player.level();
        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        FluidState fluid = level.getFluidState(eyePos);
        if (!(IClientFluidTypeExtensions.of(fluid) instanceof NVFluidClientExtensions)) {
            return null;
        }
        return player.getEyeY() <= eyePos.getY() + fluid.getHeight(level, eyePos) ? fluid : null;
    }
}
