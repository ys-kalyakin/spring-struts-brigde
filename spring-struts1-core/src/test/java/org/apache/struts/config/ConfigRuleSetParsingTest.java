package org.apache.struts.config;

import java.net.URL;

import org.apache.commons.digester.Digester;
import org.junit.jupiter.api.Test;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Digester parsing of struts-config.xml (task 2.2). The
 * fixture configuration mirrors the structure of weblegacy apps/blank.
 */
class ConfigRuleSetParsingTest {

    private ModuleConfig parse(String resource) throws Exception {
        URL url = getClass().getResource(resource);
        ModuleConfig config = ModuleConfigFactory.createFactory()
            .createModuleConfig("");
        Digester digester = new Digester();
        digester.push(config);
        digester.setClassLoader(Thread.currentThread()
            .getContextClassLoader());
        digester.setValidating(false);
        digester.addRuleSet(new ConfigRuleSet());
        digester.parse(url);
        return config;
    }

    @Test
    void parsesBlankStyleConfiguration() throws Exception {
        ModuleConfig config = parse("/org/apache/struts/config/struts-config-a.xml");

        // Controller
        assertEquals("text/plain", config.getControllerConfig().getContentType());
        assertTrue(config.getControllerConfig().getNocache());

        // Form beans
        FormBeanConfig form = config.findFormBeanConfig("testForm");
        assertNotNull(form);
        assertEquals("org.apache.struts.fixtures.FixtureForm", form.getType());

        // Global forwards are ActionForward instances
        ForwardConfig globalForward = config.findForwardConfig("welcome");
        assertNotNull(globalForward);
        assertEquals(ActionForward.class, globalForward.getClass());
        assertEquals("/Welcome.do", globalForward.getPath());
        assertFalse(globalForward.getRedirect());

        // Global exceptions
        assertNotNull(config.findExceptionConfig(
            "java.lang.IllegalStateException"));

        // Action mappings are ActionMapping instances
        ActionConfig welcome = config.findActionConfig("/Welcome");
        assertNotNull(welcome);
        assertEquals(ActionMapping.class, welcome.getClass());
        assertEquals("/pages/Welcome.jsp", welcome.getForward());
        // Classic defaults: scope=session, validate=true
        assertEquals("session", welcome.getScope());
        assertTrue(welcome.getValidate());

        // Full action with form, local forward, local exception, set-property
        ActionConfig input = (ActionMapping) config.findActionConfig("/Input");
        assertEquals("org.apache.struts.fixtures.FixtureAction",
            input.getType());
        assertEquals("testForm", input.getName());
        assertEquals("request", input.getScope());
        assertEquals("/input.jsp", input.getInput());
        assertEquals("/done.jsp", input.findForwardConfig("done").getPath());
        assertEquals("/local-error.jsp", input
            .findExceptionConfig("java.lang.RuntimeException").getPath());
        assertEquals("extraValue", input.getProperty("extraProperty"));
        // Local exception takes priority over the global one at runtime lookup
        assertSame(input.findExceptionConfig("java.lang.RuntimeException"),
            input.findException(RuntimeException.class));

        // Message resources: default and named
        assertNotNull(config.findMessageResourcesConfig(
            org.apache.struts.Globals.MESSAGES_KEY));
        assertEquals("test-messages", config
            .findMessageResourcesConfig(
                org.apache.struts.Globals.MESSAGES_KEY).getParameter());
        assertEquals("alternate-messages", config
            .findMessageResourcesConfig("alternate").getParameter());

        // Plugins
        PlugInConfig[] plugins = config.findPlugInConfigs();
        assertEquals(1, plugins.length);
        assertEquals("org.apache.struts.fixtures.FixturePlugin",
            plugins[0].getClassName());
        assertEquals("hello", plugins[0].getProperties().get("message"));
    }
}
