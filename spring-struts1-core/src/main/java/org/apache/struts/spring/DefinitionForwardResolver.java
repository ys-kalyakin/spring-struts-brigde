package org.apache.struts.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Extension point of the request pipeline: resolves a forward path as a
 * named view definition before ordinary dispatch. The Tiles plugin
 * registers an implementation as a Spring bean; when no such bean exists
 * the pipeline behaves exactly like the classic engine.
 *
 * <p>This is the Spring-native counterpart of the classic
 * <code>TilesRequestProcessor</code>/<code>TilesPreProcessor</code>
 * integration: no processor is swapped, the hook is a bean.</p>
 */
public interface DefinitionForwardResolver {

    /**
     * Try to resolve the given forward path as a definition and produce the
     * response.
     *
     * @param path     the raw forward path (a definition name when it is
     *                 not a resource path)
     * @param request  current request
     * @param response current response
     * @return <code>true</code> if the path was resolved as a definition and
     *         the response has been produced; <code>false</code> to let the
     *         pipeline proceed with ordinary dispatch
     */
    boolean resolveForward(String path, HttpServletRequest request,
        HttpServletResponse response) throws Exception;
}
