package me.ez.farmhand;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "farmhand";

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        // Blocks register before items: block items wrap block holders.
        Init.BLOCKS.register(modEventBus);
        Init.BLOCK_ENTITIES.register(modEventBus);
        Init.CREATIVE_TABS.register(modEventBus);
        Init.ITEMS.register(modEventBus);
        Init.MENUS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
