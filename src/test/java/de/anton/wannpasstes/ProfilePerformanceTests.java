package de.anton.wannpasstes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "demo.performance", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:profile-performance-demo;DB_CLOSE_ON_EXIT=FALSE")
class ProfilePerformanceTests {

    @LocalServerPort
    private int port;

    @Test
    void checksLocalResponseTimeAfterWarmup() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/profiles"))
                .timeout(Duration.ofSeconds(10)).GET().build();
        for (int i = 0; i < 5; i++) client.send(request, HttpResponse.BodyHandlers.discarding());

        var milliseconds = new ArrayList<Double>();
        for (int i = 0; i < 30; i++) {
            long start = System.nanoTime();
            assertEquals(200, client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode());
            milliseconds.add((System.nanoTime() - start) / 1_000_000.0);
        }
        Collections.sort(milliseconds);
        double p95 = milliseconds.get((int) Math.ceil(milliseconds.size() * 0.95) - 1);
        double demoLimit = Double.parseDouble(System.getProperty("demo.maxP95Millis", "1000"));
        System.out.printf("Local response-time demo: p95=%.1f ms, limit=%.1f ms%n", p95, demoLimit);
        assertTrue(p95 < demoLimit, "Local demo response-time budget exceeded: " + p95);
    }

    @Test
    void handlesFourConcurrentUsersWithTenRequestsEach() throws Exception {
        // A bounded load example, not a capacity or stress benchmark.
        CountDownLatch start = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(4)) {
            List<Future<Void>> users = new ArrayList<>();
            for (int user = 0; user < 4; user++) {
                users.add(workers.submit(() -> {
                    start.await();
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder(
                            URI.create("http://localhost:" + port + "/profiles"))
                            .timeout(Duration.ofSeconds(5)).GET().build();
                    for (int i = 0; i < 10; i++) {
                        assertEquals(200, client.send(request,
                                HttpResponse.BodyHandlers.discarding()).statusCode());
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<Void> user : users) user.get(15, TimeUnit.SECONDS);
        }
    }
}
