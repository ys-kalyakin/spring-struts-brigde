package org.apache.struts.action;

import jakarta.servlet.ServletException;

import org.apache.struts.config.ModuleConfig;

/**
 * A <strong>PlugIn</strong> is a configuration time plugin that provides a
 * way for an application to hook its lifecycle into the Struts lifecycle.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.action.PlugIn).</p>
 */
public interface PlugIn {

    /**
     * Receive notification that the specified module is being started up.
     *
     * @param servlet ActionServlet that is managing all the modules in this
     *                web application
     * @param config  ModuleConfig for the module with which this plugin is
     *                associated
     */
    void init(ActionServlet servlet, ModuleConfig config)
        throws ServletException;

    /**
     * Receive notification that our module is being shut down.
     */
    void destroy();
}
