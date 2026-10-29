package org.apache.struts.spring.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

/**
 * Engine-level integration tests of declarative validation (spec
 * struts-validator): ValidatorPlugIn declared through struts-config,
 * ValidatorForm and DynaValidatorForm validated in the request lifecycle.
 */
class ValidatorFlowTest {

    static TomcatTestServer server;
    static HttpClient client;

    @BeforeAll
    static void startTomcat() throws Exception {
        server = new TomcatTestServer(Path.of("src/test/webapp-multipart"),
            "/mp");
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopTomcat() {
        server.close();
    }

    static HttpResponse<String> post(String path, String body)
        throws Exception {
        String encoded = body == null ? "" : body;
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path))
            .header("Content-Type",
                "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encoded))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Test
    void validationErrorsForwardToInputBeforeAction() throws Exception {
        HttpResponse<String> resp = post("/ValidatorSubmit.do",
            "firstName=&zip=12345");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("validator-input-errors:1"),
            resp.body());
    }

    @Test
    void validSubmissionReachesTheAction() throws Exception {
        HttpResponse<String> resp = post("/ValidatorSubmit.do",
            "firstName=Ivan&zip=12345");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("validator-ok:firstName=Ivan"),
            resp.body());
    }

    @Test
    void dynaValidatorFormValidatesInTheLifecycle() throws Exception {
        HttpResponse<String> invalid = post("/DynaValidatorSubmit.do", "");
        assertEquals(200, invalid.statusCode(), invalid.body());
        assertTrue(invalid.body().contains("validator-input-errors:1"),
            invalid.body());

        HttpResponse<String> valid = post("/DynaValidatorSubmit.do",
            "firstName=Petr");
        assertTrue(valid.body().contains("dyna-validator-ok:firstName=Petr"),
            valid.body());
    }
}
