package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.fixtures.NamesForm;

/**
 * Regression tests of the reserved-property-name filter in
 * {@link RequestUtils#populate} (CVE-2006-1547, CVE-2016-1182): the exact
 * reserved set is never assignable from request parameters, the match is
 * case-sensitive, and <code>page</code> is deliberately not reserved.
 * Populate-level fixtures live in <code>PopulateReservedNamesFixtureTest</code>.
 */
class RequestUtilsReservedNamesTest {

    @Test
    void reservedSetIsExactlyTheFiveInternalNames() {
        assertEquals(5, RequestUtils.RESERVED_PROPERTY_NAMES.size());
        assertTrue(RequestUtils.RESERVED_PROPERTY_NAMES.contains("servlet"));
        assertTrue(RequestUtils.RESERVED_PROPERTY_NAMES
            .contains("multipartRequestHandler"));
        assertTrue(RequestUtils.RESERVED_PROPERTY_NAMES
            .contains("resultValueMap"));
        assertTrue(RequestUtils.RESERVED_PROPERTY_NAMES
            .contains("validatorResults"));
        assertTrue(RequestUtils.RESERVED_PROPERTY_NAMES
            .contains("dynaClass"));
    }

    @Test
    void matchIsExactAndCaseSensitive() {
        assertFalse(RequestUtils.RESERVED_PROPERTY_NAMES.contains("Servlet"));
        assertFalse(RequestUtils.RESERVED_PROPERTY_NAMES
            .contains("MultipartRequestHandler"));
        assertFalse(RequestUtils.RESERVED_PROPERTY_NAMES
            .contains("validatorresults"));
        assertFalse(RequestUtils.RESERVED_PROPERTY_NAMES
            .contains("servletName"));
    }

    @Test
    void pageIsDeliberatelyNotReserved() {
        assertFalse(RequestUtils.RESERVED_PROPERTY_NAMES.contains("page"));
    }

    @Test
    void reservedParametersAreSkippedWhileOthersPopulate() {
        for (String reserved : RequestUtils.RESERVED_PROPERTY_NAMES) {
            NamesForm form = new NamesForm();
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setMethod("POST");
            request.setParameter(reserved, "x");
            request.setParameter("name", "Alice");

            assertDoesNotThrow(() -> RequestUtils.populate(form, request),
                "parameter '" + reserved + "' must be skipped");
            assertEquals("Alice", form.getName(),
                "the remaining parameters still populate");
        }
    }
}
