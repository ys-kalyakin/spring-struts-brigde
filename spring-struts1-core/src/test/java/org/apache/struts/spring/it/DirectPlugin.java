package org.apache.struts.spring.it;

import jakarta.servlet.ServletException;

import org.apache.struts.config.ModuleConfig;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;

/**
 * Plugin fixture created directly (not a Spring bean) with a set-property.
 */
public class DirectPlugin implements PlugIn {

    private String label;

    @Override
    public void init(ActionServlet servlet, ModuleConfig config) throws ServletException {
        servlet.getServletContext().setAttribute("plugin.direct",
            label + ":init");
    }

    @Override
    public void destroy() {
        // nothing to release
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
