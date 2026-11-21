package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@EnabledIfSystemProperty(named = "demo.browser", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:profile-browser-demo;DB_CLOSE_ON_EXIT=FALSE")
class ProfileBrowserTests {

    @LocalServerPort
    private int port;

    @MockitoBean
    private GeocodingClient geocodingClient;

    @MockitoBean
    private WeatherClient weatherClient;

    @Test
    void savesReusesAndDeletesProfileInChromium() throws Exception {
        // External APIs are outside this browser scenario's test boundary.
        var harbor = location(123, "Testhafen");
        var mountain = location(456, "Testberg");
        when(geocodingClient.getLocation(2950159)).thenReturn(location(2950159, "Teststadt"));
        when(geocodingClient.getLocation(harbor.id())).thenReturn(harbor);
        when(geocodingClient.getLocation(mountain.id())).thenReturn(mountain);
        when(geocodingClient.search("Testhafen")).thenReturn(List.of(harbor));
        when(geocodingClient.search("Testberg")).thenReturn(List.of(mountain));
        ZoneId zone = ZoneId.of("Europe/Berlin");
        LocalDate searchDate = LocalDate.now(zone).plusDays(1);
        OffsetDateTime start = searchDate.atTime(22, 0).atZone(zone).toOffsetDateTime();
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any(ZoneId.class)))
                .thenReturn(List.of(hour(start), hour(start.plusHours(1)), hour(start.plusHours(2)), hour(start.plusHours(3))));

        ProcessBuilder command = new ProcessBuilder(
                System.getProperty("demo.node", "node"),
                Path.of("src/test/browser/profile-flow.mjs").toAbsolutePath().toString(),
                "http://localhost:" + port,
                searchDate.toString());
        command.redirectErrorStream(true);
        Process browser = command.start();
        try {
            assertTrue(browser.waitFor(45, TimeUnit.SECONDS), "Browser demo timed out");
            String output = new String(browser.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, browser.exitValue(), output);
        } finally {
            browser.destroyForcibly();
        }
    }
    private GeocodingResponse.Location location(int id, String name) {
        return new GeocodingResponse.Location(id, name, 52.5, 13.4, "Europe/Berlin", "Testland", "Testregion");
    }

    private WeatherHour hour(OffsetDateTime time) {
        return new WeatherHour(22, time, 12, 0, 0);
    }
}
