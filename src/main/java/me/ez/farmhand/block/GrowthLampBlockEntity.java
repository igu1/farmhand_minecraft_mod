package me.ez.farmhand.block;

import me.ez.farmhand.Config;
import me.ez.farmhand.Init;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Server-side ticker that grows nearby crops. */
public class GrowthLampBlockEntity extends BlockEntity {

    private int cooldown;

    public GrowthLampBlockEntity(BlockPos pos, BlockState state) {
        super(Init.GROWTH_LAMP_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrowthLampBlockEntity lamp) {
        if (!(level instanceof ServerLevel server)
                || !Config.ENABLED.get() || !Config.LAMP_ENABLED.get()) {
            return;
        }
        if (--lamp.cooldown > 0) {
            return;
        }
        lamp.cooldown = Config.LAMP_INTERVAL.get();
        if (me.ez.farmhand.util.GrowpostRegistry.pausesLamp(level, pos)) return;

        int radius = Config.LAMP_RADIUS.get();
        int chance = Config.LAMP_CHANCE.get();
        RandomSource random = level.getRandom();

        for (int attempt = 0; attempt < radius * radius; attempt++) {
            BlockPos target = pos.offset(
                    random.nextInt(radius * 2 + 1) - radius,
                    random.nextInt(5) - 2,
                    random.nextInt(radius * 2 + 1) - radius);
            if (!level.hasChunkAt(target)) {
                continue;
            }
            BlockState cropState = level.getBlockState(target);
            if (cropState.getBlock() instanceof CropBlock crop
                    && !crop.isMaxAge(cropState)
                    && random.nextInt(100) < chance
                    && crop.isValidBonemealTarget(level, target, cropState)) {
                crop.performBonemeal(server, random, target, cropState);
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        target.getX() + 0.5, target.getY() + 0.8, target.getZ() + 0.5,
                        3, 0.2, 0.15, 0.2, 0.01);
                server.sendParticles(ParticleTypes.GLOW,
                        pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5,
                        1, 0.15, 0.2, 0.15, 0.01);
            }
        }
    }
}
