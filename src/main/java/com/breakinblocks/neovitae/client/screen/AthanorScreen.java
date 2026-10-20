package com.breakinblocks.neovitae.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import com.breakinblocks.neovitae.common.blockentity.AthanorBlockEntity;
import com.breakinblocks.neovitae.common.blockentity.AthanorSides;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.menu.AthanorMenu;
import com.breakinblocks.neovitae.compat.jei.NeoVitaeJEIPlugin;
import com.breakinblocks.neovitae.compat.jei.athanor.AthanorRecipeCategory;
import com.breakinblocks.neovitae.util.helper.RenderHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Map;

public class AthanorScreen extends AbstractContainerScreen<AthanorMenu> {
    private final Identifier background = Identifier.fromNamespaceAndPath(NeoVitae.MODID, "textures/gui/container/athanor_gui.png");
    private final Identifier progress = Identifier.fromNamespaceAndPath(NeoVitae.MODID, "container/athanor/progress");
    private final Identifier gauge = Identifier.fromNamespaceAndPath(NeoVitae.MODID, "container/athanor/gauge");
    private static final Identifier BARS_TEXTURE = NeoVitae.rl("textures/hud/bars.png");

    private static final SpiritusType[] ORDERED_TYPES = {
            SpiritusType.RAW, SpiritusType.RUINA,
            SpiritusType.INVICTUS, SpiritusType.NIHILUM, SpiritusType.VINDICTA
    };
    private static final String[] TYPE_KEYS = {
            "spiritus.neovitae.raw",
            "spiritus.neovitae.ruina",
            "spiritus.neovitae.invictus",
            "spiritus.neovitae.nihilum",
            "spiritus.neovitae.vindicta"
    };

    private static final int GAUGE_X = 35;
    private static final int GAUGE_Y = 76;
    private static final int[] BAR_X_OFFSETS = {2, 0, 0, 0, 2};
    private static final int[] BAR_WIDTHS = {52, 56, 58, 56, 52};

