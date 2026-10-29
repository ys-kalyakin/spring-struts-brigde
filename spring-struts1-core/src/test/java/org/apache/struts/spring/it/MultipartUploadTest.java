package org.apache.struts.spring.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.struts.spring.testsupport.TomcatTestServer;

/**
 * Integration tests of the multipart pipeline on embedded Tomcat 11: file
 * upload with text fields, multi-file arrays, the graceful limit-exceeded
 * handling and temp file cleanup (spec multipart-upload).
 */
class MultipartUploadTest {

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

    private static final String BOUNDARY = "it-boundary";

    private static byte[] body(String textField, String textValue,
        String fileField, String fileName, byte[] fileData) {
        StringBuilder sb = new StringBuilder();
        if (textField != null) {
            sb.append("--").append(BOUNDARY).append("\r\n")
                .append("Content-Disposition: form-data; name=\"")
                .append(textField).append("\"\r\n\r\n")
                .append(textValue).append("\r\n");
        }
        if (fileField != null) {
            sb.append("--").append(BOUNDARY).append("\r\n")
                .append("Content-Disposition: form-data; name=\"")
                .append(fileField).append("\"; filename=\"")
                .append(fileName).append("\"\r\n")
                .append("Content-Type: text/plain\r\n\r\n")
                .append(new String(fileData, StandardCharsets.UTF_8))
                .append("\r\n");
        }
        sb.append("--").append(BOUNDARY).append("--\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    static HttpResponse<String> postMultipart(String path, byte[] body)
        throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(
            URI.create(server.baseUrl() + path))
            .header("Content-Type",
                "multipart/form-data; boundary=" + BOUNDARY)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void uploadsWithTextAndSingleFile() throws Exception {
        HttpResponse<String> resp = postMultipart("/Upload.do",
            body("theText", "hello", "theFile", "report.txt",
                "file-payload".getBytes(StandardCharsets.UTF_8)));
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("upload:text=hello"), resp.body());
        assertTrue(resp.body().contains(":file=report.txt"), resp.body());
        assertTrue(resp.body().contains("text/plain"), resp.body());
        assertTrue(resp.body().contains(":data=file-payload"), resp.body());
        assertTrue(resp.body().contains(":files=0"), resp.body());
    }

    @Test
    void multiFileFieldFillsArrayProperty() throws Exception {
        HttpResponse<String> resp = postMultipart("/Upload.do",
            body("theText", "multi", "files", "a.txt",
                "aaa".getBytes(StandardCharsets.UTF_8)));
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains(":files=1"), resp.body());
    }

    @Test
    void exceededFileSizeForwardsToInputWithValidationError() throws Exception {
        // maxFileSize is 2K; the payload is clearly larger
        byte[] big = new byte[8192];
        java.util.Arrays.fill(big, (byte) 'x');
        HttpResponse<String> resp = postMultipart("/Upload.do",
            body("theText", "boom", "theFile", "big.bin", big));
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("upload-input-errors:1"), resp.body());
    }

    private static long countUploadTempFiles(java.io.File tempDir)
        throws IOException {
        try (Stream<Path> walk = Files.walk(tempDir.toPath())) {
            return walk.filter(p -> p.getFileName().toString()
                .startsWith("upload_")).count();
        }
    }

    @Test
    void tempFilesAreCleanedAfterRequest() throws Exception {
        // memFileSize=1K forces a disk-backed item; finish()/rollback() must
        // remove the handler's temp file once the response is fully produced
        java.io.File tempDir = (java.io.File) server.servletContext()
            .getAttribute("jakarta.servlet.context.tempdir");
        long before = countUploadTempFiles(tempDir);

        // above the 1K disk threshold, below the 2K maxFileSize
        byte[] payload = new byte[1536];
        java.util.Arrays.fill(payload, (byte) 'd');
        HttpResponse<String> resp = postMultipart("/Upload.do",
            body("theText", "cleanup", "theFile", "temp.bin", payload));
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("upload:text=cleanup"), resp.body());

        assertEquals(before, countUploadTempFiles(tempDir),
            "temp upload files must be deleted after the request");
    }
}
