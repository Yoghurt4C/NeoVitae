package com.breakinblocks.neovitae.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neovitae.common.blockentity.HellfireForgeBlockEntity;

public class HellfireForgeRenderer implements BlockEntityRenderer<HellfireForgeBlockEntity, HellfireForgeRenderer.State> {

    private static final float CORNER_OFFSET = 1 + 3 / 16F;
    private static final int SLOT_COUNT = 6;
    private static final float GEM_HEIGHT = 1.1F;
    private static final float GEM_SCALE = 0.6F;
    private static final float GEM_BOB = 0.04F;
    private static final float GEM_SWAY_DEGREES = 20.0F;

    private final ItemModelResolver itemModelResolver;

    public HellfireForgeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState[] items = new ItemStackRenderState[SLOT_COUNT];
        public final boolean[] present = new boolean[SLOT_COUNT];
        public float itemRotation;
        public float gemYaw;
        public float gemBob;

        public State() {
            for (int i = 0; i < SLOT_COUNT; i++) items[i] = new ItemStackRenderState();
        }
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(HellfireForgeBlockEntity forge, State s, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(forge, s, partialTick, cameraPos, crumbling);
        Level level = forge.getLevel();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = forge.inv.getStackInSlot(i);
            s.present[i] = !stack.isEmpty();
            if (s.present[i]) {
                this.itemModelResolver.updateForTopItem(s.items[i], stack, ItemDisplayContext.FIXED, level, null, i);
            } else {
                s.items[i].clear();
            }
        }
        s.itemRotation = 720.0F * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL;
        float seconds = (System.currentTimeMillis() % 3_600_000L) / 1000.0F;
        double dx = cameraPos.x - (forge.getBlockPos().getX() + 0.5);
        double dz = cameraPos.z - (forge.getBlockPos().getZ() + 0.5);
        s.gemYaw = (float) Math.toDegrees(Math.atan2(dx, dz)) + GEM_SWAY_DEGREES * (float) Math.sin(seconds * 1.5F);
        s.gemBob = GEM_BOB * (float) Math.sin(seconds * 2.0F);
    }

    @Override
    public void submit(State s, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        submitSlot(s, HellfireForgeBlockEntity.SOUTH, 1 / 16F, CORNER_OFFSET, 15 / 16F, poseStack, collector);
        submitSlot(s, HellfireForgeBlockEntity.WEST, 1 / 16F, CORNER_OFFSET, 1 / 16F, poseStack, collector);
        submitSlot(s, HellfireForgeBlockEntity.NORTH, 15 / 16F, CORNER_OFFSET, 1 / 16F, poseStack, collector);
        submitSlot(s, HellfireForgeBlockEntity.EAST, 15 / 16F, CORNER_OFFSET, 15 / 16F, poseStack, collector);
        submitGem(s, poseStack, collector);
        submitSlot(s, HellfireForgeBlockEntity.OUTPUT_SLOT, 0.5F, 1.5F, 0.5F, poseStack, collector);
    }

    private void submitGem(State s, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!s.present[HellfireForgeBlockEntity.GEM_SLOT]) return;
        poseStack.pushPose();
        poseStack.translate(0.5F, GEM_HEIGHT + s.gemBob, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(s.gemYaw));
        poseStack.scale(GEM_SCALE, GEM_SCALE, GEM_SCALE);
        s.items[HellfireForgeBlockEntity.GEM_SLOT].submit(poseStack, collector, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private void submitSlot(State s, int slotIndex, float x, float y, float z, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!s.present[slotIndex]) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(s.itemRotation));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        s.items[slotIndex].submit(poseStack, collector, s.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
