package examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.struts.spring.testsupport.TomcatTestServer;

/**
 * Integration tests of the cookbook application on embedded Tomcat 11:
 * the DynaValidatorForm rules (byte, integer, range, creditCard, email),
 * the custom twofields validator, the multibox and localization sections
 * and the transactional token handling (spec cookbook).
 */
class CookbookAppTest {

    static TomcatTestServer server;
    static HttpClient client;

    @BeforeAll
    static void startTomcat() throws Exception {
        server = new TomcatTestServer(Path.of("src/main/webapp"), "/cookbook");
        server.start();
        // the transactional token lives in the session: keep cookies between
        // requests
        client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .build();
    }

    @AfterAll
    static void stopTomcat() {
        server.close();
    }

    private static HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(String path, String form)
        throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form,
                StandardCharsets.UTF_8))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static final Pattern TOKEN_PARAM = Pattern.compile(
        "name=\"org\\.apache\\.struts\\.taglib\\.html\\.TOKEN\"\\s*value=\"([^\"]+)\"");

    @Test
    void validatorRejectsInvalidDynaForm() throws Exception {
        HttpResponse<String> resp = post("/processValidator.do",
            "byteValue=abc&integerValue=abc&range=5&creditCard=notacard"
            + "&email=bad&password=pass1&password2=different");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("must be a byte."), resp.body());
        assertTrue(resp.body().contains("must be an integer."), resp.body());
        assertTrue(resp.body().contains("is not in the range"), resp.body());
        assertTrue(resp.body()
            .contains("is an invalid credit card number."), resp.body());
        assertTrue(resp.body().contains("is an invalid e-mail address."),
            resp.body());
        // the custom twofields validator loaded from validation.xml
        assertTrue(resp.body().contains("must have the same value as the"),
            resp.body());
    }

    @Test
    void validatorAcceptsValidDynaForm() throws Exception {
        HttpResponse<String> resp = post("/processValidator.do",
            "byteValue=10&shortValue=20&integerValue=500&longValue=600"
            + "&floatValue=1.5&doubleValue=2.5&range=500"
            + "&creditCard=4111111111111111&date=12%2F31%2F2026"
            + "&email=test%40example.com&mask=12345&min=abcde&max=abc"
            + "&required=needed&password=abcdef&password2=abcdef");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("Validator Example Results"),
            resp.body());
        assertTrue(!resp.body().contains("must be a byte."), resp.body());
    }

    @Test
    void tokenPreventsDuplicateSubmission() throws Exception {
        HttpResponse<String> prepared = get("/prepareToken.do");
        assertEquals(200, prepared.statusCode(), prepared.body());
        Matcher matcher = TOKEN_PARAM.matcher(prepared.body());
        assertTrue(matcher.find(), "the form must carry the transaction token");
        String token = matcher.group(1);

        HttpResponse<String> first = post("/processToken.do",
            "org.apache.struts.taglib.html.TOKEN=" + token + "&name=first");
        assertEquals(200, first.statusCode(), first.body());
        assertTrue(first.body().contains("Token Example Results"),
            first.body());

        // the same token again (or none at all): out of sequence
        HttpResponse<String> duplicate = post("/processToken.do",
            "org.apache.struts.taglib.html.TOKEN=" + token + "&name=again");
        assertEquals(200, duplicate.statusCode(), duplicate.body());
        assertTrue(duplicate.body()
            .contains("Operation is not in sequence."), duplicate.body());
    }

    @Test
    void localizationSwitchesLanguage() throws Exception {
        HttpResponse<String> english = get("/prepareLocalization.do");
        assertEquals(200, english.statusCode(), english.body());
        assertTrue(english.body()
            .contains("Welcome to the examples page."), english.body());

        HttpResponse<String> french = get(
            "/processLocalization.do?language=fr");
        assertEquals(200, french.statusCode(), french.body());
        assertTrue(french.body().contains("Bienvenue"), french.body());
    }

    @Test
    void multiboxCollectsSelections() throws Exception {
        HttpResponse<String> prepared = get("/prepareMultibox.do");
        assertEquals(200, prepared.statusCode(), prepared.body());
        assertTrue(prepared.body().contains("Apple"), prepared.body());

        HttpResponse<String> resp = post("/processMultibox.do",
            "fruits=Apple&fruits=Orange&colors=Red");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("Apple"), resp.body());
        assertTrue(resp.body().contains("Orange"), resp.body());
        assertTrue(resp.body().contains("Red"), resp.body());
    }
}
