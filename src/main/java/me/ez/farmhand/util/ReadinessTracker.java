package me.ez.farmhand.util;

/** Edge-triggered milestones: re-arm only when the field falls below that milestone. */
public final class ReadinessTracker {
    private int mask;
    public static final int FIRST = 1, THRESHOLD = 2, FULL = 4;
    public int advance(int ready, int total, int threshold) {
        int current = 0;
        if (ready > 0 && total > 0) current |= FIRST;
        if (total > 0 && ready * 100L >= (long) total * threshold) current |= THRESHOLD;
        if (total > 0 && ready >= total) current |= FULL;
        int crossed = current & ~mask;
        mask = current;
        return crossed;
    }
    public int mask() { return mask; }
    public void restore(int mask) { this.mask = mask & 7; }
    public static int signal(int ready, int total) {
        return total <= 0 ? 0 : (int) Math.max(0, Math.min(15, ready * 15L / total));
    }
}
