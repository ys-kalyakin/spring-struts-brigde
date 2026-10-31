package org.apache.struts.tiles.taglib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.DefinitionsFactoryConfig;
import org.apache.struts.tiles.TilesUtilImpl;

/**
 * Unit tests of the tiles tags on their Spring-aware bases: the
 * definition/layout pair rendering with a controller, nested tiles with
 * put overrides, and the attribute-exporting tags importAttribute and
 * useAttribute. Runs against the engine-default resolver service (no
 * Spring context), as the tags must work standalone.
 */
class TilesTagRenderTest {

    private static final String FIXTURES =
        "/org/apache/struts/tiles/fixtures/tiles-defs.xml";

    private static final String COMPONENT_CONTEXT =
        ComponentConstants.COMPONENT_CONTEXT;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockPageContext pageContext;

    @BeforeEach
    void setUp() throws Exception {
        // other tests (the webapp IT) may have installed a module-aware
        // TilesUtil implementation as static state; unit tests need the
        // plain default
        org.apache.struts.tiles.testsupport.TilesStaticReset.resetTilesUtil();

        MockServletContext servletContext = new MockServletContext();
        DefinitionsFactoryConfig config = new DefinitionsFactoryConfig();
        config.setDefinitionConfigFiles(FIXTURES);
        config.setParserValidate(false);
        config.setFactoryName("");
        new TilesUtilImpl().createDefinitionsFactory(servletContext, config);

        request = new MockHttpServletRequest("GET", "/page.jsp");
        response = new MockHttpServletResponse();
        pageContext = new MockPageContext(servletContext, request, response);
    }

    @AfterEach
    void tearDown() {
        request.setAttribute(COMPONENT_CONTEXT, null);
    }

    private InsertTag insertTag(String definition) throws Exception {
        InsertTag tag = new InsertTag();
        tag.setPageContext(pageContext);
        tag.setDefinition(definition);
        return tag;
    }

    @Test
    void insertDefinitionRunsControllerAndForwardsLayout() throws Exception {
        InsertTag tag = insertTag(".controlled");
        tag.doStartTag();
        tag.doEndTag();

        // the controller saw the definition attributes via the sub-context
        assertEquals("TITLE:Tile Title;BODY:null", response.getContentAsString());
        assertEquals("/controlled.jsp", response.getIncludedUrl());
    }

    @Test
    void nestedPutOverridesAttributeOfDefinition() throws Exception {
        InsertTag tag = insertTag(".controlled");
        tag.doStartTag();
        PutTag put = new PutTag();
        put.setPageContext(pageContext);
        put.setParent(tag);
        put.setName("body");
        put.setValue("/nested-body.jsp");
        put.doStartTag();
        put.doEndTag();
        tag.doEndTag();

        assertEquals("TITLE:Tile Title;BODY:/nested-body.jsp",
            response.getContentAsString());
        assertEquals("/controlled.jsp", response.getIncludedUrl());
    }

    @Test
    void extendsDefinitionRendersBaseLayoutWithOverrides() throws Exception {
        InsertTag tag = insertTag(".page");
        tag.doStartTag();
        tag.doEndTag();

        // no controller: layout include still happens with the overridden body
        assertEquals("/layout.jsp", response.getIncludedUrl());
        // classic behavior: without an enclosing context, the inserted
        // sub-context stays in the request
        ComponentContext left = (ComponentContext)
            request.getAttribute(COMPONENT_CONTEXT);
        assertEquals("/other-body.jsp", left.getAttribute("body"));
    }

    @Test
    void contextSetOutsideIsVisibleToInsertHandler() throws Exception {
        // an enclosing tile context (as an outer tiles:insert would set it)
        ComponentContext outer = new ComponentContext(
            Map.of("title", "Outer Title"));
        ComponentContext.setContext(outer, request);

        InsertTag tag = insertTag(".controlled");
        tag.doStartTag();
        tag.doEndTag();

        assertEquals("TITLE:Tile Title;BODY:null", response.getContentAsString());
        assertSame(outer, request.getAttribute(COMPONENT_CONTEXT),
            "the outer context is restored after insert");
    }

    private static void assertSame(Object expected, Object actual, String message) {
        assertTrue(expected == actual, message);
    }

    @Test
    void importAttributeExportsAllAttributes() throws Exception {
        ComponentContext context = new ComponentContext(
            Map.of("title", "Imported", "body", "/b.jsp"));
        ComponentContext.setContext(context, request);

        ImportAttributeTag tag = new ImportAttributeTag();
        tag.setPageContext(pageContext);
        tag.doStartTag();
        tag.doEndTag();

        assertEquals("Imported", pageContext.getAttribute("title"));
        assertEquals("/b.jsp", pageContext.getAttribute("body"));
    }

    @Test
    void importAttributeExportsSingleNamedAttribute() throws Exception {
        ComponentContext context = new ComponentContext(
            Map.of("title", "Imported", "body", "/b.jsp"));
        ComponentContext.setContext(context, request);

        ImportAttributeTag tag = new ImportAttributeTag();
        tag.setPageContext(pageContext);
        tag.setName("title");
        tag.doStartTag();
        tag.doEndTag();

        assertEquals("Imported", pageContext.getAttribute("title"));
        assertNull(pageContext.getAttribute("body"),
            "unnamed attributes are not exported");
    }

    @Test
    void useAttributeExposesContextAttributeInScope() throws Exception {
        ComponentContext context = new ComponentContext(
            Map.of("title", "Exposed"));
        ComponentContext.setContext(context, request);

        UseAttributeTag tag = new UseAttributeTag();
        tag.setPageContext(pageContext);
        tag.setId("theTitle");
        tag.setName("title");
        tag.setScope("page");
        tag.doStartTag();
        tag.doEndTag();

        assertEquals("Exposed", pageContext.getAttribute("theTitle"));
    }

    @Test
    void getAsStringPrintsCurrentTileAttribute() throws Exception {
        ComponentContext context = new ComponentContext(
            Map.of("title", "Printed"));
        ComponentContext.setContext(context, request);

        GetAttributeTag tag = new GetAttributeTag();
        tag.setPageContext(pageContext);
        tag.setName("title");
        tag.setIgnore(false);
        tag.doStartTag();
        tag.doEndTag();

        assertTrue(response.getContentAsString().contains("Printed"),
            "attribute value written to the page output");
    }

    @Test
    void getAsStringMissingAttributeWithoutIgnoreThrows() throws Exception {
        ComponentContext context = new ComponentContext(Map.of());
        ComponentContext.setContext(context, request);

        GetAttributeTag tag = new GetAttributeTag();
        tag.setPageContext(pageContext);
        tag.setName("missing");
        tag.setIgnore(false);

        Exception thrown = null;
        try {
            tag.doStartTag();
            tag.doEndTag();
        } catch (Exception ex) {
            thrown = ex;
        }
        assertNotNull(thrown, "missing attribute without ignore must throw");
    }
}
