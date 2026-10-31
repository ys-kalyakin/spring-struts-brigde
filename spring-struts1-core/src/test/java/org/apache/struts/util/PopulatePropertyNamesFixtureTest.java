package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.fixtures.NamesForm;

/**
 * Fixture tests of the parameter-name syntax accepted by
 * {@link RequestUtils#populate}: simple, indexed, mapped, nested names and
 * their combinations, plus the skipped names. These pin the observable
 * behavior of the current implementation, including the beanutils
 * asymmetry: a top-level mapped property receives the raw parameter value
 * (String[]), while an indexed mapped element receives the first parameter
 * element as a String.
 */
class PopulatePropertyNamesFixtureTest {

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
    void simpleParameterNamePopulatesProperty() throws Exception {
        NamesForm form = populated("name", "Alice");
        assertEquals("Alice", form.getName());
    }

    @Test
    void indexedParameterSetsSingleElement() throws Exception {
        NamesForm form = populated("intIndexed[0]", "5");
        assertEquals(5, form.getIntIndexed()[0]);
        assertEquals(10, form.getIntIndexed()[1],
            "the rest of the array is untouched");
    }

    @Test
    void mappedParameterReceivesRawParameterValue() throws Exception {
        NamesForm form = populated("mapValue(color)", "red");
        Object value = form.getMapValue().get("color");
        assertEquals(String[].class, value.getClass(),
            "beanutils puts the raw parameter value into the map");
        assertEquals("red", ((String[]) value)[0]);
    }

    @Test
    void nestedParameterPopulatesNestedBean() throws Exception {
        NamesForm form = populated("nested.intProperty", "7");
        assertEquals(7, form.getNested().getIntProperty());
    }

    @Test
    void nestedIndexedCombinationPopulates() throws Exception {
        NamesForm form = populated("nested.strings[1]", "x");
        assertEquals("x", form.getNested().getStrings()[1]);
        assertEquals("a", form.getNested().getStrings()[0]);
    }

    @Test
    void nestedIndexedMappedCombinationReceivesFirstElement() throws Exception {
        NamesForm form = new NamesForm();
        form.getNested().getMaps()[0] = new HashMap<>();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("nested.maps[0](color)", new String[] {"red",
            "green"});
        RequestUtils.populate(form, request);

        Map<String, String> map = form.getNested().getMaps()[0];
        assertEquals("red", map.get("color"),
            "an existing map element receives the first parameter element");
    }

    @Test
    void mappedIntoNullArrayElementIsSkipped() throws Exception {
        NamesForm form = new NamesForm();
        form.getNested().getMaps()[0] = null;

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("nested.maps[0](color)", "red");
        assertDoesNotThrow(() -> RequestUtils.populate(form, request));

        assertEquals(null, form.getNested().getMaps()[0],
            "a null element is silently skipped");
    }

    @Test
    void nullNestedPropertyIsSkipped() throws Exception {
        NamesForm form = new NamesForm();
        form.setNested(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("nested.intProperty", "7");
        assertDoesNotThrow(() -> RequestUtils.populate(form, request));

        assertEquals(null, form.getNested(),
            "a null nested target is silently skipped");
    }

    @Test
    void indexedWriteMethodIsResolvedWithoutArrayField() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("zipPostal[1]", "62704");

        org.apache.struts.fixtures.IndexedMethodsForm form =
            new org.apache.struts.fixtures.IndexedMethodsForm();
        RequestUtils.populate(form, request);

        assertEquals("62704", form.getZipPostal(1),
            "an indexed write method backs the property");
        assertEquals(null, form.getZipPostal(0),
            "the other elements are untouched");
    }

    @Test
    void unknownPropertyNameIsSkipped() throws Exception {
        NamesForm form = populated("doesNotExist", "1");
        assertEquals(null, form.getName());
    }

    @Test
    void strutsServiceParametersAreSkipped() {
        assertDoesNotThrow(
            () -> populated("org.apache.struts.action.CANCEL", "1"));
    }

    @Test
    void classAccessPatternIsIgnored() {
        assertDoesNotThrow(() -> populated("class", "java.lang.String"));
    }
}
