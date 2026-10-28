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
 * Path-mapped servlet mapping integration tests: with the servlet mapping
 * <code>/action/*</code> the request <code>/action/Welcome</code> resolves
 * to the mapping registered under path <code>/Welcome</code>.
 */
class PathMappedRoutingTest {

    static TomcatTestServer server;
    static HttpClient client;

    @BeforeAll
    static void startTomcat() throws Exception {
        server = new TomcatTestServer(Path.of("src/test/webapp-path"), "/tagsp");
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopTomcat() {
        server.close();
    }

    static HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void routesViaPathPrefix() throws Exception {
        HttpResponse<String> resp = get("/action/Welcome");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("TAG-WELCOME-JSP"), resp.body());
    }

    @Test
    void extensionIsOptionalInPathMapping() throws Exception {
        HttpResponse<String> resp = get("/action/Welcome.do");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("TAG-WELCOME-JSP"), resp.body());
    }

    @Test
    void unknownActionYields404() throws Exception {
        assertEquals(404, get("/action/Missing").statusCode());
    }
}
