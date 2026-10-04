package me.ez.farmhand.client;

import java.util.ArrayList;
import java.util.List;
import me.ez.farmhand.menu.GrowpostMenu;
import me.ez.farmhand.network.PostLabelPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class GrowpostScreen extends AbstractContainerScreen<GrowpostMenu> {
    private EditBox label;
    private boolean helpers;
    private final List<Button> controls = new ArrayList<>();
    private final List<Button> toggles = new ArrayList<>();
    private final long opened = System.nanoTime();
    public GrowpostScreen(GrowpostMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, 300, 230); }
    protected void init() {
        super.init(); controls.clear(); toggles.clear();
        label = new EditBox(font, leftPos + 12, topPos + 34, 218, 18, Component.translatable("farmhand.growpost.label"));
        label.setMaxLength(32); label.setValue(title.getString()); addRenderableWidget(label);
        controls.add(addRenderableWidget(Button.builder(Component.translatable("farmhand.growpost.save"), b ->
                ClientPacketDistributor.sendToServer(new PostLabelPayload(menu.containerId, label.getValue())))
                .bounds(leftPos + 236, topPos + 34, 52, 18).build()));
        control("-", 224, 57, 0); control("+", 258, 57, 1);
        control("-", 224, 81, 2); control("+", 258, 81, 3);
        addRenderableWidget(Button.builder(Component.translatable("farmhand.growpost.alerts"), b -> { helpers = false; refresh(); })
                .bounds(leftPos + 12, topPos + 105, 135, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("farmhand.growpost.helpers"), b -> { helpers = true; refresh(); })
                .bounds(leftPos + 153, topPos + 105, 135, 18).build());
        for (int i = 0; i < 3; i++) {
            final int row = i;
            toggles.add(addRenderableWidget(Button.builder(Component.empty(), b -> send(helpers ? 7 + row : 4 + row))
                    .bounds(leftPos + 12, topPos + 130 + i * 22, 276, 20).build()));
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(leftPos + 238, topPos + 202, 50, 18).build());
        refresh();
    }
    private void control(String text, int x, int y, int action) {
        controls.add(addRenderableWidget(Button.builder(Component.literal(text), b -> send(action))
                .bounds(leftPos + x, topPos + y, 30, 18).build()));
    }
    private void send(int action) {
        if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
    }
    private void refresh() {
        label.setEditable(menu.editable());
        for (Button button : controls) button.active = menu.editable();
        for (int i = 0; i < toggles.size(); i++) {
            Button button = toggles.get(i);
            button.visible = !helpers || i < 2;
            button.active = menu.editable();
            int index = helpers ? 5 + i : 2 + i;
            button.setMessage(Component.translatable("farmhand.growpost.setting." + index)
                    .append(": ").append(Component.translatable(menu.value(index) == 1 ? "farmhand.growpost.on" : "farmhand.growpost.off")));
        }
    }
    protected void containerTick() { super.containerTick(); refresh(); }
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) { onClose(); return true; }
        return label.keyPressed(event) || label.canConsumeInput() || super.keyPressed(event);
    }
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        super.extractBackground(g, mouseX, mouseY, partial);
        int x = leftPos, y = topPos;
        double ease = 1 - Math.pow(1 - Math.min(1, (System.nanoTime() - opened) / 220_000_000.0), 3);
        int a = (int) (250 * ease);
        g.fill(x + 2, y + 3, x + 302, y + 232, 0x60000000);
        g.fill(x, y, x + 300, y + 230, a << 24 | 0x17271f);
        g.fill(x + 1, y + 1, x + 299, y + 27, a << 24 | 0x304638);
        g.fill(x + 1, y, x + 299, y + 2, a << 24 | 0xd2aa6d);
        g.fill(x + 12, y + 195, x + 288, y + 196, 0xff48654b);
    }
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font, Component.translatable("block.farmhand.growpost"), 12, 10, 0xfff2e9ce, false);
        g.text(font, Component.translatable(menu.editable() ? "farmhand.growpost.owner" : "farmhand.growpost.readonly"), 126, 10, 0xffb2c7ab, false);
        g.text(font, Component.translatable("farmhand.growpost.radius", menu.value(0)), 12, 62, 0xffd9dfc9, false);
        g.text(font, Component.translatable("farmhand.growpost.threshold", menu.value(1)), 12, 86, 0xffd9dfc9, false);
        g.text(font, Component.translatable("farmhand.growpost.readiness", menu.value(7), menu.value(8)), 12, 202, 0xffb7d88e, false);
        g.text(font, Component.translatable("farmhand.growpost.signal", menu.value(10)), 12, 215, 0xff9fb5a1, false);
    }
    public boolean isPauseScreen() { return false; }
}
