package org.apache.struts.spring;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.struts.spring.testsupport.TomcatTestServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stack spike (roadmap phase 1, task 1.2): proves JSP compilation and jakarta.el
 * work on embedded Tomcat 11 with the pinned Spring Framework 7 stack.
 */
class TomcatJspSpikeTest {

    static TomcatTestServer server;

    @BeforeAll
    static void startTomcat(@TempDir Path docBase) throws Exception {
        Files.writeString(docBase.resolve("index.jsp"), """
                <%@ page contentType="text/html;charset=UTF-8" %>
                el-ok: ${1 + 1}
                scriptlet-ok: <%= "s" + 1 %>
                """);
        server = new TomcatTestServer(docBase, "/");
        server.start();
    }

    @AfterAll
    static void stopTomcat() {
        server.close();
    }

    @Test
    void rendersJspWithElAndScriptlets() throws Exception {
        HttpResponse<String> resp = TomcatTestServer.get(server.baseUrl() + "/index.jsp");
        assertEquals(200, resp.statusCode(), "JSP must render: " + resp.body());
        assertTrue(resp.body().contains("el-ok: 2"), "jakarta.el must evaluate: " + resp.body());
        assertTrue(resp.body().contains("scriptlet-ok: s1"), "scriptlets must render: " + resp.body());
    }
}
