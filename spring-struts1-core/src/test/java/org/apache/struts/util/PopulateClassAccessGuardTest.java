package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.commons.beanutils.LazyDynaBean;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Regression tests of the populate-level class-access guard (the second
 * defense layer behind the <code>RequestUtils</code> CVE-2014-0114
 * filter): a parameter name that uses <code>class</code> as a property
 * name — bare, nested, indexed or mapped — is skipped silently, and the
 * remaining parameters are still populated. The values deliberately are
 * not valid class names, so a conversion attempt would fail populate.
 */
class PopulateClassAccessGuardTest {

    /** A form with a readable nested <code>servlet</code> property: the guard must
 * fire even when navigation would succeed. */
    public static class GuardForm {
        private String name;
        private Nested servlet = new Nested();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Nested getServlet() {
            return servlet;
        }

        public void setServlet(Nested servlet) {
            this.servlet = servlet;
        }
    }

    public static class Nested {
        private String nestedProp;

        public String getNestedProp() {
            return nestedProp;
        }

        public void setNestedProp(String nestedProp) {
            this.nestedProp = nestedProp;
        }
    }

    @Test
    void bareClassParameterIsSkipped() throws Exception {
        GuardForm form = populate(new GuardForm(), "class", "not.a.Class",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void classUppercaseParameterIsSkipped() throws Exception {
        GuardForm form = populate(new GuardForm(), "CLASS", "not.a.Class",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void trailingClassSegmentIsSkipped() throws Exception {
        GuardForm form = populate(new GuardForm(), "x.class", "not.a.Class",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void classAccessThroughReadableNestedPropertyIsSkipped() throws Exception {
        GuardForm form = populate(new GuardForm(), "servlet.class",
            "not.a.Class", "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void classLoaderChainIsSkipped() throws Exception {
        GuardForm form = populate(new GuardForm(),
            "class.module.classLoader.DefaultAssertionStatus", "1",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void indexedClassAccessIsSkipped() throws Exception {
        GuardForm form = populate(new GuardForm(), "class[0]", "1",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void mappedClassAccessIsSkipped() throws Exception {
        GuardForm form = populate(new GuardForm(), "class(key)", "1",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void nestedClassAccessIsSkipped() throws Exception {
        GuardForm form = populate(new GuardForm(), "a.class.b", "1",
            "name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void lazyFormDoesNotGetClassProperty() throws Exception {
        LazyDynaBean dyna = new LazyDynaBean();
        populate(dyna, "class", "not.a.Class", "anything", "1");
        assertFalse(hasProperty(dyna, "class"),
            "the class entry is not added to a lazy form");
        assertEquals("1", dyna.get("anything"));
    }

    @Test
    void lazyFormClassTraversalIsSkipped() throws Exception {
        LazyDynaBean dyna = new LazyDynaBean();
        populate(dyna, "class.classLoader", "1", "anything", "1");
        assertFalse(hasProperty(dyna, "class"));
        assertFalse(hasProperty(dyna, "classLoader"));
        assertEquals("1", dyna.get("anything"));
    }

    private boolean hasProperty(LazyDynaBean dyna, String name) {
        for (var property : dyna.getDynaClass().getDynaProperties()) {
            if (property.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private <T> T populate(T bean, String... params) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.setParameter(params[i], params[i + 1].split(",", -1));
        }
        RequestUtils.populate(bean, request);
        return bean;
    }
}
