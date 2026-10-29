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
 * 11: the exercise module pages render their classic HTML structure, the
 * dispatch module scenarios execute their methods through the classic
 * dispatch actions, the upload module runs the multipart pipeline (including
 * the graceful two-megabyte limit) and the validator module runs the
 * registration, multi-page registration, validwhen, bundles and
 * client-javascript scenarios.
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

    @Test
    void springIntegrationShowcase() throws Exception {
        HttpResponse<String> resp = get(
            "/SpringIntegration.do?label=from-query");
        assertTrue(resp.statusCode() == 200, resp.body());

        // EL resolves the Spring bean by name through the engine.
        assertTrue(resp.body().contains("el-bean:injected-service-ok"),
            resp.body());
        // spring:message renders from the Struts bundle via MessageSource.
        assertTrue(resp.body().contains("spring-message:Spring demo message"),
            resp.body());
        // The unannotated form is a Spring prototype: dependency injected,
        // @PostConstruct executed before populate.
        assertTrue(resp.body()
                .contains("di-form:injected-service-ok:postconstructed"),
            resp.body());
        // html:errors renders through the Spring errors renderer.
        assertTrue(resp.body()
                .contains("errors:<UL><LI>Demo error message</LI></UL>"),
            resp.body());
        // html:form/html:text build the URL and take the value from the
        // populated form.
        assertTrue(resp.body().contains("value=\"from-query\""),
            resp.body());
        assertTrue(resp.body()
            .contains("action=\"/examples/SpringIntegration.do"),
            resp.body());
    }

    // ------------------------------------------------------------------
    // upload module
    // ------------------------------------------------------------------

    private static final String UPLOAD_BOUNDARY = "examples-it-boundary";

    private static byte[] multipartBody(String textName, String textValue,
        String fileField, String[] fileNames) {
        StringBuilder sb = new StringBuilder();
        if (textName != null) {
            sb.append("--").append(UPLOAD_BOUNDARY).append("\r\n")
                .append("Content-Disposition: form-data; name=\"")
                .append(textName).append("\"\r\n\r\n")
                .append(textValue).append("\r\n");
        }
        if (fileField != null) {
            for (String fileName : fileNames) {
                sb.append("--").append(UPLOAD_BOUNDARY).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"")
                    .append(fileField).append("\"; filename=\"")
                    .append(fileName).append("\"\r\n")
                    .append("Content-Type: text/plain\r\n\r\n")
                    .append("payload-of-").append(fileName).append("\r\n");
            }
        }
        sb.append("--").append(UPLOAD_BOUNDARY).append("--\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static HttpResponse<String> postMultipart(String path,
        byte[] body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path))
            .header("Content-Type",
                "multipart/form-data; boundary=" + UPLOAD_BOUNDARY)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void uploadSubmitShowsTextAndFirstFile() throws Exception {
        HttpResponse<String> resp = postMultipart("/upload/upload-submit.do",
            multipartBody("theText", "hello", "theFile",
                new String[] {"report.txt"}));
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("The Text:</b>&nbsp;hello"),
            resp.body());
        assertTrue(resp.body().contains("The File name:</b>&nbsp;report.txt"),
            resp.body());
        assertTrue(resp.body().contains("The File content type:</b>&nbsp;text/plain"),
            resp.body());
        assertTrue(resp.body().contains("payload-of-report.txt"), resp.body());
    }

    @Test
    void uploadOverTwoMegabytesShowsGracefulMessage() throws Exception {
        StringBuilder bigFile = new StringBuilder();
        while (bigFile.length() < 3 * 1024 * 1024) {
            bigFile.append("0123456789abcdef");
        }
        // the single part replaces the small payload with a huge one
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(UPLOAD_BOUNDARY).append("\r\n")
            .append("Content-Disposition: form-data; name=\"theText\"\r\n\r\n")
            .append("boom\r\n");
        sb.append("--").append(UPLOAD_BOUNDARY).append("\r\n")
            .append("Content-Disposition: form-data; name=\"theFile\"; filename=\"big.bin\"\r\n")
            .append("Content-Type: application/octet-stream\r\n\r\n")
            .append(bigFile).append("\r\n");
        sb.append("--").append(UPLOAD_BOUNDARY).append("--\r\n");
        HttpResponse<String> resp = postMultipart("/upload/upload-submit.do",
            sb.toString().getBytes(StandardCharsets.UTF_8));
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body()
            .contains("The maximum upload length has been exceeded by the client."),
            resp.body());
    }

    @Test
    void uploadMultiCountsSelectedFiles() throws Exception {
        HttpResponse<String> resp = postMultipart(
            "/upload/upload-submit-multi.do",
            multipartBody("theText", "multi", "theFile",
                new String[] {"a.txt", "b.txt"}));
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("Number of selected files:</b>&nbsp;2"),
            resp.body());
        assertTrue(resp.body().contains("The File name:</b>&nbsp;a.txt"),
            resp.body());
    }

    // ------------------------------------------------------------------
    // validator module
    // ------------------------------------------------------------------

    @Test
    void registrationEmptySubmitShowsRequiredErrors() throws Exception {
        // stopOnFirstError only affects the generated javascript; the
        // server side reports every field error
        HttpResponse<String> resp = post("/validator/registration-submit.do",
            "firstName=&lastName=&email=");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("First Name is required."), resp.body());
        assertTrue(resp.body().contains("E-mail is required."), resp.body());
    }

    @Test
    void registrationValidSubmitSucceeds() throws Exception {
        HttpResponse<String> resp = post("/validator/registration-submit.do",
            "firstName=Tester&lastName=User&addr=1+Main+St"
            + "&cityStateZip.city=Springfield&cityStateZip.stateProv=IL"
            + "&cityStateZip.zipPostal%5B1%5D=62704&phone=555-1234567"
            + "&email=test%40example.com");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("Struts Validator"), resp.body());
        assertTrue(!resp.body().contains("is required"), resp.body());
    }

    @Test
    void multiRegistrationPageFlow() throws Exception {
        // invalid page 1 -> back to the page 1 input
        HttpResponse<String> invalid = post(
            "/validator/multiRegistration-submit.do",
            "page=1&firstName=&lastName=&addr=");
        assertEquals(200, invalid.statusCode(), invalid.body());
        assertTrue(invalid.body().contains("First Name is required."),
            invalid.body());

        // valid page 1 fields but missing page 2 fields: the action calls
        // validate() itself and (without accept-page, the weblegacy default)
        // all pages are validated — the page 2 errors route back to input 1
        HttpResponse<String> page1 = post(
            "/validator/multiRegistration-submit.do",
            "page=1&firstName=Tester&lastName=User&addr=1+Main+St"
            + "&cityStateZip.city=Springfield");
        assertEquals(200, page1.statusCode(), page1.body());
        assertTrue(page1.body().contains("State is required."), page1.body());
        assertTrue(page1.body().contains("E-mail is required."), page1.body());

        // valid page 2 (page 1 fields still submitted) -> success
        HttpResponse<String> done = post(
            "/validator/multiRegistration-submit.do",
            "page=2&firstName=Tester&lastName=User&addr=1+Main+St"
            + "&cityStateZip.city=Springfield&cityStateZip.stateProv=IL"
            + "&cityStateZip.zipPostal%5B1%5D=62704&phone=555-1234567"
            + "&email=test%40example.com");
        assertEquals(200, done.statusCode(), done.body());
        assertTrue(done.body().contains("Struts Validator"), done.body());
    }

    @Test
    void validwhenRejectsMaxBelowMinAndAcceptsCorrection() throws Exception {
        // every rule but max passes; the max=9 against min=25 violates
        // the (*this* > min) comparison
        HttpResponse<String> invalid = post(
            "/validator/validateValidWhenExamples.do",
            "firstName=Fred&lastName=World&fieldA=X&fieldB=Y"
            + "&min=25&max=9&address1=1+Main&city=Springfield&zip=12345");
        assertEquals(200, invalid.statusCode(), invalid.body());
        assertTrue(invalid.body()
            .contains("Maximum must be greater than the Minimum"),
            invalid.body());
        assertTrue(!invalid.body().contains("Last Name is required."),
            invalid.body());

        HttpResponse<String> valid = post(
            "/validator/validateValidWhenExamples.do",
            "firstName=Fred&lastName=World&fieldA=X&fieldB=Y"
            + "&min=25&max=30&address1=1+Main&city=Springfield&zip=12345");
        assertEquals(200, valid.statusCode(), valid.body());
        assertTrue(valid.body().contains("Struts Validator"), valid.body());
        assertTrue(!valid.body()
            .contains("Maximum must be greater than the Minimum"),
            valid.body());
    }

    @Test
    void jsRegistrationRendersClientValidationScript() throws Exception {
        HttpResponse<String> resp = get("/validator/jsRegistration.do");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("function validateRegistrationForm("),
            resp.body());
        assertTrue(resp.body().contains("required"), resp.body());
    }

    @Test
    void bundleExamplesResolveAlternateBundles() throws Exception {
        HttpResponse<String> resp = post(
            "/validator/validateBundleExamples.do",
            "name=&address=&phone=&dob=&age=");
        assertEquals(200, resp.statusCode(), resp.body());
        // literal literal message (resource=false)
        assertTrue(resp.body()
            .contains("You have forgotten to enter the name."), resp.body());
        // msg resolved from the "other" bundle, arg from the "different"
        // bundle and the default bundle
        assertTrue(resp.body()
            .contains("[Other Bundle]Missing Field: Address(Other Bundle)"),
            resp.body());
        assertTrue(resp.body()
            .contains("[Other Bundle]Missing Field: Phone Number(Different Bundle)"),
            resp.body());
        assertTrue(resp.body()
            .contains("[Default Bundle]Missing Field: Date of Birth(Different Bundle)"),
            resp.body());
    }
}
