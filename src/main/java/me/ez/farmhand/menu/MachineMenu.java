package me.ez.farmhand.menu;

import me.ez.farmhand.Init;
import me.ez.farmhand.block.AnimalFeederBlockEntity;
import me.ez.farmhand.block.MachineBlockEntity;
import me.ez.farmhand.util.FarmUtil;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MachineMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData data;

    public MachineMenu(int id, Inventory player) { this(id, player, new SimpleContainer(9), new SimpleContainerData(4)); }
    public MachineMenu(int id, Inventory player, Container inventory, ContainerData data) {
        super(Init.MACHINE_MENU.get(), id);
        this.inventory = inventory;
        this.data = data;
        checkContainerSize(inventory, 9);
        checkContainerDataCount(data, 4);
        addDataSlots(data);
        for (int i = 0; i < 9; i++) {
            final int index = i;
            addSlot(new Slot(inventory, i, 8 + i * 18, 66) {
                public boolean mayPlace(ItemStack stack) {
                    boolean accepted = switch (kind()) {
                        case 0 -> stack.is(Items.EGG) || stack.is(Items.FEATHER);
                        case 1 -> AnimalFeederBlockEntity.isAnimalFood(stack);
                        default -> FarmUtil.cropFor(stack) != null;
                    };
                    return accepted && inventory.canPlaceItem(index, stack);
                }
            });
        }
        addStandardInventorySlots(player, 8, 116);
    }
    public int kind() { return data.get(0); }
    public boolean running() { return data.get(1) != 0; }
    public int status() { return data.get(2); }
    public int operations() { return data.get(3); }
    public boolean stillValid(Player player) { return inventory.stillValid(player); }
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(inventory instanceof PouchInventory pouch && pouch.owns(slot.getItem()))
                && super.canTakeItemForPickAll(stack, slot);
    }
    public boolean clickMenuButton(Player player, int button) {
        if (button != 0 || !stillValid(player) || !(inventory instanceof MachineBlockEntity machine)) return false;
        machine.toggle();
        broadcastChanges();
        return true;
    }
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (inventory instanceof PouchInventory pouch && pouch.owns(slot.getItem())) return ItemStack.EMPTY;
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (!moveItemStackTo(stack, index < 9 ? 9 : 0, index < 9 ? slots.size() : 9, index < 9)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ContainerInput type, Player player) {
        if (inventory instanceof PouchInventory pouch) {
            if (slotId >= 0 && slotId < slots.size() && pouch.owns(slots.get(slotId).getItem())) return;
            if (type == net.minecraft.world.inventory.ContainerInput.SWAP
                    && (button == 40 ? pouch.owns(player.getOffhandItem())
                    : button >= 0 && button < 9 && pouch.owns(player.getInventory().getItem(button)))) return;
        }
        super.clicked(slotId, button, type, player);
    }
}
