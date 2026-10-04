package me.ez.farmhand.item;

import me.ez.farmhand.Config;
import me.ez.farmhand.util.FarmUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;

/** Plants a plane of seeds from the player's inventory onto farmland. */
public class SeedPouchItem extends Item {

    public SeedPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!Config.ENABLED.get() || !Config.POUCH_ENABLED.get()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clicked = context.getClickedPos();
        int halfWidth = Config.POUCH_WIDTH.get();
        int halfDepth = Config.POUCH_DEPTH.get();
        int planted = 0;

        for (int dx = -halfWidth; dx <= halfWidth; dx++) {
            for (int dz = -halfDepth; dz <= halfDepth; dz++) {
                BlockPos soil = clicked.offset(dx, 0, dz);
                BlockPos cropPos = soil.above();
                if (!level.getBlockState(soil).is(Blocks.FARMLAND) || !level.isEmptyBlock(cropPos)) {
                    continue;
                }
                ItemStack seed = FarmUtil.findSeed(context.getPlayer());
                if (seed.isEmpty() || !(seed.getItem() instanceof BlockItem blockItem)
                        || !(blockItem.getBlock() instanceof CropBlock crop)) {
                    break;
                }
                level.setBlockAndUpdate(cropPos, crop.getStateForAge(0));
                if (!context.getPlayer().isCreative()) {
                    seed.shrink(1);
                }
                planted++;
            }
        }
        return planted > 0 ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
    }
}
