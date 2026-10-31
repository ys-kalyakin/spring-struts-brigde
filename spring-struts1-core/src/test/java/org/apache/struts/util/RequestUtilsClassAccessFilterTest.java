package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Regression matrix of the CVE-2014-0114 class-access filter (the first
 * defense layer in <code>RequestUtils</code>): the typical class traversal
 * forms are blocked, while names that do not navigate through the
 * <code>class</code> property — including mapped keys — remain allowed.
 * The populate-level guard behind this filter is covered by
 * <code>PopulateClassAccessGuardTest</code>.
 */
class RequestUtilsClassAccessFilterTest {

    private boolean blocked(String name) {
        return RequestUtils.CLASS_ACCESS_PATTERN.matcher(name).matches();
    }

    @Test
    void classTraversalFormsAreBlocked() {
        assertTrue(blocked("class.classLoader"));
        assertTrue(blocked("class.module.classLoader.DefaultAssertionStatus"));
        assertTrue(blocked("class.protectionDomain"));
        assertTrue(blocked("foo.class.bar"));
        assertTrue(blocked("CLASS.classLoader"));
        assertTrue(blocked("Class.classLoader"));
        assertTrue(blocked("x.class.y"));
        assertTrue(blocked("class[0]"));
    }

    @Test
    void namesWithoutPropertyAccessToClassAreAllowed() {
        assertFalse(blocked("class"));
        assertFalse(blocked("classname"));
        assertFalse(blocked("foo[class]"));
        assertFalse(blocked("foo('class')"));
        assertFalse(blocked("map(class)"));
        assertFalse(blocked("x.class"));
    }
}
