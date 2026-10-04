package me.ez.farmhand.block;

import me.ez.farmhand.Config;
import me.ez.farmhand.Init;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;

/** Breeding food inventory; animals on a breeding cooldown must not consume food. */
public class AnimalFeederBlockEntity extends MachineBlockEntity {
    public AnimalFeederBlockEntity(BlockPos pos, BlockState state) { super(Init.ANIMAL_FEEDER_BE.get(), pos, state); }
    public int kind() { return 1; }
    protected int cycleLength() { return 40; }
    protected Component getDefaultName() { return Component.translatable("block.farmhand.animal_feeder"); }
    public static boolean isAnimalFood(ItemStack s) {
        return s.is(ItemTags.COW_FOOD) || s.is(ItemTags.SHEEP_FOOD) || s.is(ItemTags.PIG_FOOD)
                || s.is(ItemTags.CHICKEN_FOOD) || s.is(ItemTags.RABBIT_FOOD) || s.is(ItemTags.HORSE_FOOD)
                || s.is(ItemTags.GOAT_FOOD) || s.is(ItemTags.PANDA_FOOD) || s.is(ItemTags.FOX_FOOD)
                || s.is(ItemTags.WOLF_FOOD) || s.is(ItemTags.CAT_FOOD) || s.is(ItemTags.TURTLE_FOOD)
                || s.is(ItemTags.STRIDER_FOOD) || s.is(ItemTags.HOGLIN_FOOD) || s.is(ItemTags.FROG_FOOD)
                || s.is(ItemTags.ARMADILLO_FOOD);
    }
    public boolean canPlaceItem(int slot, ItemStack stack) { return isAnimalFood(stack); }

    public static void tick(Level level, BlockPos pos, BlockState state, AnimalFeederBlockEntity feeder) {
        if (!(level instanceof ServerLevel) || !Config.ENABLED.get() || !Config.FEEDER_ENABLED.get() || !feeder.running) return;
        if (--feeder.cooldown > 0) return;
        feeder.cooldown = feeder.cycleLength();
        var animals = level.getEntitiesOfClass(Animal.class, new AABB(pos).inflate(Config.FEEDER_RADIUS.get()));
        for (Animal animal : animals) {
            if (animal.getAge() != 0 || !animal.canFallInLove()) continue;
            // Do not waste food on a lone animal. Compatible adult partners must be nearby.
            Animal partner = animals.stream().filter(other -> other != animal && other.getClass() == animal.getClass()
                    && other.getAge() == 0 && (other.canFallInLove() || other.isInLove()))
                    .min(java.util.Comparator.comparingDouble(animal::distanceToSqr)).orElse(null);
            if (partner == null) continue;
            int needed = partner.isInLove() ? 1 : 2;
            int available = feeder.items.stream().filter(food -> !food.isEmpty() && animal.isFood(food)
                    && partner.isFood(food)).mapToInt(ItemStack::getCount).sum();
            if (available < needed) continue;
            for (ItemStack food : feeder.items) {
                if (needed == 0) break;
                if (food.isEmpty() || !animal.isFood(food) || !partner.isFood(food)) continue;
                int consumed = Math.min(needed, food.getCount());
                food.shrink(consumed);
                needed -= consumed;
            }
            animal.setInLove(null); // Vanilla sends the heart event; no duplicate particle spam.
            if (!partner.isInLove()) partner.setInLove(null);
            feeder.operations++;
            feeder.setChanged();
        }
    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (items.stream().allMatch(ItemStack::isEmpty)) {
            items.set(0, input.read("Food", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        }
    }
}
