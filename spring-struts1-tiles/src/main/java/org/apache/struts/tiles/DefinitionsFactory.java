package org.apache.struts.tiles;

import java.io.Serializable;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;

/**
 * Tiles Definition factory.
 * This interface replace old ComponentDefinitionsFactory.
 * Main method getDefinition() is exactly the same. Initialization method change.
 * This interface allows to retrieve a definition by its name, independently of
 * the factory implementation.
 * Object life cycle is as follow:
 * <ul>
 * <li>Constructor: create object</li>
 * <li>setConfig: set config and initialize factory. After first call to this
 * method, factory is operational.</li>
 * <li>destroy: factory is being shutdown.</li>
 * </ul>
 * Implementation must be Serializable, in order to be compliant with web Container
 * having this constraint (Weblogic 6.x).
 */
public interface DefinitionsFactory extends Serializable {

   /**
     * Get a definition by its name.
     * @param name Name of requested definition.
     * @param request Current servelet request
     * @param servletContext current servlet context
     * @throws DefinitionsFactoryException An error occur while getting definition.
     * @throws NoSuchDefinitionException No definition found for specified name
     * Implementation can throw more accurate exception as a subclass of this exception
   */
    ComponentDefinition getDefinition(String name, ServletRequest request, ServletContext servletContext)
        throws NoSuchDefinitionException,DefinitionsFactoryException;

   /**
    * Init definition factory.
    * This method is called immediately after factory creation, and prior any call
    * to setConfig().
    *
    * @param config Configuration object used to set factory configuration.
    * @param servletContext Servlet Context passed to factory.
    * @throws DefinitionsFactoryException An error occur during initialization.
    */
    void init(DefinitionsFactoryConfig config, ServletContext servletContext)
        throws DefinitionsFactoryException;

    /**
     * <p>Receive notification that the factory is being
     * shut down.</p>
     */
    void destroy();

   /**
    * Set factory configuration.
    * This method is used to change factory configuration.
    * This method is optional, and can send an exception if implementation
    * doesn't allow change in configuration.
    *
    * @param config Configuration object used to set factory configuration.
    * @param servletContext Servlet Context passed to factory.
    * @throws DefinitionsFactoryException An error occur during initialization.
    */
    void setConfig(DefinitionsFactoryConfig config, ServletContext servletContext)
        throws DefinitionsFactoryException;

   /**
    * Get factory configuration.
    * @return TilesConfig
    */
    DefinitionsFactoryConfig getConfig();


}
