package me.ez.farmhand.util;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Indexed loaded troughs, instead of thousands of block lookups for every animal. */
public final class FeederRegistry {
    private static final WeakHashMap<Level, Set<BlockPos>> FEEDERS = new WeakHashMap<>();
    private FeederRegistry() {}
    public static void add(Level level, BlockPos pos) { FEEDERS.computeIfAbsent(level,k->new HashSet<>()).add(pos.immutable()); }
    public static void remove(Level level, BlockPos pos) { var set=FEEDERS.get(level);if(set!=null)set.remove(pos); }
    public static Set<BlockPos> positions(Level level) { return FEEDERS.getOrDefault(level, Set.of()); }
}
