package org.apache.struts.spring;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ModuleConfig;

/**
 * Spring MVC handler mapping that routes requests to Struts action mappings
 * using suffix mapping (by default <code>*.do</code>): the request
 * <code>/Welcome.do</code> resolves to the mapping registered under path
 * <code>/Welcome</code>. Requests to unregistered paths yield no handler so
 * the container produces 404.
 */
public class StrutsHandlerMapping extends AbstractHandlerMapping {

    private static final Logger log =
        LoggerFactory.getLogger(StrutsHandlerMapping.class);

    /** The URL suffix that selects Struts actions (classic extension mapping). */
    private String extension = ".do";

    private final ActionServlet actionServlet;

    public StrutsHandlerMapping(ActionServlet actionServlet) {
        this.actionServlet = actionServlet;
    }

    /**
     * Return the URL suffix used to route action requests.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Configure the URL suffix used to route action requests.
     */
    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    protected Object getHandlerInternal(HttpServletRequest request)
        throws Exception {
        String path = request.getServletPath();
        if ((path == null || path.isEmpty())
            && request.getPathInfo() != null) {
            path = request.getPathInfo();
        }
        if (path == null) {
            return null;
        }

        String actionPath;
        if (extension == null || extension.isEmpty()) {
            actionPath = path;
        } else {
            if (!path.endsWith(extension)) {
                log.debug("Request path {} does not match extension {}",
                    path, extension);
                return null;
            }
            actionPath = path.substring(0,
                path.length() - extension.length());
        }

        ModuleConfig moduleConfig = actionServlet.getModuleConfig();
        if (moduleConfig == null) {
            return null;
        }
        ActionConfig config = moduleConfig.findActionConfig(actionPath);
        log.debug("Routing path={} actionPath={} config={}", path, actionPath,
            (config == null ? "NOT FOUND" : config.getType()));
        if (config == null) {
            return null;
        }
        if (!(config instanceof ActionMapping)) {
            throw new ServletException(
                "ActionConfig of path " + actionPath
                    + " is not an ActionMapping: the action-mappings type must extend org.apache.struts.action.ActionMapping");
        }
        return config;
    }
}
