package app;

import jakarta.servlet.ServletException;

import org.apache.struts.config.ModuleConfig;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;

/**
 * Simple plugin replacing the weblegacy blank's ValidatorPlugIn (the
 * validator arrives in phase 3): records its lifecycle in the servlet
 * context so the integration run can verify init/destroy.
 */
public class TestPlugin implements PlugIn {

    @Override
    public void init(ActionServlet servlet, ModuleConfig config) throws ServletException {
        servlet.getServletContext().setAttribute("blank.plugin.init", "true");
    }

    @Override
    public void destroy() {
        // nothing to release
    }
}
