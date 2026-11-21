package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Error pages over real HTTP, including Spring Boot's error dispatch (C1, C2).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:error-pages;DB_CLOSE_ON_EXIT=FALSE")
@Import(ErrorPageHttpTests.FailingController.class)
class ErrorPageHttpTests {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    /** Simulates an unexpected server error with an internal message. */
    @Controller
    static class FailingController {
        @GetMapping("/test-only/failure")
        String fail() {
            throw new IllegalStateException("secret internal detail");
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .timeout(Duration.ofSeconds(10)).header("Accept", "text/html").GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static void assertNoTechnicalDetails(String body) {
        assertFalse(body.contains("Whitelabel"), body);
        assertFalse(body.contains("Exception"), body);
        assertFalse(body.contains("at de.anton"), body);
    }

    @Test
    void unknownUrlShowsDesignedNotFoundPage() throws Exception {
        HttpResponse<String> response = get("/does-not-exist");

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Seite nicht gefunden"));
        assertTrue(response.body().contains("Zur Planung"));
        assertTrue(response.body().contains("/css/app.css"));
        assertNoTechnicalDetails(response.body());
    }

    @Test
    void unexpectedErrorShowsDesignedPageWithoutDetails() throws Exception {
        HttpResponse<String> response = get("/test-only/failure");

        assertEquals(500, response.statusCode());
        assertTrue(response.body().contains("Da ist etwas schiefgelaufen"));
        assertFalse(response.body().contains("secret internal detail"));
        assertNoTechnicalDetails(response.body());
    }

    @Test
    void invalidParametersShowGermanErrorPage() throws Exception {
        HttpResponse<String> response = get("/?minDurationMinutes=abc");

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Ungültige Eingabe"));
        assertTrue(response.body().contains("Mindestdauer"));
        assertNoTechnicalDetails(response.body());
    }

    @Test
    void unknownProfileShowsNotFoundMessage() throws Exception {
        HttpResponse<String> response = get("/profiles/987654/edit");

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Suchprofil nicht gefunden"));
        assertNoTechnicalDetails(response.body());
    }
}
