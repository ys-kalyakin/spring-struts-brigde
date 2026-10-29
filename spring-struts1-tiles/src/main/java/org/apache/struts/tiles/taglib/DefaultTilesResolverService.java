package org.apache.struts.tiles.taglib;

import jakarta.servlet.jsp.PageContext;

import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.TilesUtil;

/**
 * Engine default of {@link TilesResolverService}: definition resolution
 * through the classic static {@link TilesUtil} facade and the tile context
 * read from the classic request attribute.
 */
public class DefaultTilesResolverService implements TilesResolverService {

    @Override
    public ComponentDefinition findDefinition(String name,
        PageContext pageContext) throws DefinitionsFactoryException {
        return TilesUtil.getDefinition(name, pageContext.getRequest(),
            pageContext.getServletContext());
    }

    @Override
    public ComponentContext getCurrentContext(PageContext pageContext) {
        return (ComponentContext) pageContext.getAttribute(
            ComponentConstants.COMPONENT_CONTEXT, PageContext.REQUEST_SCOPE);
    }
}
