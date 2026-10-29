package org.apache.struts.apps.mailreader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.apache.struts.spring.testsupport.TomcatTestServer;

/**
 * Integration tests of the mailreader application on embedded Tomcat 11:
 * logon validation and credentials, the ExpiredPasswordException forward,
 * registration of a new user (config extends + transactional tokens) and
 * the subscription create/edit/delete cycle (spec mailreader).
 */
class MailreaderAppTest {

    static TomcatTestServer server;
    static HttpClient client;

    /**
     * The memory database plugin persists to WEB-INF/database.xml under the
     * docBase: run the application from a disposable copy so the source
     * tree (and the seed users between runs) stays untouched.
     */
    @TempDir
    static Path tempDir;

    @BeforeAll
    static void startTomcat() throws Exception {
        Path source = Path.of("src/main/webapp");
        Path docBase = Files.createDirectory(tempDir.resolve("mailreader"));
        try (Stream<Path> walk = Files.walk(source)) {
            for (Path file : walk.toList()) {
                Path target = docBase.resolve(source.relativize(file));
                if (Files.isDirectory(file)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(file, target);
                }
            }
        }
        server = new TomcatTestServer(docBase, "/mailreader");
        server.start();
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
            URI.create(server.baseUrl() + path))
            .header("Accept-Language", "en")
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(String path, String form)
        throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept-Language", "en")
            .POST(HttpRequest.BodyPublishers.ofString(form,
                StandardCharsets.UTF_8))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static final Pattern TOKEN_PARAM = Pattern.compile(
        "name=\"org\\.apache\\.struts\\.taglib\\.html\\.TOKEN\"\\s*value=\"([^\"]+)\"");

    private static String token(HttpResponse<String> prepared) {
        Matcher matcher = TOKEN_PARAM.matcher(prepared.body());
        assertTrue(matcher.find(), "the form must carry the transaction token");
        return matcher.group(1);
    }

    private static void logon(String username, String password)
        throws Exception {
        HttpResponse<String> resp = post("/SubmitLogon.do",
            "username=" + username + "&password=" + password);
        assertEquals(200, resp.statusCode(), resp.body());
    }

