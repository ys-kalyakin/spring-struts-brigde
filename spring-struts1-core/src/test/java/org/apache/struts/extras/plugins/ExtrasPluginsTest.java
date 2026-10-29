package org.apache.struts.extras.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.impl.ModuleConfigImpl;

import jakarta.servlet.ServletException;

/**
 * Unit tests of the extras plugins: the ModuleConfigVerifier scans a
 * module configuration and rejects the invalid one; the DigestingPlugIn
 * parses its XML data file through digester rules and stores the result
 * under the configured key.
 */
class ExtrasPluginsTest {

    private ActionServlet servletWith(final MockServletContext sc) {
        return new ActionServlet() {
            @Override
            public jakarta.servlet.ServletContext getServletContext() {
                return sc;
            }

            @Override
            public String getInitParameter(String name) {
                return null;
            }

            {
                // no full servlet init in unit tests; the internal
                // resources must be live for the verifier messages
                try {
                    initInternal();
                } catch (jakarta.servlet.ServletException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        };
    }

    private MockServletContext servletContext() {
        return new MockServletContext() {
            @Override
            public java.net.URL getResource(String path)
                throws java.net.MalformedURLException {
                if ("/WEB-INF/test-data.xml".equals(path)) {
                    return ExtrasPluginsTest.class.getResource(
                        "/org/apache/struts/extras/plugins/test-data.xml");
                }
                return super.getResource(path);
            }
        };
    }

    @Test
    void moduleConfigVerifierAcceptsSoundModule() throws Exception {
        MockServletContext sc = servletContext();
        ActionServlet servlet = servletWith(sc);
        ModuleConfigImpl module = new ModuleConfigImpl("");
        module.addActionConfig(new org.apache.struts.action.ActionMapping());

        ModuleConfigVerifier verifier = new ModuleConfigVerifier();
        verifier.init(servlet, module);
        verifier.destroy();
    }

    @Test
    void moduleConfigVerifierRejectsBrokenModule() throws Exception {
        MockServletContext sc = servletContext();
        ActionServlet servlet = servletWith(sc);
        ModuleConfigImpl module = new ModuleConfigImpl("");
        // the verifier checks the action-mappings class of the module
        module.setActionMappingClass("no.such.ActionMapping");

        ModuleConfigVerifier verifier = new ModuleConfigVerifier();
        assertThrows(ServletException.class, () -> verifier.init(servlet, module));
    }

    @Test
    void digestingPluginParsesConfigIntoContext() throws Exception {
        MockServletContext sc = servletContext();
        ActionServlet servlet = servletWith(sc);

        // the classic initModulePlugIns populates the plugin properties
        // with BeanUtils; here the same values are set directly
        DigestingPlugIn plugin = new DigestingPlugIn();
        plugin.setKey("serverTypes");
        plugin.setDigesterPath(
            "org/apache/struts/extras/plugins/test-rules.xml");
        plugin.setDigesterSource("classpath");
        plugin.setConfigPath("org/apache/struts/extras/plugins/test-data.xml");
        plugin.setConfigSource("classpath");
        plugin.init(servlet, new ModuleConfigImpl(""));

        Object stored = sc.getAttribute("serverTypes");
        assertNotNull(stored, "digest result stored under the configured key");
        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) stored;
        assertEquals(2, types.size());
        assertTrue(types.contains("imap"));
    }

    @Test
    void digestingPluginFailsOnMissingConfigFile() throws Exception {
        MockServletContext sc = servletContext();
        ActionServlet servlet = servletWith(sc);

        DigestingPlugIn plugin = new DigestingPlugIn();
        plugin.setKey("missing");
        plugin.setDigesterPath(
            "org/apache/struts/extras/plugins/test-rules.xml");
        plugin.setDigesterSource("classpath");
        plugin.setConfigPath("/WEB-INF/no-such-file.xml");
        plugin.setConfigSource("servlet");
        assertThrows(ServletException.class, () ->
            plugin.init(servlet, new ModuleConfigImpl("")));
    }
}
