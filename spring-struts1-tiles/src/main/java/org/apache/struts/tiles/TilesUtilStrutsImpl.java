package org.apache.struts.tiles;

import jakarta.servlet.ServletContext;

import org.apache.struts.config.ModuleConfig;

/**
 * TilesUtil implementation for Struts 1.1 with one single factory.
 * This class contains default implementation of utilities. This implementation
 * is intended to be used with Struts 1.1.
 * This class is used as the base class for all Struts 1.1 implementations of TilesUtil.
 */
public class TilesUtilStrutsImpl extends TilesUtilImpl {
    private static final long serialVersionUID = 6361461121282446038L;

    /**
     * Get definition factory for the module attached to the specified moduleConfig.
     * @param servletContext Current servlet context
     * @param moduleConfig Module config of the module for which the factory is requested.
     * @return Definitions factory or null if not found.
     */
    public DefinitionsFactory getDefinitionsFactory(
        ServletContext servletContext,
        ModuleConfig moduleConfig) {

        return (DefinitionsFactory) servletContext.getAttribute(DEFINITIONS_FACTORY);
    }

}
