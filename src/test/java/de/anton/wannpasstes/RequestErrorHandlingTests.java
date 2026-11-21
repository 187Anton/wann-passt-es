package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Invalid input and unexpected failures have to end on a styled German page
 * with a fitting status code, never on a whitelabel page or a stack trace.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:request-errors;DB_CLOSE_ON_EXIT=FALSE")
class RequestErrorHandlingTests {

    @LocalServerPort
    private int port;

    @MockitoBean
    private GeocodingClient geocodingClient;

    @MockitoBean
    private WeatherClient weatherClient;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @Test
    void rejectsUnknownActivityWithFriendlyBadRequest() throws Exception {
        HttpResponse<String> response = get("/?activity=FLYING");

        assertEquals(400, response.statusCode());
        assertStyledErrorPage(response.body());
        assertTrue(response.body().contains("Ungültige Eingabe"), response.body());
    }

    @Test
    void rejectsZeroMinimumDurationWithFriendlyBadRequest() throws Exception {
        HttpResponse<String> response = get("/?minDurationMinutes=0");

        assertEquals(400, response.statusCode());
        assertStyledErrorPage(response.body());
    }

    @Test
    void rejectsTextInsteadOfNumberWithFriendlyBadRequest() throws Exception {
        HttpResponse<String> response = get("/?minDurationMinutes=sechzig");

        assertEquals(400, response.statusCode());
        assertStyledErrorPage(response.body());
    }

    @Test
    void rejectsUnparsableDateWithFriendlyBadRequest() throws Exception {
        HttpResponse<String> response = get("/?startDate=2026-13-99");

        assertEquals(400, response.statusCode());
        assertStyledErrorPage(response.body());
    }

    @Test
    void acceptsEmptyDateParameters() throws Exception {
        // An empty date input submits an empty parameter; that has to mean
        // "no date given" instead of a conversion error.
        when(geocodingClient.getLocation(anyInt())).thenReturn(
                new GeocodingResponse.Location(1, "Testort", 50.0, 8.0,
                        "Europe/Berlin", "Deutschland", null));
        when(weatherClient.fetchForecast(anyDouble(), anyDouble(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.List.of());

        HttpResponse<String> response = get("/?startDate=&endDate=");

        assertEquals(200, response.statusCode(), response.body());
    }

    @Test
    void rejectsTextInsteadOfProfileIdWithFriendlyBadRequest() throws Exception {
        HttpResponse<String> response = post("/profiles/delete", "id=abc");

        assertEquals(400, response.statusCode());
        assertStyledErrorPage(response.body());
    }

    @Test
    void showsStyledNotFoundPageForUnknownUrl() throws Exception {
        HttpResponse<String> response = get("/diese-seite-gibt-es-nicht");

        assertEquals(404, response.statusCode());
        assertStyledErrorPage(response.body());
        assertTrue(response.body().contains("Seite nicht gefunden"), response.body());
    }

    @Test
    void hidesTechnicalDetailsOfUnexpectedErrors() throws Exception {
        when(geocodingClient.getLocation(anyInt()))
                .thenThrow(new IllegalStateException("interner Testfehler"));

        HttpResponse<String> response = get("/");

        assertEquals(500, response.statusCode());
        assertStyledErrorPage(response.body());
        assertTrue(response.body().contains("Etwas ist schiefgelaufen"), response.body());
        assertFalse(response.body().contains("interner Testfehler"));
        assertFalse(response.body().contains("IllegalStateException"));
    }

    private void assertStyledErrorPage(String body) {
        assertFalse(body.contains("Whitelabel Error Page"), body);
        assertTrue(body.contains("Wann passt es?"), body);
        assertTrue(body.contains("Zur Planung"), body);
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(baseUrl() + path))
                        .timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String form) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(baseUrl() + path))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(form)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
