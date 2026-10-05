package me.ez.farmhand;

import me.ez.farmhand.block.AnimalFeederBlock;
import me.ez.farmhand.block.AnimalFeederBlockEntity;
import me.ez.farmhand.block.ChickenCoopBlock;
import me.ez.farmhand.block.ChickenCoopBlockEntity;
import me.ez.farmhand.block.GrowthLampBlock;
import me.ez.farmhand.block.GrowthLampBlockEntity;
import me.ez.farmhand.block.GrowpostBlock;
import me.ez.farmhand.block.GrowpostBlockEntity;
import me.ez.farmhand.menu.GrowpostMenu;
import me.ez.farmhand.item.SeedPouchItem;
import me.ez.farmhand.menu.MachineMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All Farmhand registry entries. */
public final class Init {

    private Init() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Main.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Main.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Main.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Main.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MACHINE_MENU =
            MENUS.register("machine", () -> new MenuType<>(MachineMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<GrowpostMenu>> GROWPOST_MENU =
            MENUS.register("growpost", () -> new MenuType<>(GrowpostMenu::new, FeatureFlags.DEFAULT_FLAGS));

    // ---- Blocks -------------------------------------------------------------
    public static final DeferredBlock<GrowpostBlock> GROWPOST = BLOCKS.registerBlock(
            "growpost", GrowpostBlock::new, props -> props.strength(2.0f).sound(SoundType.WOOD).noOcclusion().forceSolidOff());
    public static final DeferredItem<net.minecraft.world.item.BlockItem> GROWPOST_ITEM = ITEMS.registerSimpleBlockItem("growpost", GROWPOST);

    public static final DeferredBlock<GrowthLampBlock> GROWTH_LAMP = BLOCKS.registerBlock(
            "growth_lamp", GrowthLampBlock::new,
            props -> props.strength(1.5f).sound(SoundType.GLASS)
                    .lightLevel(state -> state.getValue(GrowthLampBlock.LIT) ? 15 : 0).noOcclusion().forceSolidOff());

    public static final DeferredBlock<ChickenCoopBlock> CHICKEN_COOP = BLOCKS.registerBlock(
            "chicken_coop", ChickenCoopBlock::new,
            props -> props.strength(2.5f).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<AnimalFeederBlock> ANIMAL_FEEDER = BLOCKS.registerBlock(
            "animal_feeder", AnimalFeederBlock::new,
            props -> props.strength(2.0f).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredItem<net.minecraft.world.item.BlockItem> GROWTH_LAMP_ITEM =
            ITEMS.registerSimpleBlockItem("growth_lamp", GROWTH_LAMP);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> CHICKEN_COOP_ITEM =
            ITEMS.registerSimpleBlockItem("chicken_coop", CHICKEN_COOP);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> ANIMAL_FEEDER_ITEM =
            ITEMS.registerSimpleBlockItem("animal_feeder", ANIMAL_FEEDER);

    // ---- Items --------------------------------------------------------------

    public static final DeferredItem<SeedPouchItem> SEED_POUCH = ITEMS.registerItem(
            "seed_pouch", SeedPouchItem::new, props -> props.stacksTo(1));

    // ---- Block entities -----------------------------------------------------
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrowpostBlockEntity>> GROWPOST_BE =
            BLOCK_ENTITIES.register("growpost", () -> new BlockEntityType<>(GrowpostBlockEntity::new, GROWPOST.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrowthLampBlockEntity>> GROWTH_LAMP_BE =
            BLOCK_ENTITIES.register("growth_lamp",
                    () -> new BlockEntityType<>(GrowthLampBlockEntity::new, GROWTH_LAMP.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChickenCoopBlockEntity>> CHICKEN_COOP_BE =
            BLOCK_ENTITIES.register("chicken_coop",
                    () -> new BlockEntityType<>(ChickenCoopBlockEntity::new, CHICKEN_COOP.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnimalFeederBlockEntity>> ANIMAL_FEEDER_BE =
            BLOCK_ENTITIES.register("animal_feeder",
                    () -> new BlockEntityType<>(AnimalFeederBlockEntity::new, ANIMAL_FEEDER.get()));

    // ---- Creative tab -------------------------------------------------------

    /** A dedicated Farmhand tab keeps the farm machines and satchel together. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register("farmhand",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.farmhand"))
                    .icon(() -> new ItemStack(GROWTH_LAMP_ITEM.get()))
                    .displayItems(Init::addTabContents)
                    .build());

    private static void addTabContents(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept(GROWTH_LAMP_ITEM.get());
        output.accept(CHICKEN_COOP_ITEM.get());
        output.accept(ANIMAL_FEEDER_ITEM.get());
        output.accept(GROWPOST_ITEM.get());
        output.accept(SEED_POUCH.get());
    }
}
