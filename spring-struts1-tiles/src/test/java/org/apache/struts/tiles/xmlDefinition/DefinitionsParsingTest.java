package org.apache.struts.tiles.xmlDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;

import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsFactory;
import org.apache.struts.tiles.DefinitionsFactoryConfig;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.NoSuchDefinitionException;
import org.apache.struts.tiles.TilesUtilImpl;

/**
 * Unit tests of the tiles-definitions XML parsing: extends inheritance,
 * put attributes, putList lists and locale-specific definition files.
 */
class DefinitionsParsingTest {

    private static final String FIXTURES =
        "/org/apache/struts/tiles/fixtures/tiles-defs.xml";

    private DefinitionsFactory factory() throws DefinitionsFactoryException {
        DefinitionsFactoryConfig config = new DefinitionsFactoryConfig();
        config.setDefinitionConfigFiles(FIXTURES);
        config.setParserValidate(false);
        return new TilesUtilImpl().createDefinitionsFactory(
            new MockServletContext(), config);
    }

    @Test
    void parsesDefinitionWithPuts() throws Exception {
        ComponentDefinition definition =
            factory().getDefinition(".base", new MockHttpServletRequest(),
                new MockServletContext());
        assertNotNull(definition);
        assertEquals("/layout.jsp", definition.getPath());
        assertEquals("/header.jsp", definition.getAttribute("header"));
        assertEquals("/body.jsp", definition.getAttribute("body"));
    }

    @Test
    void extendsOverridesSingleAttribute() throws Exception {
        ComponentDefinition definition =
            factory().getDefinition(".page", new MockHttpServletRequest(),
                new MockServletContext());
        assertEquals("/other-body.jsp", definition.getAttribute("body"));
        // inherited attributes stay
        assertEquals("/header.jsp", definition.getAttribute("header"));
        assertEquals("/layout.jsp", definition.getPath());
    }

    @Test
    @SuppressWarnings("unchecked")
    void putListBuildsListAttribute() throws Exception {
        ComponentDefinition definition =
            factory().getDefinition(".menu", new MockHttpServletRequest(),
                new MockServletContext());
        Object items = definition.getAttribute("items");
        assertTrue(items instanceof List, "putList must yield a list");
        assertEquals(2, ((List<Object>) items).size());
    }

    @Test
    void localeFileOverridesBaseDefinitions() throws Exception {
        // the i18n factory reads its locale from the session attribute
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(java.util.Locale.ENGLISH);
        request.getSession().setAttribute(
            org.apache.struts.tiles.taglib.ComponentConstants.LOCALE_KEY,
            java.util.Locale.ENGLISH);
        ComponentDefinition definition =
            factory().getDefinition(".page", request,
                new MockServletContext());
        assertEquals("/english-body.jsp", definition.getAttribute("body"));

        // without a locale attribute the base file wins
        ComponentDefinition base = factory().getDefinition(".page",
            new MockHttpServletRequest(), new MockServletContext());
        assertEquals("/other-body.jsp", base.getAttribute("body"));
    }

    @Test
    void missingDefinitionYieldsNull() throws Exception {
        ComponentDefinition definition =
            factory().getDefinition(".nope", new MockHttpServletRequest(),
                new MockServletContext());
        assertEquals(null, definition,
            "missing definitions resolve to null like the reference");
    }
}
