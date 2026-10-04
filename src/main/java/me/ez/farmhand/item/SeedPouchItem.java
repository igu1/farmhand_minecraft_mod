package me.ez.farmhand.item;

import me.ez.farmhand.Config;
import me.ez.farmhand.menu.MachineMenu;
import me.ez.farmhand.menu.PouchInventory;
import me.ez.farmhand.util.FarmUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;

public class SeedPouchItem extends Item {
    public SeedPouchItem(Properties properties) { super(properties); }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!Config.ENABLED.get() || !Config.POUCH_ENABLED.get()) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            PouchInventory storage = new PouchInventory(player.getItemInHand(hand));
            var data = new SimpleContainerData(4);
            data.set(0, 2);
            player.openMenu(new SimpleMenuProvider((id, inv, owner) -> new MachineMenu(id, inv, storage, data),
                    Component.translatable("item.farmhand.seed_pouch")));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    public InteractionResult useOn(UseOnContext context) {
        if (!Config.ENABLED.get() || !Config.POUCH_ENABLED.get() || context.getPlayer() == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player.isShiftKeyDown()) return use(level, player, context.getHand());
        BlockPos clicked = context.getClickedPos();
        var clickedBlock = level.getBlockState(clicked).getBlock();
        if (clickedBlock instanceof CropBlock || clickedBlock instanceof StemBlock) clicked = clicked.below();
        if (!level.getBlockState(clicked).is(Blocks.FARMLAND)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        PouchInventory storage = new PouchInventory(context.getItemInHand());
        int planted = 0;
        for (int dx = -Config.POUCH_WIDTH.get(); dx <= Config.POUCH_WIDTH.get(); dx++) {
            for (int dz = -Config.POUCH_DEPTH.get(); dz <= Config.POUCH_DEPTH.get(); dz++) {
                BlockPos soil = clicked.offset(dx, 0, dz), target = soil.above();
                if (!level.hasChunkAt(target) || !level.getBlockState(soil).is(Blocks.FARMLAND)
                        || !level.isEmptyBlock(target) || !player.mayUseItemAt(target, context.getClickedFace(), context.getItemInHand())) continue;
                ItemStack seed = ItemStack.EMPTY;
                for (int slot = 0; slot < storage.getContainerSize(); slot++) {
                    if (FarmUtil.cropFor(storage.getItem(slot)) != null) { seed = storage.getItem(slot); break; }
                }
                if (seed.isEmpty()) seed = FarmUtil.findSeed(player);
                var crop = FarmUtil.cropFor(seed);
                if (crop == null || !crop.defaultBlockState().canSurvive(level, target)) continue;
                if (level.setBlockAndUpdate(target, crop.defaultBlockState())) {
                    if (!player.isCreative()) seed.shrink(1);
                    planted++;
                    me.ez.farmhand.util.GrowpostRegistry.wake(level, target);
                    ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            target.getX() + .5, target.getY() + .2, target.getZ() + .5, 1, .1, .1, .1, .005);
                }
            }
        }
        storage.setChanged();
        return planted > 0 ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
    }
}
