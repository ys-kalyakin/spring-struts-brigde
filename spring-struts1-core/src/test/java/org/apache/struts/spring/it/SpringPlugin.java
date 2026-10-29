package org.apache.struts.spring.it;

import jakarta.servlet.ServletException;

import org.apache.struts.config.ModuleConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;

/**
 * Plugin fixture registered as a Spring bean: resolved from the context
 * with dependency injection.
 */
@Component
public class SpringPlugin implements PlugIn {

    @Autowired
    private transient GreeterService greeterService;

    @Override
    public void init(ActionServlet servlet, ModuleConfig config) throws ServletException {
        servlet.getServletContext().setAttribute("plugin.spring",
            greeterService.greet() + ":init");
    }

    @Override
    public void destroy() {
        // nothing to release
    }
}
