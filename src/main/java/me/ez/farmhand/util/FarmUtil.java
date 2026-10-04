package me.ez.farmhand.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Small shared helpers for the Farmhand machines and items. */
public final class FarmUtil {

    private FarmUtil() {}

    /**
     * Counts fully-grown crops around {@code center}.
     *
     * @return {@code [readyCount, nearestX, nearestY, nearestZ, hasNearest]}
     */
    public static int[] scanCrops(ServerLevel level, BlockPos center, int radius) {
        int count = 0;
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                count++;
                double dx = pos.getX() - center.getX();
                double dy = pos.getY() - center.getY();
                double dz = pos.getZ() - center.getZ();
                double distance = dx * dx + dy * dy + dz * dz;
                if (distance < best) {
                    best = distance;
                    nearest = pos.immutable();
                }
            }
        }
        if (nearest == null) {
            return new int[] {count, 0, 0, 0, 0};
        }
        return new int[] {count, nearest.getX(), nearest.getY(), nearest.getZ(), 1};
    }

    /** The first plantable seed (a block item for a crop) in the player's inventory. */
    public static ItemStack findSeed(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof CropBlock) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Moves a stack into the player's inventory, dropping whatever does not fit. */
    public static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
