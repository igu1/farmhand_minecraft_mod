package me.ez.farmhand.block;

import me.ez.farmhand.Config;
import me.ez.farmhand.Init;
import me.ez.farmhand.util.FarmUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/** Absorbs egg/feather item entities in range and can hatch a stored egg. */
public class ChickenCoopBlockEntity extends BlockEntity {

    private static final int CAPACITY = 64;

    private int eggs;
    private int feathers;
    private int cooldown;

    public ChickenCoopBlockEntity(BlockPos pos, BlockState state) {
        super(Init.CHICKEN_COOP_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChickenCoopBlockEntity coop) {
        if (!(level instanceof ServerLevel server)
                || !Config.ENABLED.get() || !Config.COOP_ENABLED.get()) {
            return;
        }
        if (--coop.cooldown > 0) {
            return;
        }
        coop.cooldown = 20;

        AABB box = new AABB(pos).inflate(Config.COOP_RADIUS.get());
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
            ItemStack stack = item.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == Items.EGG) {
                int moved = coop.absorb(stack, true);
                if (moved > 0) {
                    coop.applyAbsorb(stack, moved, true);
                    coop.consume(item, stack);
                }
            } else if (stack.getItem() == Items.FEATHER) {
                int moved = coop.absorb(stack, false);
                if (moved > 0) {
                    coop.applyAbsorb(stack, moved, false);
                    coop.consume(item, stack);
                }
            }
        }

        if (Config.COOP_INCUBATE.get() && coop.eggs > 0
                && level.getRandom().nextInt(100) < Config.COOP_INCUBATE_CHANCE.get()) {
            coop.hatch(server, pos);
        }
        coop.setChanged();
    }

    private int absorb(ItemStack stack, boolean egg) {
        int stored = egg ? eggs : feathers;
        return Math.min(stack.getCount(), Math.max(0, CAPACITY - stored));
    }

    private void applyAbsorb(ItemStack stack, int moved, boolean egg) {
        if (egg) {
            eggs += moved;
        } else {
            feathers += moved;
        }
        stack.shrink(moved);
    }

    private void consume(ItemEntity item, ItemStack stack) {
        if (stack.isEmpty()) {
            item.discard();
        } else {
            item.setItem(stack);
        }
    }

    private void hatch(ServerLevel level, BlockPos pos) {
        Chicken chick = EntityType.CHICKEN.create(level, EntitySpawnReason.BREEDING);
        if (chick == null) {
            return;
        }
        eggs--;
        chick.setBaby(true);
        chick.snapTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, level.getRandom().nextFloat() * 360f, 0f);
        level.addFreshEntity(chick);
    }

    /** Hands the stored contents to the player (or drops them at the block). */
    public void dispense(Player player) {
        if (eggs > 0) {
            FarmUtil.giveOrDrop(player, new ItemStack(Items.EGG, eggs));
            eggs = 0;
        }
        if (feathers > 0) {
            FarmUtil.giveOrDrop(player, new ItemStack(Items.FEATHER, feathers));
            feathers = 0;
        }
        setChanged();
    }

    public void dropContents() {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (eggs > 0) {
            Block.popResource(level, worldPosition, new ItemStack(Items.EGG, eggs));
            eggs = 0;
        }
        if (feathers > 0) {
            Block.popResource(level, worldPosition, new ItemStack(Items.FEATHER, feathers));
            feathers = 0;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.putInt("Eggs", eggs);
        output.putInt("Feathers", feathers);
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        eggs = input.getIntOr("Eggs", 0);
        feathers = input.getIntOr("Feathers", 0);
    }
}
