package me.ez.farmhand.client;

import me.ez.farmhand.menu.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Actual synchronized machine/pouch inventory, not a help dialog. */
public final class MachineInventoryScreen extends AbstractContainerScreen<MachineMenu> {
    private Button toggle;
    private final long opened = System.nanoTime();

    public MachineInventoryScreen(MachineMenu menu, Inventory player, Component title) {
        super(menu, player, title, 176, 212);
        inventoryLabelY = 104;
    }

    protected void init() {
        super.init();
        toggle = addRenderableWidget(new ToggleButton(leftPos + 94, topPos + 36, button -> {
            if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        }));
        updateButton();
    }

    private void updateButton() {
        toggle.visible = menu.kind() != 2;
        toggle.setMessage(Component.translatable("farmhand.menu." + (menu.kind() == 0 ? "hatch" : "feed")
                + (menu.running() ? ".on" : ".off")));
    }

    protected void containerTick() {
        super.containerTick();
        updateButton();
    }

    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        super.extractBackground(g, mouseX, mouseY, partial);
        int x = leftPos, y = topPos;
        double ease = 1 - Math.pow(1 - Math.min(1, (System.nanoTime() - opened) / 220_000_000.0), 3);
        int a = (int) (255 * ease);
        g.fill(x + 2, y + 2, x + 178, y + 214, 0x60000000);
        g.fill(x, y, x + 176, y + 212, a << 24 | 0x17271f);
        g.fill(x + 1, y + 1, x + 175, y + 32, a << 24 | 0x304638);
        g.fill(x + 1, y, x + 175, y + 2, a << 24 | 0xd2aa6d);
        g.fill(x, y + 2, x + 1, y + 211, a << 24 | 0x53694e);
        g.fill(x + 175, y + 2, x + 176, y + 211, a << 24 | 0x53694e);
        g.fill(x + 1, y + 211, x + 175, y + 212, a << 24 | 0x53694e);
        for (int i = 0; i < 9; i++) slot(g, x + 8 + i * 18, y + 66);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) slot(g, x + 8 + col * 18, y + 116 + row * 18);
        for (int col = 0; col < 9; col++) slot(g, x + 8 + col * 18, y + 174);
    }

    private static void slot(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, 0xff54715a);
        g.fill(x, y, x + 16, y + 16, 0xff101d16);
        g.fill(x, y + 15, x + 16, y + 16, 0xff385341);
    }

    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font, title, 8, 9, 0xfff2e9ce, false);
        g.text(font, Component.translatable("farmhand.menu.subtitle." + menu.kind()), 8, 22, 0xffb2c7ab, false);
        g.text(font, Component.translatable(menu.kind() == 2 ? "farmhand.menu.seeds" : "farmhand.menu.storage"), 8, 43, 0xffc7d1b7, false);
        g.text(font, playerInventoryTitle, 8, 104, 0xffb2c7ab, false);
        if (menu.kind() == 2) g.text(font, Component.translatable("farmhand.menu.plant_hint"), 8, 89, 0xffa6c67c, false);
        else if (menu.kind() == 0) g.text(font, Component.translatable("farmhand.menu.coop_status." + menu.status()), 8, 89, 0xffa6c67c, false);
        else g.text(font, Component.translatable("farmhand.menu.feeder_hint"), 8, 89, 0xffa6c67c, false);
        g.text(font, Component.translatable("farmhand.menu.shift_hint"), 8, 199, 0xff819a83, false);
    }

    public boolean isPauseScreen() { return false; }

    private final class ToggleButton extends Button {
        private float hover;
        private long frame = System.nanoTime();
        ToggleButton(int x, int y, OnPress action) {
            super(x, y, 74, 20, Component.empty(), action, DEFAULT_NARRATION);
        }
        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
            long now = System.nanoTime();
            float step = Math.min(1, (now - frame) / 100_000_000f);
            frame = now;
            hover += ((isHoveredOrFocused() ? 1 : 0) - hover) * step;
            int x = getX(), y = getY();
            g.fill(x + 1, y, x + 73, y + 20, 0xff557151);
            g.fill(x, y + 1, x + 74, y + 19, 0xff557151);
            int shade = 35 + (int) (hover * 18);
            g.fill(x + 1, y + 1, x + 73, y + 19, 0xff000000 | shade << 16 | (shade + 20) << 8 | shade);
            g.fill(x + 3, y + 8, x + 7, y + 12, menu.running() ? 0xffbadb83 : 0xff9b8c70);
            g.text(font, getMessage(), x + 11, y + 6, 0xffeee3c5, false);
        }
    }
}
