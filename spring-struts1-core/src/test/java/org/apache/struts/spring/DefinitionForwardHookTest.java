package org.apache.struts.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.impl.ModuleConfigImpl;

/**
 * Unit tests of the Spring-native definition-forward hook in
 * {@link StrutsHandlerAdapter}: registered {@link DefinitionForwardResolver}
 * beans get the chance to render the forward as a named view definition
 * before the ordinary dispatcher forward, and an ordinary path is untouched
 * when no resolver produces a response.
 */
class DefinitionForwardHookTest {

    /** Stub resolver with configurable outcome. */
    static class StubResolver implements DefinitionForwardResolver {
        boolean resolved;
        String seenPath;

        @Override
        public boolean resolveForward(String path,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response)
            throws IOException {
            seenPath = path;
            if (resolved) {
                PrintWriter writer = response.getWriter();
                writer.write("TILE-RENDERED");
                writer.flush();
            }
            return resolved;
        }
    }

    private AnnotationConfigWebApplicationContext context;

    private StrutsHandlerAdapter adapterWith(StubResolver resolver) {
        context = new AnnotationConfigWebApplicationContext();
        context.refresh();
        ActionServlet servlet = new ActionServlet();
        servlet.setApplicationContext(context);
        if (resolver != null) {
            context.getBeanFactory().registerSingleton("stubResolver",
                resolver);
        }
        StrutsHandlerAdapter adapter = new StrutsHandlerAdapter(servlet,
            new StrutsInstanceResolver());
        adapter.setServletContext(new MockServletContext());
        return adapter;
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET",
            "/someAction.do");
        request.setAttribute(Globals.MODULE_KEY, new ModuleConfigImpl(""));
        return request;
    }

    @Test
    void definitionForwardIsRenderedByResolver() throws Exception {
        StubResolver resolver = new StubResolver();
        resolver.resolved = true;
        StrutsHandlerAdapter adapter = adapterWith(resolver);
        MockHttpServletResponse response = new MockHttpServletResponse();

        adapter.processForwardConfig(request(), response,
            new ActionForward("/mainLayout"));

        assertEquals("/mainLayout", resolver.seenPath);
        assertEquals("TILE-RENDERED", response.getContentAsString());
        assertNull(response.getForwardedUrl(),
            "definition forward must not reach the dispatcher");
    }

    @Test
    void resolverDecliningLeavesOrdinaryPathUntouched() throws Exception {
        StubResolver resolver = new StubResolver();
        resolver.resolved = false;
        StrutsHandlerAdapter adapter = adapterWith(resolver);
        MockHttpServletResponse response = new MockHttpServletResponse();

        adapter.processForwardConfig(request(), response,
            new ActionForward("/layout.jsp"));

        assertEquals("/layout.jsp", resolver.seenPath);
        assertEquals("/layout.jsp", response.getForwardedUrl());
    }

    @Test
    void noResolverBeansLeavesOrdinaryPathUntouched() throws Exception {
        StrutsHandlerAdapter adapter = adapterWith(null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        adapter.processForwardConfig(request(), response,
            new ActionForward("/layout.jsp"));

        assertEquals("/layout.jsp", response.getForwardedUrl());
    }
}
