package me.ez.farmhand;

import me.ez.farmhand.util.ReadinessTracker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReadinessTrackerTest {
    @Test void milestonesFireOnceAndRearmBelowTheirThreshold() {
        var tracker = new ReadinessTracker();
        assertEquals(0, tracker.advance(0, 10, 50));
        assertEquals(ReadinessTracker.FIRST, tracker.advance(1, 10, 50));
        assertEquals(0, tracker.advance(2, 10, 50));
        assertEquals(ReadinessTracker.THRESHOLD, tracker.advance(5, 10, 50));
        assertEquals(ReadinessTracker.FULL, tracker.advance(10, 10, 50));
        assertEquals(0, tracker.advance(10, 10, 50));
        assertEquals(0, tracker.advance(6, 10, 50));
        assertEquals(ReadinessTracker.FULL, tracker.advance(10, 10, 50));
        tracker.advance(0, 10, 50);
        assertEquals(7, tracker.advance(10, 10, 50));
    }
    @Test void persistedMilestonesPreventReloadSpam() {
        var tracker = new ReadinessTracker();
        tracker.restore(7);
        assertEquals(0, tracker.advance(10, 10, 50));
        assertEquals(7, tracker.mask());
    }
    @Test void analogOutputIsBoundedAndEmptyFieldsAreNotReady() {
        assertEquals(0, ReadinessTracker.signal(0, 0));
        assertEquals(7, ReadinessTracker.signal(5, 10));
        assertEquals(15, ReadinessTracker.signal(10, 10));
        assertEquals(15, ReadinessTracker.signal(20, 10));
        assertEquals(0, new ReadinessTracker().advance(0, 0, 50));
    }
}
