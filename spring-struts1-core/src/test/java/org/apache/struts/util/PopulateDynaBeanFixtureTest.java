package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.LazyDynaClass;

import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.FormPropertyConfig;
import org.apache.struts.validator.LazyValidatorForm;

/**
 * Fixture tests of the DynaBean path of {@link RequestUtils#populate}:
 * declared {@code form-property} types (including zero-values on invalid
 * input), mapped properties receiving the raw parameter value, unknown
 * properties and lazy auto-addition. These pin the observable behavior of
 * the current implementation.
 */
class PopulateDynaBeanFixtureTest {

    private DynaActionForm dynaForm(FormPropertyConfig... properties) {
        FormBeanConfig bean = new FormBeanConfig();
        bean.setName("dynaBean");
        bean.setType("org.apache.struts.action.DynaActionForm");
        for (FormPropertyConfig property : properties) {
            bean.addFormPropertyConfig(property);
        }
        return (DynaActionForm) bean.createActionForm();
    }

    private MockHttpServletRequest request(String... params) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.setParameter(params[i], params[i + 1].split(",", -1));
        }
        return request;
    }

    @Test
    void dynaFormPopulatesDeclaredTypes() throws Exception {
        DynaActionForm form = dynaForm(
            new FormPropertyConfig("testString", "java.lang.String", null),
            new FormPropertyConfig("confirmed", "java.lang.Boolean", null),
            new FormPropertyConfig("count", "java.lang.Integer", null));

        RequestUtils.populate(form, request(
            "testString", "hello", "confirmed", "true", "count", "42"));

        assertEquals("hello", form.get("testString"));
        assertEquals(Boolean.TRUE, form.get("confirmed"));
        assertEquals(Integer.valueOf(42), form.get("count"));
    }

    @Test
    void dynaFormInvalidNumberGivesZeroValueWithoutError() throws Exception {
        DynaActionForm form = dynaForm(
            new FormPropertyConfig("count", "java.lang.Integer", "7"));

        RequestUtils.populate(form, request("count", "abc"));

        assertEquals(Integer.valueOf(0), form.get("count"),
            "invalid input assigns the zero value over the initial");
    }

    @Test
    void dynaFormMappedPropertyReceivesRawValue() throws Exception {
        DynaActionForm form = dynaForm(
            new FormPropertyConfig("map", "java.util.HashMap", null));

        RequestUtils.populate(form, request("map(color)", "red"));

        Object value = ((Map<?, ?>) form.get("map")).get("color");
        assertEquals(String[].class, value.getClass(),
            "beanutils puts the raw parameter value into the dyna map");
        assertEquals("red", ((String[]) value)[0]);
    }

    @Test
    void unknownDynaPropertyIsSkipped() {
        DynaActionForm form = dynaForm(
            new FormPropertyConfig("testString", "java.lang.String", null));

        assertDoesNotThrow(
            () -> RequestUtils.populate(form, request("nope", "1")));
        assertEquals("", form.get("testString"));
    }

    @Test
    void lazyFormAddsUndeclaredPropertyAsFirstElement() throws Exception {
        LazyValidatorForm form = new LazyValidatorForm();

        RequestUtils.populate(form, request("extra", "1,2"));

        assertEquals("1", form.get("extra"),
            "an undeclared lazy property receives the first parameter "
                + "element as a String");
    }

    @Test
    void lazyFormConvertsDeclaredProperty() throws Exception {
        LazyDynaClass dynaClass = new LazyDynaClass();
        dynaClass.add("count", Integer.class);
        LazyValidatorForm form = new LazyValidatorForm(
            new LazyDynaBean(dynaClass));

        RequestUtils.populate(form, request("count", "7"));

        assertEquals(Integer.valueOf(7), form.get("count"));
    }
}
