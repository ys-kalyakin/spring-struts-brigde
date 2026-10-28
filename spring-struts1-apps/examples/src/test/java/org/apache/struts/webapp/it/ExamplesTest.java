package org.apache.struts.webapp.it;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.struts.spring.testsupport.TomcatTestServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ported Struts Examples application integration tests on embedded Tomcat
 * 11: the exercise module pages render their classic HTML structure and the
 * dispatch module scenarios execute their methods through the classic
 * dispatch actions.
 *
 * <p>The ported application intentionally registers only the default,
 * exercise and dispatch modules (no upload/validator modules in this
 * phase). Consequently the pages that reference the excluded modules are
 * out of scope: html-link.jsp (links /upload and /validator) and
 * bean-include.jsp (includes /welcome.jsp, which links /validator). Their
 * tag-level behavior is covered by the taglib module ITs.</p>
 */
class ExamplesTest {

    static TomcatTestServer server;
    static HttpClient client;

    @BeforeAll
    static void startTomcat() throws Exception {
        server = new TomcatTestServer(Path.of("src/main/webapp"), "/examples");
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

    /** The page must answer 200 and carry its classic structural marker. */
    static void assertPage(String path, String marker) throws Exception {
        HttpResponse<String> resp = get(path);
        assertTrue(resp.statusCode() == 200,
            path + " must render: " + resp.body());
        assertTrue(resp.body().contains(marker),
            path + " must contain '" + marker + "': " + resp.body());
    }

    @Test
    void exerciseModuleWelcome() throws Exception {
        assertPage("/exercise/welcome.do", "<");
    }

    @Test
    void beanTagExercisePagesRender() throws Exception {
        assertPage("/exercise/bean-cookie.jsp",
            "Test struts-bean:cookie Tag");
        assertPage("/exercise/bean-define.jsp",
            "Test struts-bean:define Tag");
        assertPage("/exercise/bean-header.jsp",
            "Test struts-bean:header Tag");
        assertPage("/exercise/bean-parameter.jsp?param1=value1"
            + "&param2=value2", "Test struts-bean:parameter Tag");
        assertPage("/exercise/bean-resource.jsp",
            "Test struts-bean:resource Tag");
        assertPage("/exercise/bean-size.jsp", "Test struts-bean Size Tag");
        assertPage("/exercise/bean-write.jsp", "Test struts-bean:write Tag");
        assertPage("/exercise/bean-write-2.do", "Test struts-bean");
    }

    @Test
    void htmlTagExercisePagesRender() throws Exception {
        assertPage("/exercise/html-button.do", "Test html:button Tag");
        assertPage("/exercise/html-cancel.do", "Test");
        assertPage("/exercise/html-form.do", "<form name=\"testbean\"");
        assertPage("/exercise/html-image.do", "Test struts-html:image Tag");
        assertPage("/exercise/html-img.do", "<img src=");
        assertPage("/exercise/html-messages.do", "Property 1, Message 1");
        assertPage("/exercise/html-multibox.do", "Test html:multibox Tag");
        assertPage("/exercise/html-select.do", "Test html:select Tag");
        assertPage("/exercise/html-setters.do",
            "Test struts-html Property Setters");
    }

    @Test
    void logicTagExercisePagesRender() throws Exception {
        assertPage("/exercise/logic-compare.jsp",
            "Test struts-logic Comparison Tags");
        assertPage("/exercise/logic-compare-numeric.jsp",
            "Test struts-logic Comparison Tags (Numeric)");
        assertPage("/exercise/logic-empty.jsp",
            "Test struts-logic Emptiness Tags");
        assertPage("/exercise/logic-iterate.jsp",
            "Test struts-logic Iterate Tag");
        assertPage("/exercise/logic-match.jsp?param1=value1",
            "Test struts-logic Match Tags");
        assertPage("/exercise/logic-present.jsp?param1=value1",
            "Test struts-logic Presence Tags");
    }

    @Test
    void logicIterateRendersTheBeanArray() throws Exception {
        HttpResponse<String> resp = get("/exercise/logic-iterate.jsp");
        // String 4 is the last element of TestBean.stringArray: the whole
        // array was iterated.
        assertTrue(resp.body().contains("String 4"), resp.body());
    }

    @Test
    void dispatchByMethodParameter() throws Exception {
        assertPage("/dispatch/dispatch-submit.do?"
            + "dispatchMethod=doFoo", "Foo button count = 1");
    }

    @Test
    void lookupDispatchByButtonLabel() throws Exception {
        String label = URLEncoder.encode("Foo Button",
            StandardCharsets.UTF_8);
        assertPage("/dispatch/lookup-submit.do?dispatchParam=" + label,
            "Foo button count = 1");
    }

    @Test
    void mappingDispatchPerMapping() throws Exception {
        assertPage("/dispatch/mapping-foo.do", "Foo button count = 1");
        assertPage("/dispatch/mapping-bar.do", "Bar button count = 1");
    }

    @Test
    void actionDispatcherScenario() throws Exception {
        assertPage("/dispatch/actionDispatcher-submit.do?"
            + "actionDispatcherMethod=doBar", "Bar button count = 1");
    }

    @Test
    void eventActionAndEventDispatcherScenarios() throws Exception {
        assertPage("/dispatch/eventAction-submit.do?doFoo=",
            "Foo button count = 1");
        // eventAction-default has no matching request parameter: the
        // default=doBar alias selects the method.
        assertPage("/dispatch/eventAction-default.do",
            "Bar button count = 1");
        assertPage("/dispatch/eventDispatcher-submit.do?doBar=",
            "Bar button count = 1");
    }

    @Test
    void dispatchWithoutParameterIsHandledByDeclarativeException()
        throws Exception {
        // The mapping declares an exception handler for
        // jakarta.servlet.ServletException that forwards to dispatch.jsp.
        assertPage("/dispatch/dispatch-noparam.do",
            "DispatchAction Example");
    }
}
