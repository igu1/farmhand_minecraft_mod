package me.ez.farmhand.client;

import me.ez.farmhand.Init;
import me.ez.farmhand.Main;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}
    @SubscribeEvent
    public static void screens(RegisterMenuScreensEvent event) {
        event.register(Init.MACHINE_MENU.get(), MachineInventoryScreen::new);
        event.register(Init.GROWPOST_MENU.get(), GrowpostScreen::new);
    }
    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Init.CHICKEN_COOP_BE.get(), MachineContentsRenderer::new);
        event.registerBlockEntityRenderer(Init.ANIMAL_FEEDER_BE.get(), MachineContentsRenderer::new);
    }
}
