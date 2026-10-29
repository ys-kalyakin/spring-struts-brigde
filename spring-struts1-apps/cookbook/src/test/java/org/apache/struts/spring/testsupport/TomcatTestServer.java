package org.apache.struts.spring.testsupport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import jakarta.servlet.ServletException;

/**
 * Embedded Tomcat 11 test server used by all request-level integration tests.
 * Requests are always tested against a real container, never a mock stack.
 */
public class TomcatTestServer implements AutoCloseable {

    private final Tomcat tomcat;
    private final String contextPath;
    private final Path docBase;

    public TomcatTestServer(Path docBase, String contextPath) throws ServletException {
        this.docBase = docBase.toAbsolutePath();
        this.contextPath = contextPath;
        Path baseDir;
        try {
            baseDir = Files.createTempDirectory("spring-struts1-tomcat");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create Tomcat base dir", e);
        }
        tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(0);
        tomcat.getConnector();
        Context ctx = tomcat.addWebapp(contextPath, this.docBase.toString());
        // Let the webapp see the test classpath (engine + Spring + fixture classes).
        ctx.setParentClassLoader(Thread.currentThread().getContextClassLoader());
    }

    public void start() throws LifecycleException {
        tomcat.start();
    }

    @Override
    public void close() {
        try {
            tomcat.stop();
            tomcat.destroy();
        } catch (LifecycleException e) {
            throw new IllegalStateException("Failed to stop embedded Tomcat", e);
        }
    }

    public String baseUrl() {
        return "http://localhost:" + tomcat.getConnector().getLocalPort() + contextPath;
    }

    public Context context() {
        return (Context) tomcat.getHost().findChildren()[0];
    }

    /** The jakarta ServletContext of the deployed webapp. */
    public jakarta.servlet.ServletContext servletContext() {
        return context().getServletContext();
    }

    public Path docBase() {
        return docBase;
    }

    public static HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    static {
        // Keep Tomcat's JULI output visible: startup failures of the engine
        // would otherwise hide behind silent 404s.
    }
}
