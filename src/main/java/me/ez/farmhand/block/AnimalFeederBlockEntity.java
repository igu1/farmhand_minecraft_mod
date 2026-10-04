package me.ez.farmhand.block;

import java.util.List;
import me.ez.farmhand.Config;
import me.ez.farmhand.Init;
import me.ez.farmhand.util.FarmUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/** Holds one kind of breeding food and sets nearby adult animals in love. */
public class AnimalFeederBlockEntity extends BlockEntity {

    /** Foods accepted by the feeder. Animals still decide what they personally eat. */
    private static final List<TagKey<Item>> FOOD_TAGS = List.of(
            ItemTags.COW_FOOD, ItemTags.SHEEP_FOOD, ItemTags.PIG_FOOD, ItemTags.CHICKEN_FOOD,
            ItemTags.RABBIT_FOOD, ItemTags.HORSE_FOOD, ItemTags.GOAT_FOOD, ItemTags.PANDA_FOOD,
            ItemTags.FOX_FOOD, ItemTags.WOLF_FOOD, ItemTags.CAT_FOOD, ItemTags.TURTLE_FOOD,
            ItemTags.STRIDER_FOOD, ItemTags.HOGLIN_FOOD, ItemTags.FROG_FOOD, ItemTags.ARMADILLO_FOOD);

    private ItemStack food = ItemStack.EMPTY;
    private int cooldown;

    public AnimalFeederBlockEntity(BlockPos pos, BlockState state) {
        super(Init.ANIMAL_FEEDER_BE.get(), pos, state);
    }

    /** @return true when the stack was accepted (or the feeder is empty-handed). */
    public boolean insert(ItemStack stack, Player player) {
        if (stack.isEmpty() || !isAnimalFood(stack)) {
            return false;
        }
        if (!food.isEmpty() && !ItemStack.isSameItem(food, stack)) {
            return false;
        }
        if (food.isEmpty()) {
            food = stack.copyWithCount(1);
        } else if (food.getCount() < 64) {
            food.grow(1);
        } else {
            return false;
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        setChanged();
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AnimalFeederBlockEntity feeder) {
        if (level.isClientSide() || !Config.ENABLED.get() || !Config.FEEDER_ENABLED.get()) {
            return;
        }
        if (--feeder.cooldown > 0) {
            return;
        }
        feeder.cooldown = 40;
        if (feeder.food.isEmpty()) {
            return;
        }

        AABB box = new AABB(pos).inflate(Config.FEEDER_RADIUS.get());
        for (Animal animal : level.getEntitiesOfClass(Animal.class, box)) {
            if (feeder.food.isEmpty()) {
                break;
            }
            if (animal.isBaby() || !animal.canFallInLove() || !animal.isFood(feeder.food)) {
                continue;
            }
            animal.setInLove(null);
            feeder.food.shrink(1);
        }
        feeder.setChanged();
    }

    public void dispense(Player player) {
        if (!food.isEmpty()) {
            FarmUtil.giveOrDrop(player, food.copy());
            food = ItemStack.EMPTY;
            setChanged();
        }
    }

    public void dropContents() {
        if (level != null && !level.isClientSide() && !food.isEmpty()) {
            Block.popResource(level, worldPosition, food.copy());
            food = ItemStack.EMPTY;
        }
    }

    private static boolean isAnimalFood(ItemStack stack) {
        for (TagKey<Item> tag : FOOD_TAGS) {
            if (stack.typeHolder().is(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        output.store("Food", ItemStack.OPTIONAL_CODEC, food);
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        food = input.read("Food", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
