package org.apache.struts.tiles;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.spring.DefinitionForwardResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring hook registered by the Tiles plugin: resolves a forward path as a
 * tiles definition and renders it. Ported from the classic
 * <code>TilesRequestProcessor.processTilesDefinition</code> — the same
 * semantics (nested tile contexts include instead of forward, action-set
 * definitions override attributes), just wired as a bean instead of a
 * swapped request processor.
 */
public class TilesDefinitionForwardResolver
    implements DefinitionForwardResolver {

    private final Logger log =
        LoggerFactory.getLogger(TilesDefinitionForwardResolver.class);

    @Override
    public boolean resolveForward(String definitionName,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        // Do we do a forward (original behavior) or an include?
        boolean doInclude = false;

        // Controller associated to a definition, if any
        Controller controller = null;

        // Computed uri to include
        String uri = null;

        ComponentContext tileContext = null;

        try {
            // Get current tile context if any.
            // If context exist, we will do an include
            tileContext = ComponentContext.getContext(request);
            doInclude = (tileContext != null);
            ComponentDefinition definition = null;

            // Process tiles definition names only if a definition factory
            // exists and the definition is found.
            DefinitionsFactory factory = TilesUtil.getDefinitionsFactory(
                request, request.getServletContext());
            if (factory != null) {
                // Get definition of tiles/component corresponding to uri.
                try {
                    definition = factory.getDefinition(definitionName,
                        request, request.getServletContext());
                } catch (NoSuchDefinitionException ex) {
                    // Ignore not found
                    log.debug("NoSuchDefinitionException {}", ex.getMessage());
                }
                if (definition != null) { // We have a definition.
                    // We use it to complete missing attribute in context.
                    // We also get uri, controller.
                    uri = definition.getPath();
                    controller = definition.getOrCreateController();

                    if (tileContext == null) {
                        tileContext =
                            new ComponentContext(definition.getAttributes());
                        ComponentContext.setContext(tileContext, request);
                    } else {
                        tileContext.addMissing(definition.getAttributes());
                    }
                }
            }

            // Process definition set in Action, if any.
            definition = DefinitionsUtil.getActionDefinition(request);
            if (definition != null) { // We have a definition.
                // We use it to complete missing attribute in context.
                // We also overload uri and controller if set in definition.
                if (definition.getPath() != null) {
                    uri = definition.getPath();
                }

                if (definition.getOrCreateController() != null) {
                    controller = definition.getOrCreateController();
                }

                if (tileContext == null) {
                    tileContext =
                        new ComponentContext(definition.getAttributes());
                    ComponentContext.setContext(tileContext, request);
                } else {
                    tileContext.addMissing(definition.getAttributes());
                }
            }
        } catch (java.lang.InstantiationException ex) {
            log.error("Can't create associated controller", ex);
            throw new ServletException("Can't create associated controller",
                ex);
        } catch (DefinitionsFactoryException ex) {
            throw new ServletException(ex);
        }

        // Have we found a definition?
        if (uri == null) {
            return false;
        }

        // Execute controller associated to definition, if any.
        if (controller != null) {
            try {
                controller.execute(tileContext, request, response,
                    request.getServletContext());
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        // If request comes from a previous Tile, do an include.
        log.debug("uri={} doInclude={}", uri, doInclude);

        if (doInclude) {
            TilesUtil.doInclude(uri, request, response,
                request.getServletContext());
        } else {
            TilesUtil.doForward(uri, request, response,
                request.getServletContext());
        }
        return true;
    }

}
