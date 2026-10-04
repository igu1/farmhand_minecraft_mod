package me.ez.farmhand.menu;

import me.ez.farmhand.util.FarmUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/** Component-backed portable inventory; contents persist on the actual pouch stack. */
public final class PouchInventory extends SimpleContainer {
    private final ItemStack pouch;
    public PouchInventory(ItemStack pouch) {
        super(9);
        this.pouch = pouch;
        pouch.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(getItems());
    }
    public boolean owns(ItemStack stack) { return stack == pouch; }
    public boolean canPlaceItem(int index, ItemStack stack) { return FarmUtil.cropFor(stack) != null; }
    public void setChanged() { pouch.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(getItems())); }
    public boolean stillValid(Player player) { return player.getMainHandItem() == pouch || player.getOffhandItem() == pouch; }
}
