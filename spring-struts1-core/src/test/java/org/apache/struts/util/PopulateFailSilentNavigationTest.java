package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.fixtures.NamesForm;

/**
 * Regression tests of the fail-silent navigation required by the
 * form-populate spec: an unknown or unreadable intermediate property of a
 * nested parameter path is skipped without an error, and the remaining
 * parameters are still populated.
 */
class PopulateFailSilentNavigationTest {

    private NamesForm populated(String... params) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.setParameter(params[i], params[i + 1].split(",", -1));
        }
        NamesForm form = new NamesForm();
        RequestUtils.populate(form, request);
        return form;
    }

    @Test
    void unknownFirstSegmentIsSkippedWithoutError() throws Exception {
        NamesForm form = populated("nonExistent.deep.path", "1",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void unknownIntermediatePropertyIsSkippedWithoutError() throws Exception {
        NamesForm form = populated("nested.missing.child", "1",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void unknownMappedPropertyIsSkippedWithoutError() throws Exception {
        NamesForm form = populated("doesNotExist(color)", "1",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void populateContinuesAfterSkippedUnknownPath() throws Exception {
        NamesForm form = populated("name", "Alice",
            "nonExistent.deep.path", "1");
        assertEquals("Alice", form.getName(),
            "the remaining parameters are populated");
    }
}
