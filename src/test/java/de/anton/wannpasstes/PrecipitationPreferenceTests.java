package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrecipitationPreferenceTests {

    @Test
    void providesConfiguredPrecipitationLimits() {
        assertEquals(0.0,
                PrecipitationPreference.DRY.getMaxPrecipitationMm());
        assertEquals(0.2,
                PrecipitationPreference.LIGHT_DRIZZLE.getMaxPrecipitationMm());
        assertEquals(1.0,
                PrecipitationPreference.LIGHT_RAIN.getMaxPrecipitationMm());
    }

    @Test
    void allowsUnlimitedPrecipitationForAnyPreference() {
        assertEquals(Double.POSITIVE_INFINITY,
                PrecipitationPreference.ANY.getMaxPrecipitationMm());
    }
}