package demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.struts.spring.testsupport.TomcatTestServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests of the migration demo on a real container: the whole
 * demo works through the starter — annotated action (singleton with an
 * injected service), prototype form, validation, message resources, tag
 * libraries.
 */
class DemoAppTest {

    private static final Pattern ACTION_INSTANCE =
        Pattern.compile("action-instance=([0-9a-f-]+)");

    private static final Pattern FORM_INSTANCE =
        Pattern.compile("form-instance=([0-9a-f-]+)");

    private final HttpClient client = HttpClient.newHttpClient();

    private TomcatTestServer server;

    @BeforeEach
    void startServer() throws Exception {
        server = new TomcatTestServer(
            Path.of("src/main/webapp"), "");
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.close();
    }

    private String baseUrl() {
        return server.baseUrl();
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(
                URI.create(baseUrl() + path)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String form) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(baseUrl() + "/subscribeSubmit.do"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void formPageRendersWithMessagesAndTags() throws Exception {
        HttpResponse<String> response = get("/subscribe.do");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subscribe to the demo"),
            "The message bundle must be reachable: " + response.body());
        assertTrue(response.body().contains("subscribeSubmit.do"),
            "The html:form tag must render the action URL: "
                + response.body());
    }

    @Test
    void validationErrorsReturnToInputPage() throws Exception {
        HttpResponse<String> response = post("");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Name is required"),
            "Validation errors must be shown on the input page: "
                + response.body());
        assertTrue(response.body().contains("A valid email is required"),
            "Validation errors must be shown on the input page: "
                + response.body());
    }

    @Test
    void annotatedActionWorksWithInjectedService() throws Exception {
        HttpResponse<String> response = post("name=Alice&email=alice@example.org");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subscribed Alice"),
            "The injected service must produce the greeting: "
                + response.body());
        assertTrue(response.body().contains("alice@example.org"),
            "The email must reach the success page: " + response.body());
    }

    @Test
    void singletonActionAndPrototypeForm() throws Exception {
        HttpResponse<String> first =
            post("name=Alice&email=alice@example.org");
        HttpResponse<String> second =
            post("name=Bob&email=bob@example.org");
        assertEquals(200, first.statusCode());
        assertEquals(200, second.statusCode());

        String firstAction = instanceId(first.body(), ACTION_INSTANCE);
        String secondAction = instanceId(second.body(), ACTION_INSTANCE);
        assertEquals(firstAction, secondAction,
            "The @StrutsAction bean must be a singleton across requests");

        String firstForm = instanceId(first.body(), FORM_INSTANCE);
        String secondForm = instanceId(second.body(), FORM_INSTANCE);
        assertNotEquals(firstForm, secondForm,
            "Every request must receive its own @StrutsForm instance");
    }

    private String instanceId(String body, Pattern pattern) {
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.find(), "Instance marker missing in: " + body);
        return matcher.group(1);
    }
}
