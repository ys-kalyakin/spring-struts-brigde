package app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.struts.Globals;
import org.apache.struts.spring.testsupport.TomcatTestServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration run of the ported weblegacy apps/blank on embedded Tomcat 11
 * (task 7.2): startup without errors, the welcome-file redirect, the
 * /Welcome.do forward rendering, messages from MessageResources and the
 * plugin lifecycle.
 */
class BlankAppTest {

    static TomcatTestServer server;

    @BeforeAll
    static void startTomcat() throws Exception {
        server = new TomcatTestServer(Path.of("src/main/webapp"), "/blank");
        server.start();
    }

    @AfterAll
    static void stopTomcat() {
        server.close();
    }

    static HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path)).build();
        return HttpClient.newHttpClient().send(request,
            HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void welcomeFileRedirectsToWelcomeAction() throws Exception {
        HttpResponse<String> root = get("/");
        assertEquals(302, root.statusCode(), root.body());
        assertTrue(root.headers().firstValue("Location").orElse("")
            .contains("/blank/Welcome.do"));

        HttpResponse<String> welcome = get("/Welcome.do");
        assertEquals(200, welcome.statusCode(), welcome.body());
        assertTrue(welcome.body().contains("Struts Blank Application"),
            "Title message must render: " + welcome.body());
        assertTrue(welcome.body().contains("Welcome!"),
            "Heading message must render: " + welcome.body());
        assertTrue(welcome.body().contains(
            "To get started on your own application"),
            "Body message must render: " + welcome.body());
    }

    @Test
    void messageResourcesStoredInApplicationContext() {
        Object resources = server.servletContext()
            .getAttribute(Globals.MESSAGES_KEY);
        assertNotNull(resources,
            "MessageResources must be in the application context");
    }

    @Test
    void pluginInitializedAtStartup() {
        assertEquals("true",
            server.servletContext().getAttribute("blank.plugin.init"));
    }

    @Test
    void unknownActionReturns404() throws Exception {
        assertEquals(404, get("/Missing.do").statusCode());
    }
}
