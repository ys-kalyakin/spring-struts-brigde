package org.apache.struts.action;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockServletConfig;

import org.apache.struts.Globals;
import org.apache.struts.config.ModuleConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the ActionServlet facade configuration contract (tasks 2.3
 * and 6.1): the config init-param list is merged, startup fails with the
 * file name for missing or malformed resources, message resources and
 * plugins are initialized.
 */
class ActionServletConfigTest {

    private static final String CONFIG_DIR = "/org/apache/struts/config/";

    private MockServletContext servletContextWithConfigs(String... names) {
        Map<String, URL> resources = new HashMap<>();
        for (String name : names) {
            URL url = getClass().getResource(CONFIG_DIR + name);
            assertNotNull(url, "Fixture config not found: " + name);
            resources.put("/WEB-INF/" + name, url);
        }
        MockServletContext sc = new MockServletContext() {
            @Override
            public URL getResource(String path)
                throws java.net.MalformedURLException {
                URL url = resources.get(path);
                return (url != null) ? url : super.getResource(path);
            }
        };
        return sc;
    }

    private ActionServlet initServlet(String configParam,
        MockServletContext sc) throws Exception {
        ActionServlet servlet = new ActionServlet();
        MockServletConfig servletConfig = new MockServletConfig(sc, "action");
        servletConfig.addInitParameter("config", configParam);
        servlet.init(servletConfig);
        return servlet;
    }

    @Test
    void initializesFacadeWithConfigInitParam() throws Exception {
        MockServletContext sc = servletContextWithConfigs(
            "struts-config-a.xml");
        ActionServlet servlet = initServlet("/WEB-INF/struts-config-a.xml",
            sc);

        assertEquals("/WEB-INF/struts-config-a.xml",
            servlet.getInitParameter("config"));
        ModuleConfig mconfig = servlet.getModuleConfig();
        assertNotNull(mconfig);
        assertNotNull(mconfig.findActionConfig("/Welcome"));
        assertSame(servlet,
            sc.getAttribute(Globals.ACTION_SERVLET_KEY));
        assertSame(mconfig, sc.getAttribute(Globals.MODULE_KEY));
        // Message resources stored in the servlet context under their key
        assertNotNull(sc.getAttribute(Globals.MESSAGES_KEY));
        assertNotNull(sc.getAttribute("alternate"));
    }

    @Test
    void mergesConfigFileListWithLaterOverride() throws Exception {
        MockServletContext sc = servletContextWithConfigs(
            "struts-config-a.xml", "struts-config-b.xml");
        ActionServlet servlet = initServlet(
            "/WEB-INF/struts-config-a.xml,/WEB-INF/struts-config-b.xml", sc);

        ModuleConfig mconfig = servlet.getModuleConfig();
        // Elements from both files are registered
        assertNotNull(mconfig.findActionConfig("/Input"));
        assertNotNull(mconfig.findActionConfig("/Extra"));
        // Later file overrides the earlier mapping of the same path
        assertEquals("/pages/Overridden.jsp",
            mconfig.findActionConfig("/Welcome").getForward());
    }

    @Test
    void missingConfigFileFailsStartupWithFileName() {
        MockServletContext sc = servletContextWithConfigs();
        Exception e = assertThrows(Exception.class,
            () -> initServlet("/WEB-INF/no-such-config.xml", sc));
        assertTrue(e.getMessage().contains("/WEB-INF/no-such-config.xml"),
            "Error must name the missing file: " + e.getMessage());
    }

    @Test
    void malformedConfigFileFailsStartupWithFileName() {
        MockServletContext sc = servletContextWithConfigs(
            "struts-config-bad.xml");
        Exception e = assertThrows(Exception.class,
            () -> initServlet("/WEB-INF/struts-config-bad.xml", sc));
        assertTrue(e.getMessage().contains("struts-config-bad.xml"),
            "Error must name the broken file: " + e.getMessage());
    }

    @Test
    void unsupportedProcessorClassFailsStartupWithProperty() {
        MockServletContext sc = servletContextWithConfigs(
            "struts-config-processor.xml");
        Exception e = assertThrows(Exception.class,
            () -> initServlet("/WEB-INF/struts-config-processor.xml", sc));
        String message = e.getMessage();
        assertTrue(message.contains("processorClass"),
            "Error must name the property: " + message);
        assertTrue(message.contains("com.example.SecureRequestProcessor"),
            "Error must name the offending value: " + message);
        assertTrue(message.contains("The default module"),
            "Error must name the module: " + message);
    }

    @Test
    void pluginInitializedWithPropertiesAndDestroy() throws Exception {
        MockServletContext sc = servletContextWithConfigs(
            "struts-config-a.xml");
        ActionServlet servlet = initServlet("/WEB-INF/struts-config-a.xml",
            sc);

        assertEquals("hello", sc.getAttribute("fixturePlugin.message"));
        PlugIn plugin = servlet.plugIns.get(0);
        assertEquals("org.apache.struts.fixtures.FixturePlugin",
            plugin.getClass().getName());

        servlet.destroy();
        assertTrue(((org.apache.struts.fixtures.FixturePlugin) plugin)
            .isDestroyed());
    }
}
