package me.ez.farmhand.client;

import me.ez.farmhand.Config;
import me.ez.farmhand.Init;
import me.ez.farmhand.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** No action-bar spam, no sounds; one quiet, held-item survey card. */
@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public final class HarvestHud {
    private static boolean visible;
    private static long opened;
    private static int previousReady;
    private static long lastAlert;
    private HarvestHud() {}
    @SubscribeEvent
    public static void layers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, Identifier.fromNamespaceAndPath(Main.MOD_ID, "harvest_survey"), (g, delta) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || mc.screen != null || mc.options.hideGui
                    || !Config.ENABLED.get() || !Config.SENTINEL_ENABLED.get()) { visible = false; return; }
            var stack = mc.player.getMainHandItem();
            if (!stack.is(Init.RESULT_SENTINEL.get())) stack = mc.player.getOffhandItem();
            if (!stack.is(Init.RESULT_SENTINEL.get())) { visible = false; return; }
            long now = System.nanoTime();
            if (!visible) { visible = true; opened = now; previousReady = -1; }
            double ease = 1 - Math.pow(1 - Math.min(1, (now - opened) / 250_000_000.0), 3);
            var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompoundOrEmpty("FarmhandSurvey");
            int ready = data.getIntOr("Ready", 0), total = data.getIntOr("Total", 0);
            if (previousReady >= 0 && ready > previousReady && now - lastAlert > 15_000_000_000L) lastAlert = now;
            previousReady = ready;
            double pulse = Math.max(0, 1 - (now - lastAlert) / 2_000_000_000.0);
            int w = Math.min(174, mc.getWindow().getGuiScaledWidth() - 16);
            int x = mc.getWindow().getGuiScaledWidth() - w - 8 + (int) ((1 - ease) * 12), y = 62;
            int a = (int) (230 * ease);
            g.fill(x + 1, y, x + w - 1, y + 62, a << 24 | 0x16261f);
            g.fill(x, y + 1, x + w, y + 61, a << 24 | 0x16261f);
            g.fill(x + 1, y, x + w - 1, y + 1, a << 24 | 0xd2aa6d);
            if (pulse > 0) g.fill(x + 1, y + 1, x + w - 1, y + 3, (int) (100 * pulse) << 24 | 0xb7d88e);
            g.item(stack, x + 8, y + 7);
            g.text(mc.font, Component.translatable("farmhand.survey.title"), x + 30, y + 9, 0xffeaddb9, false);
            g.text(mc.font, Component.translatable("farmhand.survey.ready", ready, total), x + 10, y + 28, 0xffb7d88e, false);
            g.fill(x + 10, y + 42, x + w - 10, y + 45, 0xff0e1912);
            if (total > 0) g.fill(x + 10, y + 42, x + 10 + (w - 20) * ready / total, y + 45, 0xffb7d88e);
            Component footer = total == 0 ? Component.translatable("farmhand.survey.none")
                    : ready == 0 ? Component.translatable("farmhand.survey.growing")
                    : Component.translatable("farmhand.survey.nearest", Math.round(Math.hypot(data.getIntOr("X", 0) - mc.player.getX(), data.getIntOr("Z", 0) - mc.player.getZ())));
            g.text(mc.font, footer, x + 10, y + 50, 0xff93ab96, false);
        });
    }
}
