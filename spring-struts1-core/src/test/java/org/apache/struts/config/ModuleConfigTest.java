package org.apache.struts.config;

import org.junit.jupiter.api.Test;

import org.apache.struts.fixtures.FixtureForm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the ported configuration model (task 2.1).
 */
class ModuleConfigTest {

    @Test
    void factoryCreatesDefaultModule() {
        ModuleConfigFactory factory = ModuleConfigFactory.createFactory();
        ModuleConfig config = factory.createModuleConfig("");
        assertEquals("", config.getPrefix());
        assertEquals("org.apache.struts.action.ActionMapping",
            config.getActionMappingClass());
        assertEquals("org.apache.struts.action.ActionFormBean",
            config.getActionFormBeanClass());
        assertEquals("org.apache.struts.action.ActionForward",
            config.getActionForwardClass());
    }

    @Test
    void actionConfigsRegisteredByPath() {
        ModuleConfig config = ModuleConfigFactory.createFactory()
            .createModuleConfig("");
        ActionConfig action = new ActionConfig();
        action.setPath("/Welcome");
        config.addActionConfig(action);

        assertSame(action, config.findActionConfig("/Welcome"));
        assertNull(config.findActionConfig("/Nope"));
        assertEquals(1, config.findActionConfigs().length);
        assertSame(config, action.getModuleConfig());
    }

    @Test
    void formBeanCreatedAndRegistered() {
        ModuleConfig config = ModuleConfigFactory.createFactory()
            .createModuleConfig("");
        FormBeanConfig form = new FormBeanConfig();
        form.setName("testForm");
        form.setType("org.apache.struts.fixtures.FixtureForm");
        config.addFormBeanConfig(form);

        assertSame(form, config.findFormBeanConfig("testForm"));
        Object created = config.findFormBeanConfig("testForm")
            .createActionForm();
        assertNotNull(created);
        assertEquals(FixtureForm.class, created.getClass());
    }

    @Test
    void globalForwardAndExceptionRegistered() {
        ModuleConfig config = ModuleConfigFactory.createFactory()
            .createModuleConfig("");
        ForwardConfig forward = new ForwardConfig("welcome", "/Welcome.do",
            false);
        config.addForwardConfig(forward);
        ExceptionConfig exception = new ExceptionConfig();
        exception.setType("java.lang.IllegalStateException");
        exception.setPath("/global-error.jsp");
        config.addExceptionConfig(exception);

        assertSame(forward, config.findForwardConfig("welcome"));
        assertSame(exception,
            config.findException(IllegalStateException.class));
        // Subclass of the registered type resolves through the hierarchy walk.
        assertSame(exception,
            config.findException(SubIllegalState.class));
        assertNull(config.findException(java.lang.Exception.class));
    }

    /** A subclass of the registered exception type (hierarchy walk check). */
    static class SubIllegalState extends IllegalStateException {
    }

    @Test
    void messageResourcesDefaultAndNamedKeys() {
        ModuleConfig config = ModuleConfigFactory.createFactory()
            .createModuleConfig("");
        MessageResourcesConfig defaultBundle = new MessageResourcesConfig();
        defaultBundle.setParameter("test-messages");
        config.addMessageResourcesConfig(defaultBundle);
        MessageResourcesConfig namedBundle = new MessageResourcesConfig();
        namedBundle.setKey("alternate");
        namedBundle.setParameter("alternate-messages");
        config.addMessageResourcesConfig(namedBundle);

        assertEquals(org.apache.struts.Globals.MESSAGES_KEY,
            defaultBundle.getKey());
        assertSame(defaultBundle,
            config.findMessageResourcesConfig(
                org.apache.struts.Globals.MESSAGES_KEY));
        assertSame(namedBundle,
            config.findMessageResourcesConfig("alternate"));
    }

    @Test
    void freezePreventsMutation() {
        ModuleConfig config = ModuleConfigFactory.createFactory()
            .createModuleConfig("");
        config.freeze();
        assertThrows(IllegalStateException.class,
            () -> config.addActionConfig(new ActionConfig()));
        ActionConfig frozen = new ActionConfig();
        frozen.setPath("/X");
        frozen.freeze();
        assertThrows(IllegalStateException.class, () -> frozen.setPath("/Y"));
    }
}
