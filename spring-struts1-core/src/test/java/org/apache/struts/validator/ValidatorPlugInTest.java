package org.apache.struts.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.validator.ValidatorResources;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.impl.ModuleConfigImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;

/**
 * Unit tests for ValidatorPlugIn: resource loading from pathnames, the
 * module-prefixed context keys, stopOnFirstError and the single-instance
 * guarantee per module.
 */
class ValidatorPlugInTest {

    private static final String RULES =
        "/org/apache/struts/validator/validator-rules.xml";

    private MockServletContext servletContext;

    private ActionServlet servlet;

    private ActionServlet servletWith(MockServletContext sc)
        throws Exception {
        this.servletContext = sc;
        ActionServlet servlet = new ActionServlet();
        MockServletConfig config = new MockServletConfig(sc, "action");
        config.addInitParameter("config", "/WEB-INF/struts-config.xml");
        servlet.init(config);
        return servlet;
    }

    static MockServletContext contextServingConfigFixture() {
        return new MockServletContext() {
            @Override
            public java.net.URL getResource(String path)
                throws java.net.MalformedURLException {
                return "/WEB-INF/struts-config.xml".equals(path)
                    ? ValidatorPlugInTest.class.getResource(
                        "/org/apache/struts/config/struts-config-a.xml")
                    : super.getResource(path);
            }
        };
    }

    private ValidatorPlugIn initPlugin(String prefix, String pathnames,
        boolean stopOnFirstError) throws Exception {
        ValidatorPlugIn plugin = new ValidatorPlugIn();
        plugin.setPathnames(pathnames);
        plugin.setStopOnFirstError(stopOnFirstError);
        plugin.init(servlet, new ModuleConfigImpl(prefix));
        return plugin;
    }

    @Test
    void loadsResourcesUnderModuleKey() throws Exception {
        servlet = servletWith(contextServingConfigFixture());
        initPlugin("", RULES, false);

        Object resources = servletContext.getAttribute(
            ValidatorPlugIn.VALIDATOR_KEY);
        assertNotNull(resources, "ValidatorResources must be stored");
        assertTrue(resources instanceof ValidatorResources);
        assertEquals(Boolean.FALSE, servletContext.getAttribute(
            ValidatorPlugIn.STOP_ON_ERROR_KEY + "."));
    }

    @Test
    void secondPluginForSameModuleFails() throws Exception {
        servlet = servletWith(contextServingConfigFixture());
        initPlugin("", RULES, true);

        assertThrows(UnavailableException.class,
            () -> initPlugin("", RULES, true));

        // another module may have its own plugin
        initPlugin("/validator", RULES, true);
        assertNotNull(servletContext.getAttribute(
            ValidatorPlugIn.VALIDATOR_KEY + "/validator"));
    }

    @Test
    void missingResourceFailsStartupWithResourceName() {
        MockServletContext sc = contextServingConfigFixture();
        try {
            servlet = servletWith(sc);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        ServletException e = assertThrows(UnavailableException.class,
            () -> initPlugin("", "/no/such/validation.xml", true));
        assertTrue(e.getMessage().contains("/no/such/validation.xml"));
    }

    @Test
    void stopOnFirstErrorDefaultsToTrue() throws Exception {
        servlet = servletWith(contextServingConfigFixture());
        ValidatorPlugIn plugin = new ValidatorPlugIn();
        plugin.setPathnames(RULES);
        plugin.init(servlet, new ModuleConfigImpl(""));
        assertEquals(Boolean.TRUE, servletContext.getAttribute(
            ValidatorPlugIn.STOP_ON_ERROR_KEY + "."));
    }
}
