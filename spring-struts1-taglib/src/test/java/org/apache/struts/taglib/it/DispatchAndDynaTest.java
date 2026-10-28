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
 * Dispatch actions and DynaActionForm integration tests: the classic
 * DispatchAction family (method parameter, lookup by button label, mapping
 * parameter, event rules, ActionDispatcher) and form population of
 * <code>DynaActionForm</code> beans.
 */
class DispatchAndDynaTest {

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
            URI.create(server.baseUrl() + path)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    static HttpResponse<String> post(String path, String form)
        throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void dispatchByMethodParameter() throws Exception {
        assertEquals("method:save",
            get("/dispatch.do?method=save").body());
        assertEquals("method:cancel",
            get("/dispatch.do?method=cancel").body());
    }

    @Test
    void dispatchWithoutParameterHitsUnspecified() throws Exception {
        assertEquals("method:unspecified", get("/dispatch.do").body());
    }

    @Test
    void dispatchWithUnknownMethodFails() throws Exception {
        assertEquals(500, get("/dispatch.do?method=nothing").statusCode());
    }

    @Test
    void lookupDispatchByButtonLabel() throws Exception {
        assertEquals("method:save", post("/lookup.do", "submit=Save").body());
        assertEquals("method:cancel",
            post("/lookup.do", "submit=Cancel").body());
    }

    @Test
    void mappingDispatchSelectsMethodPerMapping() throws Exception {
        assertEquals("method:edit", get("/mappingEdit.do").body());
        assertEquals("method:save", get("/mappingSave.do").body());
    }

    @Test
    void eventDispatchMatchesParametersAndDefault() throws Exception {
        assertEquals("method:save", post("/event.do", "save=").body());
        assertEquals("method:cancel", post("/event.do", "cancel=").body());
        assertEquals("method:go", post("/event.do", "").body());
        assertEquals("method:save", post("/event.do", "save.x=1").body());
    }

    @Test
    void plainActionUsesDispatcherMappingFlavor() throws Exception {
        assertEquals("method:go", get("/plainDispatch.do").body());
    }

    @Test
    void dynaFormPopulatesAndUsesInitialValues() throws Exception {
        // JSP output carries leading/trailing newlines
        assertEquals("dyna:testString=:status=new",
            get("/dynaCheck.do").body().trim());
        assertEquals("dyna:testString=hi:status=set",
            get("/dynaCheck.do?testString=hi&status=set").body().trim());
    }
}
