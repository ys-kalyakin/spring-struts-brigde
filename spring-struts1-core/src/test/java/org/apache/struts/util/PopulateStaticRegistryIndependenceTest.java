package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.ConvertUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.fixtures.ScalarForm;

/**
 * Populate must not depend on the static commons-beanutils
 * <code>ConvertUtils</code> registry: a converter registered there by an
 * application has no effect on the engine's populate conversion.
 */
class PopulateStaticRegistryIndependenceTest {

    @AfterEach
    void restoreRegistryDefaults() {
        ConvertUtils.deregister();
    }

    @Test
    void staticRegistryConverterDoesNotAffectPopulate() throws Exception {
        ConvertUtils.register(new Converter() {
            @Override
            public <T> T convert(Class<T> type, Object value) {
                // a deliberately wrong conversion for any Integer target
                return type.cast(Integer.valueOf(12345));
            }
        }, Integer.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("intPrimitive", "7");
        request.setParameter("intWrapper", "8");

        ScalarForm form = new ScalarForm();
        RequestUtils.populate(form, request);

        assertEquals(7, form.getIntPrimitive(),
            "populate uses the engine's conversion service, not the static "
                + "registry");
        assertEquals(Integer.valueOf(8), form.getIntWrapper());
    }
}
