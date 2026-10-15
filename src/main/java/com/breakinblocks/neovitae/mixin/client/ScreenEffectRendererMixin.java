package com.breakinblocks.neovitae.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.breakinblocks.neovitae.client.render.NVFluidClientExtensions;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private MultiBufferSource bufferSource;

    @Inject(method = "renderScreenEffect", at = @At("TAIL"))
    private void neovitae$renderFluidOverlay(boolean isFirstPerson, boolean isSleeping, float partialTicks,
                                             SubmitNodeCollector submitNodeCollector, boolean hideGui, CallbackInfo ci) {
        if (isFirstPerson && !isSleeping) {
            NVFluidClientExtensions.renderOverlay(minecraft, bufferSource);
        }
    }
}
