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
 * Rendering integration tests of the bean/html/logic/nested tag libraries
 * against real JSP pages on embedded Tomcat 11.
 */
class TagRenderingTest {

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
            // bean:header/bean:cookie on the test page throw when the data
            // is absent, so every request carries the expected values.
            .header("Accept-Language", "ru-RU")
            .header("Cookie", "flavor=vanilla")
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void beanWriteFiltersEscapesAndMessages() throws Exception {
        HttpResponse<String> resp = get(
            "/view.do?color=red&method=GET");
        assertEquals(200, resp.statusCode(), resp.body());
        String body = resp.body();

        assertTrue(body.contains("name=[Ivan]"), body);
        assertTrue(body.contains("html=[&lt;b&gt;bold&lt;/b&gt;]"), body);
        assertTrue(body.contains("htmlRaw=[<b>bold</b>]"), body);
        assertTrue(body.contains("msg=[Hello taglib!]"), body);
        assertTrue(body.contains("msgArg=[Hello, Ivan!]"), body);
        assertTrue(body.contains("msgAlt=[ALT-MSG!]"), body);
        assertTrue(body.contains("defined=[Ivan]"), body);
        assertTrue(body.contains("param=[red]"), body);
        assertTrue(body.contains("header=[ru-RU]"), body);
        assertTrue(body.contains("cookie=[vanilla]"), body);
        assertTrue(body.contains("size=[3]"), body);
    }

    @Test
    void htmlFormRendersFormControls() throws Exception {
        HttpResponse<String> resp = get("/formSubmit.do?name=Petr&color=red"
            + "&active=on&token=t-42");
        assertEquals(200, resp.statusCode(), resp.body());
        String body = resp.body();

        assertTrue(body.contains("action=\"/tags/formSubmit.do"), body);
        assertTrue(body.contains("type=\"text\" name=\"name\" value=\"Petr\""),
            body);
        assertTrue(body.contains("<select name=\"color\""), body);
        assertTrue(body.contains("value=\"red\" selected=\"selected\""), body);
        assertTrue(body.contains("type=\"checkbox\" name=\"active\""), body);
        assertTrue(body.contains("checked=\"checked\""), body);
        assertTrue(body.contains("type=\"hidden\" name=\"token\" value=\"t-42\""),
            body);
        assertTrue(body.contains("type=\"submit\""), body);
    }

    @Test
    void xhtmlModeRendersXmlSyntax() throws Exception {
        HttpResponse<String> resp = get("/xhtmlPage.do");
        assertEquals(200, resp.statusCode(), resp.body());
        String body = resp.body();

        assertTrue(body.contains("<html xmlns=\"http://www.w3.org/1999/xhtml\""),
            body);
        assertTrue(body.contains("/>"), body);
    }

    @Test
    void errorsRenderWithBundleDecoration() throws Exception {
        HttpResponse<String> resp = get("/errorPage.do");
        assertEquals(200, resp.statusCode(), resp.body());
        String body = resp.body();

        assertTrue(body.contains(
            "all=[<UL><LI>Name is required</LI><LI>Value is invalid</LI></UL>]"),
            body);
        assertTrue(body.contains("nameOnly=[<UL><LI>Name is required</LI></UL>]"),
            body);
        assertTrue(body.contains("msgs=[Name is required;Value is invalid;]"),
            body);
        assertTrue(body.contains("msgPresent=[Y]"), body);
    }

    @Test
    void linkRewriteAndImgRender() throws Exception {
        HttpResponse<String> resp = get("/link.do");
        assertEquals(200, resp.statusCode(), resp.body());
        String body = resp.body();

        assertTrue(
            body.contains("linkFwd=[<a href=\"/tags/welcome.jsp;jsessionid="),
            body);
        assertTrue(body.contains("linkHref=[<a href=\"/plain.html\">H</a>]"),
            body);
        assertTrue(body.contains("linkParam=[<a href=\"/list.do?id=7\">L</a>]"),
            body);
        assertTrue(body.contains("rewrite=[/tags/welcome.jsp;jsessionid="), body);
        assertTrue(body.contains("src=\"/img/logo.gif\""), body);
    }

    @Test
    void logicIterateAndComparisons() throws Exception {
        HttpResponse<String> resp = get("/logicPage.do?page=3");
        assertEquals(200, resp.statusCode(), resp.body());
        String body = resp.body();

        assertTrue(body.contains("iter=[A;B;C;]"), body);
        assertTrue(body.contains("idx=[0,1,2,]"), body);
        assertTrue(body.contains("off=[B,C,]"), body);
        assertTrue(body.contains("eqParam=[Y]"), body);
        assertTrue(body.contains("neProp=[Y]"), body);
        assertTrue(body.contains("matchStart=[Y]"), body);
        assertTrue(body.contains("empty=[Y]"), body);
        assertTrue(body.contains("presentMsg=[Y]"), body);
        assertTrue(body.contains("notPresentReq=[Y]"), body);
        assertTrue(body.contains("msgNotPresent=[Y]"), body);
    }

    @Test
    void nestedRendersFormAndIterate() throws Exception {
        HttpResponse<String> resp = get("/nestedPage.do");
        assertEquals(200, resp.statusCode(), resp.body());
        String body = resp.body();

        assertTrue(body.contains("action=\"/tags/formSubmit.do"), body);
        assertTrue(body.contains("type=\"text\" name=\"name\""), body);
        assertTrue(body.contains("items=[[child-A]]"), body);
        assertTrue(body.contains("root=[Ivan]"), body);
    }

    @Test
    void logicRedirectSends302ToForward() throws Exception {
        HttpResponse<String> resp = get("/redirectTest.do");
        assertEquals(302, resp.statusCode(), resp.body());
        String location = resp.headers().firstValue("Location").orElse("")
            .split(";")[0];
        assertEquals("/tags/welcome.jsp", location);
    }

    @Test
    void javascriptTagFailsWithoutValidator() throws Exception {
        // html:javascript is excluded from this phase (needs commons-validator):
        // the page must fail loudly instead of rendering silently.
        assertEquals(500, get("/javascriptTest.do").statusCode());
    }
}
