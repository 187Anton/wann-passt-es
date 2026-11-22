package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

//vollständiger Anwendungskontext wird geladen
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, //Webserver starten
        properties = "spring.datasource.url=jdbc:h2:mem:profile-http-demo;DB_CLOSE_ON_EXIT=FALSE") //Datenbank wird eingerichtet
@Import(SearchProfileHttpTests.ErrorTestConfiguration.class)
class SearchProfileHttpTests {

    @LocalServerPort
    private int port;

    @Autowired //Repository bereitstellen
    private SearchProfileRepository repository;

    @MockitoBean
    private GeocodingClient geocodingClient;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @Test
    void savesRendersAndDeletesProfileOverRealHttp() throws Exception {
        when(geocodingClient.getLocation(123)).thenReturn(new GeocodingResponse.Location(
                123, "Testort", 52.5, 13.4, "Europe/Berlin", null, null));
        String name = "HTTP demo profile";
        Long savedId = null;
        try {
            HttpResponse<String> saved = post("/profiles", //Formularwerte werden über HTTP gesendet
                    "profileName=HTTP+demo+profile&locationId=123&activity=WALKING"
                            + "&minDurationMinutes=60&temperaturePreference=MILD"
                            + "&windPreference=WEAK&precipitationPreference=DRY");
            assertEquals(302, saved.statusCode());
            String redirectPath = URI.create(saved.headers().firstValue("location").orElseThrow()).getPath();
            // A first redirect may carry a servlet session ID before cookies exist.
            assertEquals("/profiles", redirectPath.split(";", 2)[0]);

            //tatsächliche Speicherung prüfen
            SearchProfile profile = repository.findAll().stream()
                    .filter(item -> name.equals(item.getName())).findFirst().orElseThrow(); // name = Testname, liest Profil aus Testdatenbank
            savedId = profile.getId();
            assertEquals(Activity.WALKING, profile.getActivity());

            //Profilübersicht prüfen
            HttpResponse<String> overview = client.send(HttpRequest.newBuilder(
                            URI.create(baseUrl() + "/profiles")).timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, overview.statusCode()); //HTTP Statuscode 200 = erfolgreicher Abruf
            assertTrue(overview.body().contains(name)); //Überprüfung, ob Profilname in der Profilübersicht enthalten ist

            //Profillöschen prüfen
            assertEquals(302, post("/profiles/delete", "id=" + savedId).statusCode());
            assertFalse(repository.existsById(savedId));
        } finally {
            // Sobald die Profil-ID bekannt ist, kann der Test damit zusätzlich aufräumen.
            if (savedId != null) repository.deleteById(savedId);
        }
    }

    @Test
    void editsAllFieldsAndCanKeepItsOwnName() throws Exception {
        stubLocation(123, "Testort");
        stubLocation(456, "Zweiter Testort");
        post("/profiles", profileForm("Edit+demo", 123));
        SearchProfile original = findProfile("Edit demo");
        try {
            assertTrue(get("/profiles/" + original.getId() + "/edit").body().contains("Testort"));
            HttpResponse<String> updated = post("/profiles/" + original.getId() + "/edit",
                    "profileName=+EDIT+demo+&locationId=456&activity=HIKING&minDurationMinutes=135"
                            + "&temperaturePreference=WARM&windPreference=MODERATE&precipitationPreference=LIGHT_RAIN");
            assertEquals(302, updated.statusCode());
            SearchProfile result = repository.findById(original.getId()).orElseThrow();
            assertEquals("EDIT demo", result.getName());
            assertEquals(456, result.getLocationId());
            assertEquals("Zweiter Testort", result.getLocationName());
            assertEquals(Activity.HIKING, result.getActivity());
            assertEquals(135, result.getMinDurationMinutes());
            assertEquals(TemperaturePreference.WARM, result.getTemperaturePreference());
            assertEquals(WindPreference.MODERATE, result.getWindPreference());
            assertEquals(PrecipitationPreference.LIGHT_RAIN, result.getPrecipitationPreference());
        } finally {
            repository.deleteById(original.getId());
        }
    }

