package demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests of the demo as an executable Boot application (the
 * <code>java -jar</code> mode): the starter configures the engine without
 * any web.xml, and the demo scenario works end to end. The document root is
 * pointed at the webapp sources the way a packaged executable WAR would
 * carry it.
 */
class DemoBootTest {

    @SpringBootApplication
    static class App {

        @Bean
        WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
            docRoot() {
            return factory -> factory.setDocumentRoot(
                Path.of("src/main/webapp").toFile());
        }
    }

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void bootRunServesTheWholeScenario() throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplication(
                App.class).run("--server.port=0")) {
            String base = "http://localhost:"
                + ((WebServerApplicationContext) context).getWebServer()
                    .getPort();

            HttpResponse<String> form = get(base + "/subscribe.do");
            assertEquals(200, form.statusCode());
            assertTrue(form.body().contains("Subscribe to the demo"),
                "Form page with messages must render: " + form.body());

            HttpResponse<String> invalid = post(base, "");
            assertEquals(200, invalid.statusCode());
            assertTrue(invalid.body().contains("Name is required"),
                "Validation must return to the input page: "
                    + invalid.body());

            HttpResponse<String> valid = post(base,
                "name=Alice&email=alice@example.org");
            assertEquals(200, valid.statusCode());
            assertTrue(valid.body().contains("Subscribed Alice"),
                "The annotated action with the injected service must "
                    + "work: " + valid.body());
        }
    }

    private HttpResponse<String> get(String url) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url))
                .GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String base, String form)
        throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(base + "/subscribeSubmit.do"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
