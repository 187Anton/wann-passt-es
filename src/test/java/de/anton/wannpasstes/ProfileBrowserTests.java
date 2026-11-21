package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
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
        when(geocodingClient.getLocation(anyInt())).thenReturn(
                new GeocodingResponse.Location(123, "Demo town", 52.5, 13.4,
                        "Europe/Berlin", "Demo country", "Demo region"));
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), any(ZoneId.class)))
                .thenReturn(List.of());

        ProcessBuilder command = new ProcessBuilder(
                System.getProperty("demo.node", "node"),
                Path.of("src/test/browser/profile-flow.mjs").toAbsolutePath().toString(),
                "http://localhost:" + port,
                LocalDate.now(ZoneId.of("Europe/Berlin")).plusDays(1).toString());
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
}
