package org.apache.struts.taglib.it;

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
 * Multi-module routing integration tests: a request to
 * <code>/sub/Hello.do</code> must be routed to the action of the module
 * with prefix <code>/sub</code>, with that module's message resources
 * selected for the tag libraries.
 */
class ModuleRoutingTest {

    static TomcatTestServer server;
    static HttpClient client;

    @BeforeAll
    static void startTomcat() throws Exception {
        server = new TomcatTestServer(Path.of("src/test/webapp"), "/tags");
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopTomcat() {
        server.close();
    }

    static HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path))
            // bean.jsp pages require these to be present
            .header("Accept-Language", "ru-RU")
            .header("Cookie", "flavor=vanilla")
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void routesIntoSubModuleWithItsResources() throws Exception {
        HttpResponse<String> resp = get("/sub/Hello.do");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("sub=[SUB-MODULE-MSG!]"), resp.body());
    }

    @Test
    void defaultModuleStillRoutes() throws Exception {
        HttpResponse<String> resp = get("/view.do?color=red");
        assertEquals(200, resp.statusCode(), resp.body());
    }

    @Test
    void unknownActionYields404() throws Exception {
        assertEquals(404, get("/Unknown.do").statusCode());
        assertEquals(404, get("/sub/Unknown.do").statusCode());
    }
}
