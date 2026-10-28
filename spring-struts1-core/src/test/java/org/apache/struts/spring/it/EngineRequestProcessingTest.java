package org.apache.struts.spring.it;

import java.net.CookieManager;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Request-level integration tests of the engine on embedded Tomcat 11,
 * covering the specs request-processing, exception-handling,
 * spring-bean-integration, action-servlet-facade and the request-flow part
 * of message-resources.
 */
class EngineRequestProcessingTest {

    static TomcatTestServer server;
    static HttpClient plainClient;
    static HttpClient sessionClient;

    @BeforeAll
    static void startTomcat() throws Exception {
        server = new TomcatTestServer(Path.of("src/test/webapp"), "/it");
        server.start();
        plainClient = HttpClient.newHttpClient();
        sessionClient = HttpClient.newBuilder()
            .cookieHandler(new CookieManager()).build();
    }

    @AfterAll
    static void stopTomcat() {
        server.close();
    }

    static HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path)).build();
        return plainClient.send(request,
            HttpResponse.BodyHandlers.ofString());
    }

    static HttpResponse<String> getSession(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path)).build();
        return sessionClient.send(request,
            HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void routesRegisteredPathAndReturns404ForUnknown() throws Exception {
        HttpResponse<String> resp = get("/Plain.do");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("plain-ok:/Plain"));

        assertEquals(404, get("/Missing.do").statusCode());
    }

    @Test
    void mappingForwardAttributeDispatchesWithoutAction() throws Exception {
        // Welcome action declares only a forward attribute and no type
        HttpResponse<String> resp = get("/Welcome.do");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("WELCOME-JSP"), resp.body());
    }

    @Test
    void populateFillsFormProperties() throws Exception {
        HttpResponse<String> resp = get("/FormRequest.do?name=Ivan&age=33");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("name=Ivan:age=33"), resp.body());
    }

    @Test
    void requestScopeGetsFreshFormPerRequest() throws Exception {
        String first = get("/FormRequest.do?name=A").body();
        String second = get("/FormRequest.do?name=B").body();
        String id1 = first.split(":")[1];
        String id2 = second.split(":")[1];
        assertNotEquals(id1, id2, "Request scope must create a fresh form: "
            + first + " vs " + second);
    }

    @Test
    void sessionScopeReusesFormAcrossRequests() throws Exception {
        HttpResponse<String> first = getSession("/FormSession.do?name=A");
        HttpResponse<String> second = getSession("/FormSession.do?name=B");
        String id1 = first.body().split(":")[1];
        String id2 = second.body().split(":")[1];
        assertEquals(id1, id2, "Session scope must reuse the form");
        assertTrue(second.body().contains("name=B"),
            "Populate must overwrite on the second request");
    }

    @Test
    void validationErrorsForwardToInputWithoutExecute() throws Exception {
        HttpResponse<String> resp = getSession("/Validated.do?name=");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("input-errors:1"), resp.body());
    }

    @Test
    void successfulValidationExecutesAction() throws Exception {
        HttpResponse<String> resp = getSession("/Validated.do?name=Ok");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("form:") && resp.body()
            .contains("validateCalled=true"),
            "Execute must run after successful validation: " + resp.body());
        assertFalse(resp.body().contains("input-errors"),
            "Must not land on the input page");
    }

    @Test
    void validateFalseSkipsValidation() throws Exception {
        HttpResponse<String> resp = getSession("/NoValidate.do?name=");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("validateCalled=false"), resp.body());
    }

    @Test
    void localForwardWinsOverGlobal() throws Exception {
        assertTrue(get("/LocalForward.do").body().contains("LOCAL-DONE-JSP"));
        assertTrue(get("/GlobalForward.do").body()
            .contains("GLOBAL-DONE-JSP"));
    }

    @Test
    void redirectForwardSends302() throws Exception {
        HttpResponse<String> resp = get("/Redirect.do");
        assertEquals(302, resp.statusCode(), resp.headers().toString());
        String location = resp.headers().firstValue("Location").orElse("");
        assertTrue(location.contains("/done.jsp"), location);

        // Follow the redirect (strip any jsessionid suffix) to the target
        String clean = location.split(";")[0];
        String path = clean.startsWith("/it") ? clean.substring(3) : clean;
        HttpResponse<String> followed = get(path);
        assertTrue(followed.body().contains("GLOBAL-DONE-JSP"));
    }

    @Test
    void dispatchForwardRendersInSameRequest() throws Exception {
        HttpResponse<String> resp = get("/GlobalForward.do");
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("GLOBAL-DONE-JSP"));
    }

    @Test
    void declaredExceptionForwardsWithMessage() throws Exception {
        HttpResponse<String> resp = get("/Boom.do");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains(
            "global-error:TestException:boom-from-/Boom:errors=1"),
            resp.body());
    }

    @Test
    void localExceptionHandlerWins() throws Exception {
        HttpResponse<String> resp = get("/LocalBoom.do");
        assertTrue(resp.body().contains("local-error:boom-from-/LocalBoom"),
            resp.body());
    }

    @Test
    void exceptionSubtypeHandledByDeclaration() throws Exception {
        HttpResponse<String> resp = get("/SubBoom.do");
        assertTrue(resp.body().contains("global-error:SubTestException"),
            resp.body());
    }

    @Test
    void undeclaredExceptionReachesContainer() throws Exception {
        HttpResponse<String> resp = get("/Uncaught.do");
        assertEquals(500, resp.statusCode(),
            "Undeclared exception must propagate to the container");
    }

    @Test
    void annotatedActionIsSingletonWithInjectedDependency() throws Exception {
        HttpResponse<String> first = get("/Annotated.do");
        HttpResponse<String> second = get("/Annotated.do");
        assertTrue(first.body().startsWith("annotated:"), first.body());
        assertTrue(first.body().endsWith(":greeted-by-service"),
            "Dependency must be injected: " + first.body());
        assertEquals(first.body(), second.body(),
            "Singleton action must be reused between requests");
    }

    @Test
    void annotatedFormIsPrototypeWithInjectedDependency() throws Exception {
        HttpResponse<String> first = get("/AnnotatedForm.do");
        HttpResponse<String> second = get("/AnnotatedForm.do");
        String id1 = first.body().split(":")[1];
        String id2 = second.body().split(":")[1];
        assertNotEquals(id1, id2, "Prototype form must be fresh per request");
        assertTrue(first.body().contains(":annotated-form:greeted-by-service"),
            "Form dependency must be injected: " + first.body());
    }

    @Test
    void facadeAccessibleFromAction() throws Exception {
        HttpResponse<String> resp = get("/ServletCheck.do");
        assertTrue(resp.body()
            .contains("servlet:true:config=/WEB-INF/struts-config.xml"),
            resp.body());
    }

    @Test
    void messagesResolvedWithSessionLocaleAndMissingKeyFormat()
        throws Exception {
        HttpResponse<String> resp = getSession("/Msg.do?setLocale=ru_RU");
        assertTrue(resp.body().contains(
            "msg:locale=ru_RU" + ":Привет движок!"
                + ":???ru_RU.it.missing???"), resp.body());
    }

    @Test
    void pluginsInitializedAtStartup() {
        assertEquals("direct:init",
            server.servletContext().getAttribute("plugin.direct"));
        assertEquals("greeted-by-service:init",
            server.servletContext().getAttribute("plugin.spring"),
            "Spring-registered plugin must be resolved from the context "
                + "with DI");
    }
}
