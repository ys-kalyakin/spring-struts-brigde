package org.apache.struts.spring.it;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.struts.spring.testsupport.TomcatTestServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test of the configurable suffix (request-processing spec):
 * with the extension configured as <code>.act</code>, <code>/Welcome.act</code>
 * routes to the action and <code>/Welcome.do</code> does not.
 */
class CustomSuffixTest {

    static TomcatTestServer server;

    @BeforeAll
    static void startTomcat() throws Exception {
        server = new TomcatTestServer(Path.of("src/test/webapp2"), "/two");
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
    void customSuffixRoutesAndDefaultSuffixDoesNot() throws Exception {
        HttpResponse<String> welcome = get("/Welcome.act");
        assertEquals(200, welcome.statusCode(), welcome.body());
        assertTrue(welcome.body().contains("SUFFIX-WELCOME-JSP"));

        HttpResponse<String> plain = get("/Plain.act");
        assertEquals(200, plain.statusCode(), plain.body());
        assertTrue(plain.body().contains("plain-ok:/Plain"));

        // The default .do suffix is not routed in this application
        assertEquals(404, get("/Welcome.do").statusCode());
    }
}
