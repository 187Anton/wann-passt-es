package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainpageControllerTests {

    @Test
    void keepsOnlyWindowsMeetingMinimumDuration() {
        WeatherClient weatherClient = mock(WeatherClient.class);

        MainpageController controller = new MainpageController(
                weatherClient,
                new TimeWindowService());

        ZoneId zone = ZoneId.of("Europe/Berlin");
        OffsetDateTime start = OffsetDateTime.now(zone).plusDays(1);

        List<WeatherHour> hours = List.of(
                new WeatherHour(18.0, start, 12.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(1), 12.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(2), 30.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(3), 12.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(4), 12.0, 0.0, 0),
                new WeatherHour(18.0, start.plusHours(5), 12.0, 0.0, 0)
        );

        when(weatherClient.fetchForecast(52.52, 13.405, zone))
                .thenReturn(hours);

        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.mainpage(120, model);

        TimeWindow expected = new TimeWindow(
                start.plusHours(3),
                start.plusHours(5));

        assertEquals("mainpage", view);
        assertEquals(List.of(expected), model.get("suitableWindows"));
        assertEquals(120, model.get("minDurationMinutes"));
    }
}