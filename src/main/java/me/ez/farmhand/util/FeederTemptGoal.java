package me.ez.farmhand.util;

import java.util.EnumSet;
import me.ez.farmhand.Config;
import me.ez.farmhand.Main;
import me.ez.farmhand.block.AnimalFeederBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Low priority temptation: never overrides panic, mating or player-held food goals. */
@EventBusSubscriber(modid = Main.MOD_ID)
public final class FeederTemptGoal extends Goal {
    private final Animal animal;
    private BlockPos feederPos;
    private Path path;
    private BlockPos approach;
    private long nextSearch;
    private int ticks;
    public FeederTemptGoal(Animal animal) { this.animal = animal; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
    @SubscribeEvent
    public static void join(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Animal animal)) return;
        install(animal);
    }
    public static void install(Animal animal) {
        if (animal.goalSelector.getAvailableGoals().stream().noneMatch(g -> g.getGoal() instanceof FeederTemptGoal))
            animal.goalSelector.addGoal(4, new FeederTemptGoal(animal));
    }
    private boolean ready() { return animal.isAlive() && animal.getAge() == 0 && animal.canFallInLove()
            && !animal.isPassenger() && !animal.isLeashed() && Config.ENABLED.get() && Config.FEEDER_ENABLED.get(); }
    private AnimalFeederBlockEntity feeder() {
        return feederPos != null && animal.level().isLoaded(feederPos)
                && animal.level().getBlockEntity(feederPos) instanceof AnimalFeederBlockEntity f ? f : null;
    }
    public boolean canUse() {
        if (!ready() || !(animal.level() instanceof ServerLevel level) || level.getGameTime() < nextSearch) return false;
        nextSearch = level.getGameTime() + 20 + animal.getRandom().nextInt(20);
        int radius = Config.FEEDER_RADIUS.get();
        double nearest = Double.MAX_VALUE; feederPos = null; path = null;
        for (BlockPos p : FeederRegistry.positions(level)) {
            if (!level.isLoaded(p) || !(level.getBlockEntity(p) instanceof AnimalFeederBlockEntity f)
                    || !f.active() || !f.hasFoodFor(animal)) continue;
            double d = p.distToCenterSqr(animal.getX(),animal.getY(),animal.getZ());
            if (d > radius * radius || d >= nearest) continue;
            Path found = null; BlockPos destination = null; double closest = Double.MAX_VALUE;
            for (BlockPos feet : BlockPos.betweenClosed(p.offset(-2,-1,-2),p.offset(2,1,2))) {
                if (!level.isLoaded(feet) || !level.isLoaded(feet.above()) || !level.isLoaded(feet.below())
                        || !p.closerToCenterThan(net.minecraft.world.phys.Vec3.atBottomCenterOf(feet),2.8)
                        || !level.getBlockState(feet).getCollisionShape(level,feet).isEmpty()
                        || !level.getBlockState(feet.above()).getCollisionShape(level,feet.above()).isEmpty()
                        || level.getBlockState(feet.below()).getCollisionShape(level,feet.below()).isEmpty()) continue;
                double distance = feet.distToCenterSqr(animal.getX(),animal.getY(),animal.getZ());
                if (distance >= closest) continue;
                Path candidate = animal.getNavigation().createPath(feet,0);
                if (candidate != null && candidate.canReach()) { found=candidate;destination=feet.immutable();closest=distance; }
            }
            if (found == null) continue;
            nearest = d; feederPos = p.immutable(); path = found; approach = destination;
        }
        return feederPos != null;
    }
    public boolean canContinueToUse() {
        AnimalFeederBlockEntity f = feeder();
        return ready() && ticks < 400 && f != null && f.active() && f.hasFoodFor(animal)
                && feederPos.closerToCenterThan(animal.position(), Config.FEEDER_RADIUS.get() + 1);
    }
    public boolean requiresUpdateEveryTick() { return true; }
    public void start() { ticks = 0; animal.getNavigation().moveTo(path, 1.0); }
    public void tick() {
        if (!canContinueToUse()) { animal.getNavigation().stop(); return; }
        ticks++;
        animal.getLookControl().setLookAt(feederPos.getX()+.5,feederPos.getY()+.6,feederPos.getZ()+.5,30,30);
        if (feederPos.closerToCenterThan(animal.position(), 2.8)) animal.getNavigation().stop();
        else if (ticks % 20 == 0) {
            Path next = animal.getNavigation().createPath(approach, 0);
            if (next != null && next.canReach()) animal.getNavigation().moveTo(next, 1.0);
        }
    }
    public void stop() { animal.getNavigation().stop(); feederPos = null; path = null; approach = null; }
}
