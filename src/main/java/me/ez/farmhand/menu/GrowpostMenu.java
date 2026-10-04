package me.ez.farmhand.menu;

import me.ez.farmhand.Init;
import me.ez.farmhand.block.GrowpostBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/** Configuration-only menu: deliberately has no fake inventory or decorative progress. */
public class GrowpostMenu extends AbstractContainerMenu {
    private final GrowpostBlockEntity post;
    private final ContainerData data;
    public GrowpostMenu(int id, Inventory inventory) { this(id, inventory, null); }
    public GrowpostMenu(int id, Inventory inventory, GrowpostBlockEntity post) {
        super(Init.GROWPOST_MENU.get(), id);
        this.post = post;
        this.data = post == null ? new SimpleContainerData(11) : new ContainerData() {
            public int get(int index) { return index == 9 ? (post.canEdit(inventory.player) ? 1 : 0) : post.setting(index); }
            public void set(int index, int value) {}
            public int getCount() { return 11; }
        };
        addDataSlots(data);
    }
    public int value(int index) { return data.get(index); }
    public boolean editable() { return value(9) == 1; }
    public boolean stillValid(Player player) { return post == null || Container.stillValidBlockEntity(post, player); }
    public boolean clickMenuButton(Player player, int button) {
        if (post == null || !stillValid(player) || !post.canEdit(player) || button < 0 || button > 8) return false;
        post.edit(button); broadcastChanges(); return true;
    }
    public void rename(Player player, String label) {
        if (post != null && stillValid(player) && post.canEdit(player)) post.setLabel(label);
    }
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
