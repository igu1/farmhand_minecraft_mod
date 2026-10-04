package me.ez.farmhand.client;

import me.ez.farmhand.Init;
import me.ez.farmhand.Main;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}
    @SubscribeEvent
    public static void screens(RegisterMenuScreensEvent event) {
        event.register(Init.MACHINE_MENU.get(), MachineInventoryScreen::new);
    }
}
