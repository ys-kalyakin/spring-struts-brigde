package org.apache.struts.tiles.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Path;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.struts.spring.testsupport.TomcatTestServer;

/**
 * Integration tests of the tiles pipeline on embedded Tomcat 11: action
 * forwards to definitions (including extends inheritance), a tile controller
 * modifying the context, putList delivery, the localized definition files
 * and the moduleAware factory keys of two modules (spec struts-tiles).
 */
class TilesIntegrationTest {

    static TomcatTestServer server;
    static HttpClient client;

    @BeforeAll
    static void startTomcat() throws Exception {
        server = new TomcatTestServer(Path.of("src/test/webapp-tiles"),
            "/tiles");
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopTomcat() {
        server.close();
        org.apache.struts.tiles.testsupport.TilesStaticReset.resetTilesUtil();
    }

    private static HttpResponse<String> get(String path, String acceptLanguage)
        throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path));
        if (acceptLanguage != null) {
            builder.header("Accept-Language", acceptLanguage);
        }
        return client.send(builder.GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void actionForwardRendersDefinitionWithInheritance() throws Exception {
        HttpResponse<String> resp = get("/Page.do", null);
        assertEquals(200, resp.statusCode(), resp.body());
        // .page extends .layout: base template, overridden title
        assertTrue(resp.body().contains("LAYOUT[TITLE:Page Title]"),
            resp.body());
        // the body put of the base definition is included as a nested tile
        assertTrue(resp.body().contains("BODY-CONTENT:Page Title"),
            resp.body());
    }

    @Test
    void controllerClassAddsAttributeToTileContext() throws Exception {
        HttpResponse<String> resp = get("/ControlledPage.do", null);
        assertEquals(200, resp.statusCode(), resp.body());
        // the definition forward itself carries no attribute the JSP shows;
        // the controller added "extra" to the context before the include
        assertTrue(resp.body().contains("EXTRA:added-by-controller"),
            resp.body());
    }

    @Test
    void putListIsDeliveredToTileController() throws Exception {
        HttpResponse<String> resp = get("/MenuPage.do", null);
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("MENU[ITEMS:[Home, About]"), resp.body());
        // the controller saw the list and reported its size through the
        // context attribute it added
        assertTrue(resp.body().contains("EXTRA:items:2"), resp.body());
    }

    @Test
    void localizedDefinitionsFileIsSelectedByLocale() throws Exception {
        // A locale without its own tiles-defs_xx.xml falls back to the base
        // file. Send it explicitly: the JVM default locale is en on CI and
        // would otherwise select tiles-defs_en for the first request.
        HttpResponse<String> baseResp = get("/LocalePage.do", "ru");
        assertEquals(200, baseResp.statusCode(), baseResp.body());
        assertTrue(baseResp.body().contains("LOCALE[TITLE:Default Title]"),
            baseResp.body());

        HttpResponse<String> englishResp = get("/LocalePage.do", "en");
        assertEquals(200, englishResp.statusCode(), englishResp.body());
        assertTrue(englishResp.body().contains("LOCALE[TITLE:English Title]"),
            englishResp.body());
    }

    @Test
    void tilesActionRunsInsideTileContext() throws Exception {
        // the action URL is the content of the .action-tile definition: the
        // definition forward establishes the tile context, then the TilesAction
        // runs with it (a top-level invocation is rejected by the classic
        // guard) and forwards to the JSP that reads the context
        HttpResponse<String> resp = get("/ActionTilePage.do", null);
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("TILESACTION[TITLE:Tile Action Title]"),
            resp.body());
        assertTrue(!resp.body().contains("LAYOUT["), resp.body());
    }

    @Test
    void moduleAwareFactoryServesBothModules() throws Exception {
        HttpResponse<String> module2Resp = get("/tiles2/Page2.do", null);
        assertEquals(200, module2Resp.statusCode(), module2Resp.body());
        assertTrue(module2Resp.body().contains("LAYOUT2[TITLE:Module2 Title]"),
            module2Resp.body());

        HttpResponse<String> defaultResp = get("/Page.do", null);
        assertTrue(defaultResp.body().contains("LAYOUT[TITLE:Page Title]"),
            defaultResp.body());
    }
}
