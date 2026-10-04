package me.ez.farmhand;

import me.ez.farmhand.menu.PouchInventory;
import me.ez.farmhand.util.FarmUtil;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import me.ez.farmhand.block.ChickenCoopBlockEntity;
import me.ez.farmhand.block.AnimalFeederBlockEntity;
import me.ez.farmhand.menu.MachineMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerInput;

/** Dependency-free regression runner for crop recognition and component-backed seed storage. */
@ExtendWith(EphemeralTestServerProvider.class)
public final class FarmhandRegressionTest {
    @Test
    public void lampPowerAndInventoryRenderSync(MinecraftServer server) {
        var lamp = Init.GROWTH_LAMP.get().defaultBlockState();
        check(lamp.getValue(me.ez.farmhand.block.GrowthLampBlock.LIT), "new lamps illuminate");
        check(lamp.getValue(me.ez.farmhand.block.GrowthLampBlock.ENABLED), "new lamps enabled");
        var off = lamp.setValue(me.ez.farmhand.block.GrowthLampBlock.LIT, false)
                .setValue(me.ez.farmhand.block.GrowthLampBlock.ENABLED, false);
        check(off.getLightEmission() == 0 && lamp.getLightEmission() == 15, "lamp light follows actual lit state");
        var coop = new ChickenCoopBlockEntity(BlockPos.ZERO, Init.CHICKEN_COOP.get().defaultBlockState());
        coop.store(new ItemStack(Items.EGG, 7));
        var tag = coop.getUpdateTag(server.registryAccess());
        var clientCopy = new ChickenCoopBlockEntity(BlockPos.ZERO, coop.getBlockState());
        clientCopy.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, server.registryAccess(), tag));
        check(clientCopy.getItem(0).is(Items.EGG) && clientCopy.getItem(0).getCount() == 7, "render sync contains actual egg inventory");
        coop.clearContent();
        coop.setChanged();
        clientCopy.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, server.registryAccess(), coop.getUpdateTag(server.registryAccess())));
        check(clientCopy.isEmpty(), "render sync clears removed inventory items");
    }
    @Test
    public void growpostSettingsPersist(MinecraftServer server) {
        var state = Init.GROWPOST.get().defaultBlockState();
        var post = new me.ez.farmhand.block.GrowpostBlockEntity(BlockPos.ZERO, state);
        post.setLabel("North Wheat");
        post.edit(1); post.edit(3); post.edit(4); post.edit(7); post.edit(8);
        var loaded = (me.ez.farmhand.block.GrowpostBlockEntity) BlockEntity.loadStatic(BlockPos.ZERO, state,
                post.saveWithFullMetadata(server.registryAccess()), server.registryAccess());
        check(loaded != null && loaded.label().equals("North Wheat"), "Growpost label survives reload");
        check(loaded.setting(0) == 9 && loaded.setting(1) == 55, "per-post radius and threshold survive reload");
        check(loaded.setting(2) == 0 && loaded.setting(5) == 0 && loaded.setting(6) == 0, "alerts and helper settings survive reload");
        check(!state.isSolid(), "Growpost must not turn its farmland support into dirt");
        check(loaded.owner() == null, "loading does not silently assign an owner");
        check(!loaded.canEdit(null), "unowned post cannot authorize arbitrary editing");
    }
    private static int checks;
    @Test
    public void cropRecognitionAndPouchPersistence(MinecraftServer server) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        check(FarmUtil.cropFor(new ItemStack(Items.WHEAT_SEEDS)) == Blocks.WHEAT, "wheat seed");
        check(FarmUtil.cropFor(new ItemStack(Items.BEETROOT_SEEDS)) == Blocks.BEETROOTS, "beetroot seed");
        check(FarmUtil.cropFor(new ItemStack(Items.CARROT)) == Blocks.CARROTS, "carrot");
        check(FarmUtil.cropFor(new ItemStack(Items.POTATO)) == Blocks.POTATOES, "potato");
        check(FarmUtil.cropFor(new ItemStack(Items.MELON_SEEDS)) == Blocks.MELON_STEM, "melon seed");
        check(FarmUtil.cropFor(new ItemStack(Items.PUMPKIN_SEEDS)) == Blocks.PUMPKIN_STEM, "pumpkin seed");
        check(FarmUtil.cropFor(new ItemStack(Items.WHEAT)) == null, "wheat is not a seed");
        check(FarmUtil.cropFor(new ItemStack(Items.DIRT)) == null, "dirt is not a seed");
        check(FarmUtil.cropFor(new ItemStack(Items.SWEET_BERRIES)) == null, "berries cannot grow on farmland");
        ItemStack pouch = new ItemStack(Items.BUNDLE);
        PouchInventory inventory = new PouchInventory(pouch);
        check(inventory.getContainerSize() == 9, "nine persistent slots");
        inventory.setItem(0, new ItemStack(Items.WHEAT_SEEDS, 32));
        check(new PouchInventory(pouch).getItem(0).getCount() == 32, "save and reopen seed storage");
        inventory.removeItem(0, 7);
        check(new PouchInventory(pouch).getItem(0).getCount() == 25, "removal is persisted");
        inventory.getItem(0).shrink(9);
        inventory.setChanged();
        check(new PouchInventory(pouch).getItem(0).getCount() == 16, "plant consumption is persisted");
        check(!inventory.canPlaceItem(1, new ItemStack(Items.BUNDLE)), "nested containers rejected");
        check(inventory.canPlaceItem(1, new ItemStack(Items.MELON_SEEDS)), "stem seeds accepted");
        check(inventory.owns(pouch) && !inventory.owns(pouch.copy()), "pouch identity lock");
        System.out.println("Passed " + checks + " Farmhand regressions.");
    }
    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
        checks++;
    }

    @Test
    public void machineInventoryToggleAndPersistence(MinecraftServer server) {
        var coopState = Init.CHICKEN_COOP.get().defaultBlockState();
        var coop = new ChickenCoopBlockEntity(BlockPos.ZERO, coopState);
        ItemStack eggs = new ItemStack(Items.EGG, 64);
        coop.store(eggs);
        check(eggs.isEmpty(), "all legacy eggs fit without loss");
        int stored = 0;
        for (int i = 0; i < 9; i++) {
            check(coop.getItem(i).getCount() <= 16, "egg slots respect vanilla stack limit");
            stored += coop.getItem(i).getCount();
        }
        check(stored == 64, "stored egg count conserved");
        coop.toggle();
        check(coop.data.get(1) == 0, "hatching toggle switches off");
        var loaded = (ChickenCoopBlockEntity) BlockEntity.loadStatic(BlockPos.ZERO, coopState,
                coop.saveWithFullMetadata(server.registryAccess()), server.registryAccess());
        check(loaded != null && loaded.data.get(1) == 0, "hatching toggle survives reload");
        int after = 0;
        for (int i = 0; i < 9; i++) after += loaded.getItem(i).getCount();
        check(after == 64, "egg inventory survives reload");
        var feeder = new AnimalFeederBlockEntity(BlockPos.ZERO, Init.ANIMAL_FEEDER.get().defaultBlockState());
        check(feeder.canPlaceItem(0, new ItemStack(Items.WHEAT)), "breeding food accepted with loaded tags");
        check(feeder.canPlaceItem(1, new ItemStack(Items.CARROT)), "mixed breeding foods accepted");
        check(!feeder.canPlaceItem(2, new ItemStack(Items.DIRT)), "nonfood rejected");
        feeder.store(new ItemStack(Items.WHEAT, 64));
        feeder.store(new ItemStack(Items.CARROT, 16));
        check(feeder.getItem(0).is(Items.WHEAT) && feeder.getItem(1).is(Items.CARROT), "mixed inventory stores separately");
    }

    @Test
    public void inventoryMenusTransferAndProtectPouch(MinecraftServer server) {
        var playerInventory = new Inventory(null, new EntityEquipment());
        var feeder = new AnimalFeederBlockEntity(BlockPos.ZERO, Init.ANIMAL_FEEDER.get().defaultBlockState());
        var menu = new MachineMenu(1, playerInventory, feeder, feeder.data);
        check(menu.slots.size() == 45, "nine machine slots plus 36 player slots");
        check(menu.slots.get(0).mayPlace(new ItemStack(Items.WHEAT)), "GUI accepts breeding food");
        check(!menu.slots.get(0).mayPlace(new ItemStack(Items.DIRT)), "GUI rejects invalid input");
        playerInventory.setItem(9, new ItemStack(Items.WHEAT, 32));
        check(!menu.quickMoveStack(null, 9).isEmpty(), "shift-click from player inventory");
        check(feeder.getItem(0).getCount() == 32 && playerInventory.getItem(9).isEmpty(), "input transfer conserves items");
        check(!menu.quickMoveStack(null, 0).isEmpty(), "shift-click back from machine");
        check(feeder.isEmpty() && playerInventory.getItem(8).getCount() == 32, "output transfer conserves items");
        ItemStack pouch = new ItemStack(Items.BUNDLE);
        playerInventory.setItem(0, pouch);
        var storage = new PouchInventory(pouch);
        var data = new SimpleContainerData(4);
        data.set(0, 2);
        var pouchMenu = new MachineMenu(2, playerInventory, storage, data);
        check(pouchMenu.quickMoveStack(null, 36).isEmpty(), "open pouch cannot be shift-moved");
        pouchMenu.clicked(36, 0, ContainerInput.PICKUP, null);
        check(playerInventory.getItem(0) == pouch, "open pouch cannot be picked up");
        check(!pouchMenu.slots.get(0).mayPlace(pouch), "pouch GUI refuses nested pouches");
    }
}
