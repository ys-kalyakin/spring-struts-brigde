package org.apache.struts.fixtures;

import jakarta.servlet.ServletException;

import org.apache.struts.config.ModuleConfig;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;

/**
 * PlugIn fixture that records init/destroy and a configured property in the
 * servlet context so tests can verify the lifecycle.
 */
public class FixturePlugin implements PlugIn {

    private String message;

    private boolean initialized;

    private boolean destroyed;

    @Override
    public void init(ActionServlet servlet, ModuleConfig config) throws ServletException {
        initialized = true;
        servlet.getServletContext().setAttribute("fixturePlugin.message",
            message);
    }

    @Override
    public void destroy() {
        destroyed = true;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
