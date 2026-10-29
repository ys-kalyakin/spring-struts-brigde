package org.apache.struts.spring.boot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests of the starter on a real server: a Boot application
 * with the starter runs on embedded Tomcat, and requests pass the full
 * engine lifecycle (config parsing, populate, execute, JSP forward through
 * the tag libraries). Always a real container, never a mock stack.
 */
class StarterBootServerTest {

    private final HttpClient client = HttpClient.newHttpClient();

    @SpringBootApplication
    static class App {

        @Bean
        WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> docRoot() {
            return factory -> factory.setDocumentRoot(
                Path.of("src/test/resources/webapp").toFile());
        }
    }

    @Test
    void extensionMappingLifecycle() throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplication(
                App.class).run("--server.port=0",
                "--spring.struts1.config=classpath:struts-config.xml")) {
            String base = baseUrl(context);

            HttpResponse<String> response =
                get(base + "/welcome.do?name=SpringBoot");
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains(
                    "Welcome from the starter test bundle"),
                "The message bundle must be reachable from the JSP");
            assertTrue(response.body().contains("form-name=SpringBoot"),
                "The request parameter must be populated into the form");

            HttpResponse<String> unknown = get(base + "/unknown.do");
            assertEquals(404, unknown.statusCode(),
                "Unregistered action paths yield 404");

            assertFalse(get(base + "/").statusCode() == 500,
                "Non-action paths are outside the engine");
        }
    }

    @Test
    void pathMappedMappingProperty() throws Exception {
        try (ConfigurableApplicationContext context = new SpringApplication(
                App.class)
                .run("--server.port=0",
                    "--spring.struts1.config=classpath:struts-config.xml",
                    "--spring.struts1.mapping.url-pattern=/action/*")) {
            String base = baseUrl(context);
            HttpResponse<String> response =
                get(base + "/action/welcome?name=PathMapped");
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("form-name=PathMapped"),
                "Path-mapped requests route with the same lifecycle");
        }
    }

    private String baseUrl(ConfigurableApplicationContext context) {
        return "http://localhost:"
            + ((WebServerApplicationContext) context).getWebServer().getPort();
    }

    private HttpResponse<String> get(String url) throws Exception {
        return client.send(
            HttpRequest.newBuilder(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }
}
