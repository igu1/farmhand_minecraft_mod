package me.ez.farmhand;

import me.ez.farmhand.block.GrowthLampBlockEntity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LampPowerTest {
    @Test void lightAndGrowthRespectManualPowerAndAutomaticPause() {
        assertTrue(GrowthLampBlockEntity.shouldIlluminate(true, true, true, false));
        assertFalse(GrowthLampBlockEntity.shouldIlluminate(true, true, true, true));
        assertFalse(GrowthLampBlockEntity.shouldIlluminate(false, true, true, false));
        assertFalse(GrowthLampBlockEntity.shouldIlluminate(false, true, true, true));
        assertFalse(GrowthLampBlockEntity.shouldIlluminate(true, false, true, false));
        assertFalse(GrowthLampBlockEntity.shouldIlluminate(true, true, false, false));
    }
}
