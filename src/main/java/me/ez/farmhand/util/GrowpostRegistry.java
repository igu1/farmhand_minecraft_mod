package me.ez.farmhand.util;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import me.ez.farmhand.Config;
import me.ez.farmhand.Main;
import me.ez.farmhand.block.GrowpostBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Loaded server posts only; no chunk loading, area scans or strong references to unloaded worlds. */
@EventBusSubscriber(modid = Main.MOD_ID)
public final class GrowpostRegistry {
    private static final WeakHashMap<Level, Set<BlockPos>> POSTS = new WeakHashMap<>();
    private GrowpostRegistry() {}
    public static void add(Level level, BlockPos pos) { POSTS.computeIfAbsent(level, unused -> new HashSet<>()).add(pos.immutable()); }
    public static void remove(Level level, BlockPos pos) { var posts = POSTS.get(level); if (posts != null) posts.remove(pos); }
    public static boolean pausesLamp(Level level, BlockPos pos) { return matches(level, pos, true); }
    private static boolean matches(Level level, BlockPos pos, boolean lamp) {
        if (!Config.ENABLED.get()) return false;
        var posts = POSTS.get(level);
        if (posts == null) return false;
        for (BlockPos postPos : posts) {
            if (!level.hasChunkAt(postPos)) continue;
            if (level.getBlockEntity(postPos) instanceof GrowpostBlockEntity post
                    && (lamp ? post.pausesLamp(pos) : post.protects(pos))) return true;
        }
        return false;
    }
    @SubscribeEvent
    public static void trample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide() && matches(level, event.getPos(), false)) event.setCanceled(true);
    }
}
