package org.apache.struts.tiles;

import java.io.Serializable;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;

/**
 * Component repository interface.
 * This interface allows to retrieve an definition by its name, independently of the
 * factory implementation.
 * Implementation must be Serializable, in order to be compliant with web Container
 * having this constraint (Weblogic 6.x).
 * @deprecated Use DefinitionsFactory instead.
*/
@Deprecated
public interface ComponentDefinitionsFactory extends Serializable {

   /**
     * Get a definition by its name.
     * @param name Name of requested definition.
     * @param request Current servelet request
     * @param servletContext current servlet context
     * @throws DefinitionsFactoryException An error occur while getting definition.
     * @throws NoSuchDefinitionException No definition found for specified name
     * Implementation can throw more accurate exception as a subclass of this exception
   */
    ComponentDefinition getDefinition(String name, ServletRequest request,
        ServletContext servletContext)
        throws NoSuchDefinitionException, DefinitionsFactoryException;

   /**
     * Init factory.
     * This method is called exactly once immediately after factory creation in
     * case of internal creation (by DefinitionUtil).
     * @param servletContext Servlet Context passed to newly created factory.
     * @param properties Map of name/property passed to newly created factory.
     * Map can contains more properties than requested.
     * @throws DefinitionsFactoryException An error occur during initialization.
   */
    void initFactory(ServletContext servletContext, Map<String, Object> properties) throws DefinitionsFactoryException;
}
