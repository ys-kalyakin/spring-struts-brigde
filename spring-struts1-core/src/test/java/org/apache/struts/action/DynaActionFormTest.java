package org.apache.struts.action;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.FormPropertyConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.config.impl.ModuleConfigImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests of {@link DynaActionForm}: dynamic creation from
 * {@link org.apache.struts.config.FormBeanConfig} with initial values and
 * the declarative <code>reset</code> behavior.
 */
class DynaActionFormTest {

    private ModuleConfig module() {
        ModuleConfigImpl module = new ModuleConfigImpl("");

        FormBeanConfig bean = new FormBeanConfig();
        bean.setName("dynaBean");
        bean.setType("org.apache.struts.action.DynaActionForm");
        bean.addFormPropertyConfig(new FormPropertyConfig(
            "name", "java.lang.String", null));
        bean.addFormPropertyConfig(new FormPropertyConfig(
            "age", "java.lang.Integer", "33"));
        bean.addFormPropertyConfig(new FormPropertyConfig(
            "extra", "java.util.HashMap", null));
        FormPropertyConfig status = new FormPropertyConfig(
            "status", "java.lang.String", "new");
        status.setReset("POST");
        bean.addFormPropertyConfig(status);
        module.addFormBeanConfig(bean);

        return module;
    }

    private ActionMapping mapping(ModuleConfig module) {
        ActionMapping mapping = new ActionMapping();
        mapping.setModuleConfig(module);
        mapping.setName("dynaBean");
        return mapping;
    }

    @Test
    void dynaFormGetsInitialValuesFromConfig() throws Exception {
        FormBeanConfig config = module().findFormBeanConfig("dynaBean");
        DynaActionForm form = (DynaActionForm) config.createActionForm();

        assertEquals("", form.get("name"),
            "a String without initial defaults to the empty string");
        assertEquals(33, form.get("age"));
        assertEquals("new", form.get("status"));
    }

    @Test
    void dynaFormClassResolvesPropertyTypes() throws Exception {
        FormBeanConfig config = module().findFormBeanConfig("dynaBean");
        DynaActionForm form = (DynaActionForm) config.createActionForm();

        assertEquals(Integer.class,
            form.getDynaClass().getDynaProperty("age").getType());
        assertEquals(String.class,
            form.getDynaClass().getDynaProperty("name").getType());
    }

    @Test
    void resetAppliesInitialsOnlyForConfiguredMethods() throws Exception {
        ModuleConfig module = module();
        ActionMapping mapping = mapping(module);
        DynaActionForm form = (DynaActionForm)
            module.findFormBeanConfig("dynaBean").createActionForm();

        form.set("status", "changed");
        form.set("age", 99);

        MockHttpServletRequest get = new MockHttpServletRequest();
        get.setMethod("GET");
        form.reset(mapping, get);
        assertEquals("changed", form.get("status"),
            "GET is not in the reset list: value must survive");
        assertEquals(99, form.get("age"));

        MockHttpServletRequest post = new MockHttpServletRequest();
        post.setMethod("POST");
        form.reset(mapping, post);
        assertEquals("new", form.get("status"),
            "POST is in the reset list: value must fall back to initial");
        assertEquals(99, form.get("age"),
            "age has no reset config: value must survive");
    }

    @Test
    void setGetAndContainsWorkOverTheDynaBean() throws Exception {
        DynaActionForm form = (DynaActionForm)
            module().findFormBeanConfig("dynaBean").createActionForm();

        form.set("name", "Ivan");
        assertEquals("Ivan", form.get("name"));

        form.set("extra", new java.util.HashMap<String, Object>(
            java.util.Map.of("k", "v")));
        assertTrue(form.contains("extra", "k"));
        assertTrue(!form.contains("extra", "absent"));
    }
}