    public AthanorScreen(AthanorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 208);
        this.titleLabelX = 38;
        this.inventoryLabelY = imageHeight - 94;
    }

    private int inputX;
    private int inputY;
    private int outputX;
    private int outputY;

    @Override
    protected void init() {
        super.init();
        this.inputX = leftPos + 8;
        this.inputY = topPos + 43;
        this.outputX = leftPos + 152;
        this.outputY = topPos + 18;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        if (!this.menu.tile.inputTank.isEmpty()) {
            int fluidHeight = 63 * this.menu.tile.inputTank.getFluidAmount() / this.menu.tile.inputTank.getCapacity();
            RenderHelper.renderGuiFluid(guiGraphics, this.menu.tile.inputTank.getFluid().getFluid(), inputX, inputY + (63 - fluidHeight), 16, fluidHeight);
        }
        if (!this.menu.tile.outputTank.isEmpty()) {
            int fluidHeight = 63 * this.menu.tile.outputTank.getFluidAmount() / this.menu.tile.outputTank.getCapacity();
            RenderHelper.renderGuiFluid(guiGraphics, this.menu.tile.outputTank.getFluid().getFluid(), outputX, outputY + (63 - fluidHeight), 16, fluidHeight);
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, gauge, inputX, inputY, 16, 57);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, gauge, outputX, outputY, 16, 57);

        renderSpiritusGauge(guiGraphics);
        renderSidePanel(guiGraphics, mouseX, mouseY);
    }

    private static final int SIDE_PANEL_WIDTH = 60;
    private static final int SIDE_PANEL_HEIGHT = 70;
    private static final int SIDE_CELL = 18;
    private static final int[][] SIDE_LAYOUT = {
            {1, 1}, {2, 2}, {0, 1}, {2, 1}, {1, 0}, {1, 2}
    };

    public int sidePanelX() {
        return leftPos + imageWidth + 2;
    }

    public int sidePanelY() {
        return topPos + 4;
    }

    public Rect2i sidePanelArea() {
        return new Rect2i(sidePanelX(), sidePanelY(), SIDE_PANEL_WIDTH, SIDE_PANEL_HEIGHT);
    }

    private int sideButtonX(AthanorSides.Face face) {
        return sidePanelX() + 4 + SIDE_LAYOUT[face.ordinal()][0] * SIDE_CELL;
    }

    private int sideButtonY(AthanorSides.Face face) {
        return sidePanelY() + 13 + SIDE_LAYOUT[face.ordinal()][1] * SIDE_CELL;
    }

    private AthanorSides.Face sideFaceAt(double mouseX, double mouseY) {
        for (AthanorSides.Face face : AthanorSides.Face.values()) {
            int bx = sideButtonX(face);
            int by = sideButtonY(face);
            if (mouseX >= bx && mouseX < bx + 16 && mouseY >= by && mouseY < by + 16) return face;
        }
        return null;
    }

    private static int sideModeColor(AthanorSides.Mode mode) {
        return switch (mode) {
            case INPUTS -> 0xFF3F76E4;
            case OUTPUTS -> 0xFFE07A2E;
            case BOTH -> 0xFF9B4FD1;
            case TOOL -> 0xFFD9B23A;
            default -> 0xFF2A2A2A;
        };
    }

    private static String sideModeLabel(AthanorSides.Mode mode) {
        return switch (mode) {
            case INPUTS -> "I";
            case OUTPUTS -> "O";
            case BOTH -> "IO";
            case TOOL -> "T";
            default -> "-";
        };
    }

    private void renderSidePanel(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int px = sidePanelX();
        int py = sidePanelY();
        guiGraphics.fill(px, py, px + SIDE_PANEL_WIDTH, py + SIDE_PANEL_HEIGHT, 0xFF000000);
        guiGraphics.fill(px + 1, py + 1, px + SIDE_PANEL_WIDTH - 1, py + SIDE_PANEL_HEIGHT - 1, 0xFF2B2228);
        guiGraphics.text(this.font, Component.translatable("gui.neovitae.athanor.sides"), px + 4, py + 3, 0xFFE0D0D0, false);
        for (AthanorSides.Face face : AthanorSides.Face.values()) {
            AthanorSides.Mode mode = menu.tile.getSideMode(face);
            boolean isDefault = mode == AthanorSides.Mode.DEFAULT;
            AthanorSides.Mode shown = isDefault ? face.defaultRole() : mode;
            int bx = sideButtonX(face);
            int by = sideButtonY(face);
            int color = sideModeColor(shown);
            if (isDefault) {
                color = 0xFF000000 | (((color >> 16) & 0xFF) * 45 / 100) << 16 | (((color >> 8) & 0xFF) * 45 / 100) << 8 | ((color & 0xFF) * 45 / 100);
            }
            boolean hovered = mouseX >= bx && mouseX < bx + 16 && mouseY >= by && mouseY < by + 16;
            guiGraphics.fill(bx - 1, by - 1, bx + 17, by + 17, hovered ? 0xFFFFFFFF : 0xFF000000);
            guiGraphics.fill(bx, by, bx + 16, by + 16, color);
            String label = sideModeLabel(shown);
            guiGraphics.text(this.font, label, bx + 8 - this.font.width(label) / 2, by + 4, isDefault ? 0xFFB0B0B0 : 0xFFFFFFFF, true);
        }
    }

    private Component sideModeName(AthanorSides.Face face) {
        AthanorSides.Mode mode = menu.tile.getSideMode(face);
        if (mode == AthanorSides.Mode.DEFAULT) {
            return Component.translatable("gui.neovitae.athanor.side_mode.default",
                    Component.translatable("gui.neovitae.athanor.side_mode." + face.defaultRole().getName()));
        }
        return Component.translatable("gui.neovitae.athanor.side_mode." + mode.getName());
    }

    private void renderSpiritusGauge(GuiGraphicsExtractor guiGraphics) {
        Map<SpiritusType, Double> costs = menu.tile.getCurrentRecipeSpiritusCost();
        int gx = leftPos + GAUGE_X;
        int gy = topPos + GAUGE_Y;

        for (int idx = 0; idx < ORDERED_TYPES.length; idx++) {
            SpiritusType type = ORDERED_TYPES[idx];
            int i = idx + 1;

            double current = menu.tile.getChunkSpiritus(type);
            double max = menu.tile.getChunkSpiritusMax(type);
            if (max <= 0) max = 100.0;
            double ratio = Math.max(0, Math.min(1, current / max));

            int fullBarWidth = BAR_WIDTHS[idx];
            int barX = gx + BAR_X_OFFSETS[idx];
            int barY = gy + 4 * i;
            int barHeight = 2;

            int textureXOffset = (i > 3) ? (i - 3) : (3 - i);
            int textureX = 2 * textureXOffset + 84;
            int textureY = 4 * i + 220;

            int fillWidth = (int) (fullBarWidth * ratio);
            if (fillWidth > 0) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BARS_TEXTURE, barX, barY, (float) textureX, (float) textureY, fillWidth, barHeight, 256, 256);
            }

            Double required = costs.get(type);
            if (required != null && required > 0) {
                int requiredWidth = (int) (fullBarWidth * Math.min(1.0, required / max));
                if (current < required) {
                    guiGraphics.fill(barX + fillWidth, barY, barX + requiredWidth, barY + barHeight, 0xAAFF2222);
                }
                int markerX = barX + requiredWidth;
                guiGraphics.fill(markerX, barY - 1, markerX + 1, barY + barHeight + 1, 0xFFFFFFFF);
            }
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor guiGraphics, int x, int y) {
        super.extractTooltip(guiGraphics, x, y);

        AthanorSides.Face hoveredFace = sideFaceAt(x, y);
        if (hoveredFace != null) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.translatable("gui.neovitae.athanor.face_mode",
                    Component.translatable("gui.neovitae.athanor.face." + hoveredFace.getName()), sideModeName(hoveredFace)));
            tip.add(Component.translatable("gui.neovitae.athanor.side_hint").withStyle(ChatFormatting.DARK_GRAY));
            guiGraphics.setComponentTooltipForNextFrame(this.font, tip, x, y);
            return;
        }

        if (x > inputX && x < inputX + 16 && y > inputY && y < inputY + 63) {
            List<Component> tip = new ArrayList<>();
            if (!this.menu.tile.inputTank.isEmpty()) {
                tip.add(this.menu.tile.inputTank.getFluid().getHoverName());
                tip.add(Component.translatable("gui.neovitae.athanor.tank_amount", this.menu.tile.inputTank.getFluidAmount(), this.menu.tile.inputTank.getCapacity()));
            } else {
                tip.add(Component.translatable("gui.neovitae.athanor.slot.empty"));
            }
            guiGraphics.setComponentTooltipForNextFrame(this.font, tip, x, y);
        }

        if (x > outputX && x < outputX + 16 && y > outputY && y < outputY + 63) {
            List<Component> tip = new ArrayList<>();
            if (!this.menu.tile.outputTank.isEmpty()) {
                tip.add(this.menu.tile.outputTank.getFluid().getHoverName());
                tip.add(Component.translatable("gui.neovitae.athanor.tank_amount", this.menu.tile.outputTank.getFluidAmount(), this.menu.tile.outputTank.getCapacity()));
            } else {
                tip.add(Component.translatable("gui.neovitae.athanor.slot.empty"));
            }
            guiGraphics.setComponentTooltipForNextFrame(this.font, tip, x, y);
        }

        if (this.hoveredSlot != null && !this.hoveredSlot.hasItem() && this.hoveredSlot.index <= 13) {
            int slotIdx = this.hoveredSlot.index;
            if (slotIdx == 0) {
                guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("gui.neovitae.athanor.slot.tool").withStyle(ChatFormatting.GRAY), x, y);
            } else if (slotIdx >= 1 && slotIdx <= 6) {
                guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("gui.neovitae.athanor.slot.input").withStyle(ChatFormatting.GRAY), x, y);
            } else if (slotIdx == 7) {
                guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("gui.neovitae.athanor.slot.fluid_input").withStyle(ChatFormatting.GRAY), x, y);
            } else if (slotIdx == 8) {
                guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("gui.neovitae.athanor.slot.fluid_output").withStyle(ChatFormatting.GRAY), x, y);
            } else if (slotIdx >= 9 && slotIdx <= 13) {
                guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("gui.neovitae.athanor.slot.output").withStyle(ChatFormatting.GRAY), x, y);
            }
        }

        if (isOverProgressArrow(x, y)) {
            List<Component> lines = new ArrayList<>();
            AthanorBlockEntity.IdleReason reason = menu.getIdleReason();
            if (reason != AthanorBlockEntity.IdleReason.NONE) {
                lines.add(Component.translatable("gui.neovitae.athanor.idle." + reason.name().toLowerCase(Locale.ROOT))
                        .withStyle(ChatFormatting.RED));
            }
            lines.add(Component.translatable("gui.neovitae.show_recipes").withStyle(ChatFormatting.YELLOW));
            guiGraphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), x, y);
        }

        Map<SpiritusType, Double> costs = menu.tile.getCurrentRecipeSpiritusCost();
        int gx = leftPos + GAUGE_X;
        int gy = topPos + GAUGE_Y;
        for (int idx = 0; idx < ORDERED_TYPES.length; idx++) {
            SpiritusType type = ORDERED_TYPES[idx];
            int i = idx + 1;
            int fullBarWidth = BAR_WIDTHS[idx];
            int barX = gx + BAR_X_OFFSETS[idx];
            int barY = gy + 4 * i;

            if (x >= barX && x <= barX + fullBarWidth && y >= barY - 1 && y <= barY + 3) {
                double current = menu.tile.getChunkSpiritus(type);
                double max = menu.tile.getChunkSpiritusMax(type);
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.translatable("gui.neovitae.athanor.type_spiritus", Component.translatable(TYPE_KEYS[idx])));
                tooltip.add(Component.translatable("gui.neovitae.athanor.spiritus_progress", String.format("%.1f", current), String.format("%.1f", max)));
                Double required = costs.get(type);
                if (required != null && required > 0) {
                    tooltip.add(Component.translatable("gui.neovitae.athanor.spiritus_required", String.format("%.1f", required)));
                    if (current < required) {
                        tooltip.add(Component.translatable("gui.neovitae.athanor.insufficient").withStyle(style -> style.withColor(0xFF5555)));
                    }
                }
                guiGraphics.setComponentTooltipForNextFrame(this.font, tooltip, x, y);
                break;
            }
        }
    }

    private boolean isOverProgressArrow(double mouseX, double mouseY) {
        int ax = leftPos + 63;
        int ay = topPos + 51;
        return mouseX >= ax && mouseX < ax + 38 && mouseY >= ay && mouseY < ay + 23;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean dragging) {
        AthanorSides.Face clickedFace = sideFaceAt(event.x(), event.y());
        if (clickedFace != null && (event.button() == 0 || event.button() == 1) && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId,
                    AthanorMenu.sideButtonId(clickedFace, event.button() == 1));
            return true;
        }
        if (event.button() == 0 && isOverProgressArrow(event.x(), event.y())) {
            var runtime = NeoVitaeJEIPlugin.jeiRuntime;
            if (runtime != null) {
                runtime.getRecipesGui().showTypes(List.of(AthanorRecipeCategory.RECIPE_TYPE));
                return true;
            }
        }
        return super.mouseClicked(event, dragging);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256);
        int progressWidth = menu.tile.getProgressForGui();
        if (progressWidth > 0) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, progress, 38, 23, 0, 4, leftPos + 63, topPos + 51, progressWidth, 19);
        }
    }
}