    @Test
    void rejectsDuplicatedNameOnCreateAndUpdateWhileKeepingSubmittedValues() throws Exception {
        stubLocation(123, "Testort");
        post("/profiles", profileForm("Edit+original", 123));
        post("/profiles", profileForm("Name+schon+da", 123));
        SearchProfile original = findProfile("Edit original");
        SearchProfile duplicate = findProfile("Name schon da");

        try {
            LocalDate from = LocalDate.now(ZoneId.of("Europe/Berlin")).plusDays(1);
            HttpResponse<String> rejectedCreate = post("/profiles", profileForm("+NAME+SCHON+DA+", 123)
                    + "&startDate=" + from + "&endDate=" + from.plusDays(1));
            assertEquals(302, rejectedCreate.statusCode());
            URI target = URI.create(rejectedCreate.headers().firstValue("location").orElseThrow());
            assertTrue(target.getRawQuery().contains("startDate=" + from));
            assertTrue(target.getRawQuery().contains("endDate=" + from.plusDays(1)));
            assertTrue(target.getRawQuery().contains("minDurationMinutes=60"));

            HttpResponse<String> rejectedEdit = post("/profiles/" + original.getId() + "/edit",
                    "profileName=+NAME+SCHON+DA+&locationId=123&activity=HIKING&minDurationMinutes=125"
                            + "&temperaturePreference=WARM&windPreference=MODERATE&precipitationPreference=LIGHT_RAIN");
            assertEquals(200, rejectedEdit.statusCode());
            assertTrue(rejectedEdit.body().contains("Diesen Profilnamen gibt es schon."));
            assertTrue(rejectedEdit.body().contains("> NAME SCHON DA </textarea>"));
            assertTrue(rejectedEdit.body().contains("value=\"125\""));
            assertEquals("Edit original", repository.findById(original.getId()).orElseThrow().getName());

            HttpResponse<String> invalidEdit = post("/profiles/" + original.getId() + "/edit",
                    profileForm("Entwurf", 123).replace("minDurationMinutes=60", "minDurationMinutes=abc"));
            assertEquals(400, invalidEdit.statusCode());
            assertTrue(invalidEdit.body().contains("Die Mindestdauer muss mindestens eine Minute betragen."));
            assertTrue(invalidEdit.body().contains(">Entwurf</textarea>"));
        } finally {
            repository.deleteById(original.getId());
            repository.deleteById(duplicate.getId());
        }
    }

    @Test
    void rendersGermanPagesForNotFoundAndUnexpectedErrors() throws Exception {
        HttpResponse<String> notFound = get("/diese-seite-gibt-es-nicht");
        assertEquals(404, notFound.statusCode());
        assertTrue(notFound.body().contains("Seite nicht gefunden"));
        assertTrue(notFound.body().contains("/css/app.css"));

        HttpResponse<String> failure = get("/test-server-error");
        assertEquals(500, failure.statusCode());
        assertTrue(failure.body().contains("Die Seite konnte nicht geladen werden"));
        assertFalse(failure.body().contains("private failure details"));
        assertFalse(failure.body().contains("Whitelabel"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(baseUrl() + path)).timeout(Duration.ofSeconds(10))
                .header("Accept", "text/html").GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private void stubLocation(int id, String name) {
        when(geocodingClient.getLocation(id)).thenReturn(new GeocodingResponse.Location(
                id, name, 52.5, 13.4, "Europe/Berlin", null, null));
    }

    private SearchProfile findProfile(String name) {
        return repository.findAll().stream().filter(profile -> name.equals(profile.getName())).findFirst().orElseThrow();
    }

    private String profileForm(String name, int locationId) {
        return "profileName=" + name + "&locationId=" + locationId + "&activity=WALKING"
                + "&minDurationMinutes=60&temperaturePreference=MILD&windPreference=WEAK&precipitationPreference=DRY";
    }

    @TestConfiguration
    static class ErrorTestConfiguration {
        @Bean
        TestErrorController testErrorController() {
            return new TestErrorController();
        }
    }

    @Controller
    static class TestErrorController {
        @GetMapping("/test-server-error")
        String fail() {
            throw new IllegalStateException("private failure details");
        }
    }

    private String baseUrl() { return "http://localhost:" + port; }

    //Java HTTP Client
    private HttpResponse<String> post(String path, String form) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(baseUrl() + path)) //Adresse des Servers
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/x-www-form-urlencoded") //Formulardaten = Übertragungsdaten
                        .POST(HttpRequest.BodyPublishers.ofString(form)).build(), //Methode & Inhalt festlegen
                HttpResponse.BodyHandlers.ofString());
    }
}
