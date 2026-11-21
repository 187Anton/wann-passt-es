package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

//vollständiger Anwendungskontext wird geladen
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, //Webserver starten
        properties = "spring.datasource.url=jdbc:h2:mem:profile-http-demo;DB_CLOSE_ON_EXIT=FALSE") //Datenbank wird eingerichtet
class SearchProfileHttpTests {

    @LocalServerPort
    private int port;

    @Autowired //Repository bereitstellen
    private SearchProfileRepository repository;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @Test
    void savesRendersAndDeletesProfileOverRealHttp() throws Exception {
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
