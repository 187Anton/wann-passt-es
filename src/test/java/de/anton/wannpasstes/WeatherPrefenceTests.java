package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherPreferenceTests {

    @Test
    void providesConfiguredTemperatureLimits() {
        assertEquals(5.0,
                TemperaturePreference.COOL.getMinTemperatureCelsius());
        assertEquals(15.0,
                TemperaturePreference.COOL.getMaxTemperatureCelsius());

        assertEquals(15.0,
                TemperaturePreference.MILD.getMinTemperatureCelsius());
        assertEquals(25.0,
                TemperaturePreference.MILD.getMaxTemperatureCelsius());

        assertEquals(20.0,
                TemperaturePreference.WARM.getMinTemperatureCelsius());
        assertEquals(30.0,
                TemperaturePreference.WARM.getMaxTemperatureCelsius());
    }

    @Test
    void providesConfiguredWindLimits() {
        assertEquals(15.0, WindPreference.WEAK.getMaxWindKmh());
        assertEquals(30.0, WindPreference.MODERATE.getMaxWindKmh());
    }

    @Test
    void allowsUnlimitedTemperatureAndWindForAnyPreference() {
        assertEquals(Double.NEGATIVE_INFINITY,
                TemperaturePreference.ANY.getMinTemperatureCelsius());
        assertEquals(Double.POSITIVE_INFINITY,
                TemperaturePreference.ANY.getMaxTemperatureCelsius());
        assertEquals(Double.POSITIVE_INFINITY,
                WindPreference.ANY.getMaxWindKmh());
    }
}