    @Test
    void logonValidationReportsAllErrors() throws Exception {
        // username missing, password too short: the server reports ALL errors
        HttpResponse<String> resp = post("/SubmitLogon.do",
            "username=&password=ab");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("Username is required."), resp.body());
        assertTrue(resp.body()
            .contains("Password cannot be less than 3 characters."),
            resp.body());
    }

    @Test
    void logonRejectsBadCredentials() throws Exception {
        HttpResponse<String> resp = post("/SubmitLogon.do",
            "username=user&password=wrong");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body()
            .contains("Invalid username and/or password, please try again"),
            resp.body());
    }

    @Test
    void logonSucceedsForSeedUser() throws Exception {
        logon("user", "pass");
        HttpResponse<String> menu = get("/MainMenu.do");
        assertEquals(200, menu.statusCode(), menu.body());
        assertTrue(menu.body().contains("Main Menu Options for"), menu.body());
        assertTrue(menu.body().contains("Log off"), menu.body());

        HttpResponse<String> logoff = get("/Logoff.do");
        assertEquals(200, logoff.statusCode(), logoff.body());
        HttpResponse<String> back = get("/MainMenu.do");
        assertEquals(200, back.statusCode(), back.body());
        assertTrue(back.body().contains(
            "MailReader Demonstration Application - Logon"), back.body());
    }

    @Test
    void expiredPasswordForwardsToChangePassword() throws Exception {
        HttpResponse<String> resp = post("/SubmitLogon.do",
            "username=Hermes&password=whatever");
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains(
            "Your password has expired."), resp.body());
    }

    @Test
    void registrationCreatesUserAndLogsOn() throws Exception {
        HttpResponse<String> prepared = get("/EditRegistration.do");
        assertEquals(200, prepared.statusCode(), prepared.body());
        assertTrue(prepared.body().contains(
            "Register for the MailReader Demonstration Application"),
            prepared.body());
        String token = token(prepared);

        HttpResponse<String> resp = post("/SaveRegistration.do",
            "task=Create&username=newuser&password=secret&password2=secret"
            + "&fullName=New+User&fromAddress=new%40example.com"
            + "&org.apache.struts.taglib.html.TOKEN=" + token);
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("Main Menu Options for"), resp.body());
        assertTrue(resp.body().contains("New User"), resp.body());
    }

    @Test
    void registrationRejectsDuplicateUsername() throws Exception {
        HttpResponse<String> prepared = get("/EditRegistration.do");
        assertEquals(200, prepared.statusCode(), prepared.body());
        String token = token(prepared);

        HttpResponse<String> resp = post("/SaveRegistration.do",
            "task=Create&username=user&password=secret&password2=secret"
            + "&fullName=Dup+User&fromAddress=dup%40example.com"
            + "&org.apache.struts.taglib.html.TOKEN=" + token);
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body()
            .contains("That username is already in use"), resp.body());
    }

    @Test
    void registrationTokenBlocksOutOfSequenceSave() throws Exception {
        HttpResponse<String> prepared = get("/EditRegistration.do");
        assertEquals(200, prepared.statusCode(), prepared.body());
        String token = token(prepared);

        String form = "task=Create&username=tokenuser&password=secret"
            + "&password2=secret&fullName=Token+User"
            + "&fromAddress=token%40example.com"
            + "&org.apache.struts.taglib.html.TOKEN=" + token;
        HttpResponse<String> first = post("/SaveRegistration.do", form);
        assertEquals(200, first.statusCode(), first.body());
        assertTrue(first.body().contains("Main Menu Options for"),
            first.body());

        HttpResponse<String> duplicate = post("/SaveRegistration.do",
            form.replace("tokenuser", "tokenuser2")
                .replace("token%40example.com", "token2%40example.com"));
        assertEquals(200, duplicate.statusCode(), duplicate.body());
        assertTrue(duplicate.body()
            .contains("Cannot submit this form out of order"), duplicate.body());
    }

    @Test
    void subscriptionEditUpdateDeleteFlow() throws Exception {
        logon("user", "pass");

        // Edit: the wildcard /Edit* action extends //BaseAction and dispatches
        // to SubscriptionAction.Edit, which populates the existing subscription
        HttpResponse<String> edit = get(
            "/EditSubscription.do?host=mail.hotmail.com");
        assertEquals(200, edit.statusCode(), edit.body());
        assertTrue(edit.body().contains(
            "Edit Registration for the MailReader Demonstration Application")
            || edit.body().contains("mail.hotmail.com"), edit.body());

        // Save with empty credentials: the SubscriptionForm validation
        // reports the missing fields on the input page
        HttpResponse<String> invalid = post("/SaveSubscription.do",
            "task=Edit&host=mail.hotmail.com&username=&password=");
        assertEquals(200, invalid.statusCode(), invalid.body());
        assertTrue(invalid.body().contains("Mail Username is required."),
            invalid.body());
        assertTrue(invalid.body().contains("Mail Password is required."),
            invalid.body());

        // Save the updated mail username
        HttpResponse<String> saved = post("/SaveSubscription.do",
            "task=Edit&host=mail.hotmail.com&username=changed123&password=bar"
            + "&type=pop3&autoConnect=false");
        assertEquals(200, saved.statusCode(), saved.body());
        assertTrue(saved.body().contains("mail.hotmail.com"), saved.body());

        // Delete the second seed subscription: /Delete* dispatches Delete,
        // the confirmation form posts task=Delete to /SaveSubscription
        HttpResponse<String> confirm = get(
            "/DeleteSubscription.do?host=mail.yahoo.com");
        assertEquals(200, confirm.statusCode(), confirm.body());
        assertTrue(confirm.body().contains("mail.yahoo.com"), confirm.body());

        HttpResponse<String> deleted = post("/SaveSubscription.do",
            "task=Delete&host=mail.yahoo.com&username=jquser&password=foo"
            + "&type=imap&autoConnect=false");
        assertEquals(200, deleted.statusCode(), deleted.body());
        assertTrue(deleted.body().contains("mail.hotmail.com"), deleted.body());
        assertFalse(deleted.body().contains("mail.yahoo.com"), deleted.body());
    }
}
