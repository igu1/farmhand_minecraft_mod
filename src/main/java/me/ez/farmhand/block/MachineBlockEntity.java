package me.ez.farmhand.block;

import me.ez.farmhand.menu.MachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Nine real slots shared by the coop and feeder. Menus synchronize inventory and state. */
public abstract class MachineBlockEntity extends BaseContainerBlockEntity {
    protected NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    protected boolean running = true;
    protected int cooldown;
    protected int operations;
    protected int status;
    public final ContainerData data = new ContainerData() {
        public int get(int index) {
            return switch (index) {
                case 0 -> kind();
                case 1 -> running ? 1 : 0;
                case 2 -> status;
                case 3 -> Math.min(operations, 32767);
                default -> 0;
            };
        }
        public void set(int index, int value) {}
        public int getCount() { return 4; }
    };

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract int kind();
    /** Redstone is a temporary override, never changes the saved GUI switch. */
    public boolean redstonePaused() { return level != null && level.hasNeighborSignal(worldPosition); }
    protected abstract int cycleLength();
    public void toggle() { running = !running; setChanged(); }
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    public int getContainerSize() { return 9; }
    protected NonNullList<ItemStack> getItems() { return items; }
    protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new MachineMenu(id, inventory, this, data);
    }

    public int roomFor(ItemStack stack) {
        int room = 0;
        for (int i = 0; i < 9; i++) {
            if (!canPlaceItem(i, stack)) continue;
            ItemStack current = items.get(i);
            if (current.isEmpty()) room += getMaxStackSize(stack);
            else if (ItemStack.isSameItemSameComponents(current, stack)) {
                room += Math.max(0, getMaxStackSize(stack) - current.getCount());
            }
        }
        return room;
    }

    /** Mutates only the remainder supplied by the caller; never deletes overflow. */
    public void store(ItemStack stack) {
        for (int i = 0; i < 9 && !stack.isEmpty(); i++) {
            if (!canPlaceItem(i, stack)) continue;
            ItemStack current = items.get(i);
            if (current.isEmpty()) items.set(i, stack.split(Math.min(stack.getCount(), getMaxStackSize(stack))));
            else if (ItemStack.isSameItemSameComponents(current, stack)) {
                int moved = Math.min(stack.getCount(), getMaxStackSize(stack) - current.getCount());
                if (moved > 0) { current.grow(moved); stack.shrink(moved); }
            }
        }
        setChanged();
    }

    public void dropContents() {
        if (level == null || level.isClientSide()) return;
        for (int i = 0; i < items.size(); i++) {
            Block.popResource(level, worldPosition, items.get(i));
            items.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putBoolean("Running", running);
        output.putInt("Operations", operations);
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(9, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        running = input.getBooleanOr("Running", running);
        operations = input.getIntOr("Operations", 0);
    }
}
