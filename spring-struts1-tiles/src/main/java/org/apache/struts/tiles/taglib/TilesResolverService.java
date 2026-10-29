package org.apache.struts.tiles.taglib;

import jakarta.servlet.jsp.PageContext;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsFactoryException;

/**
 * The shared tiles-tag logic as a Spring service: definition resolution
 * against the current factory and access to the current tile context. The
 * tags obtain the service through the shared tag-services lookup, so an
 * application overrides the behavior of the tiles tags by defining its own
 * bean of this type; without a bean the engine default
 * ({@link DefaultTilesResolverService}) is used.
 */
public interface TilesResolverService {

    /**
     * Get the component definition of the given name, resolved against the
     * definitions factory visible to the current request.
     * @param name Definition name.
     * @param pageContext The page context of the calling tag.
     * @throws DefinitionsFactoryException when the definition or the
     *  factory can't be found (classic tiles exceptions are preserved, each
     *  tag maps them to its own classic message)
     */
    ComponentDefinition findDefinition(String name, PageContext pageContext)
        throws DefinitionsFactoryException;

    /**
     * Return the current tile context of the request, or <code>null</code>
     * when no context has been established yet.
     * @param pageContext The page context of the calling tag.
     */
    ComponentContext getCurrentContext(PageContext pageContext);
}
