package me.ez.farmhand.block;

import me.ez.farmhand.Config;
import me.ez.farmhand.Init;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Visible item attraction followed by real inventory storage and optional incubation. */
public class ChickenCoopBlockEntity extends MachineBlockEntity {
    public ChickenCoopBlockEntity(BlockPos pos, BlockState state) {
        super(Init.CHICKEN_COOP_BE.get(), pos, state);
        running = Config.COOP_INCUBATE.get();
        cooldown = cycleLength();
    }
    public int kind() { return 0; }
    protected int cycleLength() { return Config.COOP_INCUBATE_TICKS.get(); }
    protected Component getDefaultName() { return Component.translatable("block.farmhand.chicken_coop"); }
    public boolean canPlaceItem(int slot, ItemStack stack) { return stack.is(Items.EGG) || stack.is(Items.FEATHER); }

    public static void tick(Level level, BlockPos pos, BlockState state, ChickenCoopBlockEntity coop) {
        if (!(level instanceof ServerLevel server)) return;
        if (!Config.ENABLED.get() || !Config.COOP_ENABLED.get()) { coop.status = 3; return; }
        Vec3 target = new Vec3(pos.getX() + .5, pos.getY() + .45, pos.getZ() + .5);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(Config.COOP_RADIUS.get()))) {
            ItemStack stack = item.getItem();
            if (stack.isEmpty() || !coop.canPlaceItem(0, stack) || coop.roomFor(stack) == 0) continue;
            Vec3 offset = target.subtract(item.position());
            if (offset.lengthSqr() > 1.15 * 1.15) {
                item.setDeltaMovement(offset.normalize().scale(.22).add(0, .04, 0));
                item.hurtMarked = true;
            } else {
                ItemStack remainder = stack.copy();
                coop.store(remainder);
                if (remainder.isEmpty()) item.discard(); else item.setItem(remainder);
                server.sendParticles(ParticleTypes.POOF, target.x, target.y, target.z, 2, .1, .1, .1, .005);
            }
        }
        if (!coop.running) {
            coop.status = 3;
            coop.cooldown = coop.cycleLength();
            return;
        }
        if (coop.items.stream().noneMatch(stack -> stack.is(Items.EGG) && !stack.isEmpty())) {
            coop.status = 0;
            coop.cooldown = coop.cycleLength();
            return;
        }
        if (coop.status != 2) coop.status = 1;
        if (--coop.cooldown > 0) return;
        coop.cooldown = coop.cycleLength();
        for (int slot = 0; slot < 9; slot++) {
            if (!coop.items.get(slot).is(Items.EGG)) continue;
            var chick = EntityType.CHICKEN.create(server, EntitySpawnReason.BREEDING);
            if (chick == null) return;
            chick.setBaby(true);
            boolean room = false;
            double[][] candidates = {{.5, 0, -.6}, {.5, 0, 1.6}, {-.6, 0, .5}, {1.6, 0, .5}, {.5, 1.05, .5}};
            for (double[] offset : candidates) {
                chick.snapTo(pos.getX() + offset[0], pos.getY() + offset[1], pos.getZ() + offset[2], 0, 0);
                if (server.noCollision(chick)) { room = true; break; }
            }
            if (!room) { coop.status = 2; coop.cooldown = 20; return; }
            if (server.addFreshEntity(chick)) {
                coop.items.get(slot).shrink(1);
                coop.operations++;
                coop.setChanged();
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER, chick.getX(), chick.getY() + .4, chick.getZ(), 4, .2, .1, .2, .01);
            }
            return;
        }
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        cooldown = Math.max(1, Math.min(cycleLength(), input.getIntOr("IncubationRemaining", cycleLength())));
        // Migrate the original counter-based inventory without duplicating new-format saves.
        if (items.stream().allMatch(ItemStack::isEmpty)) {
            int eggs = input.getIntOr("Eggs", 0), feathers = input.getIntOr("Feathers", 0);
            if (eggs > 0) store(new ItemStack(Items.EGG, Math.min(64, eggs)));
            if (feathers > 0) store(new ItemStack(Items.FEATHER, Math.min(64, feathers)));
        }
    }

    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("IncubationRemaining", Math.max(1, cooldown));
    }
}
