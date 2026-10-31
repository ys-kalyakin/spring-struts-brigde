package org.apache.struts.spring;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.ModuleUtils;

/**
 * Spring MVC handler mapping that routes requests to Struts action mappings
 * using the classic servlet mapping semantics: either extension mapping
 * (by default <code>*.do</code>: the request <code>/Welcome.do</code>
 * resolves to the mapping registered under path <code>/Welcome</code>) or
 * path mapping (<code>/action/*</code>: the request
 * <code>/action/Welcome</code> resolves to the mapping
 * <code>/Welcome</code>). The module is selected by the request path prefix
 * (requests without a module prefix use the default module). Requests to
 * unregistered paths yield no handler so the container produces 404.
 */
public class StrutsHandlerMapping extends AbstractHandlerMapping {

    private static final Logger log =
        LoggerFactory.getLogger(StrutsHandlerMapping.class);

    /** The servlet mapping that selects Struts actions (classic format). */
    private String servletMapping = "*.do";

    private final ActionServlet actionServlet;

    /** Parsed mapping kind and value. */
    private String extension;

    private String prefix;

    public StrutsHandlerMapping(ActionServlet actionServlet) {
        this.actionServlet = actionServlet;
    }

    /**
     * Return the servlet mapping used to route action requests.
     */
    public String getServletMapping() {
        return servletMapping;
    }

    /**
     * Configure the servlet mapping used to route action requests: either an
     * extension mapping (<code>*.do</code>) or a path mapping
     * (<code>/action/*</code>).
     */
    public void setServletMapping(String servletMapping) {
        this.servletMapping = servletMapping;
        parseServletMapping();
    }

    /**
     * Configure the URL suffix used to route action requests (equivalent to
     * the servlet mapping <code>*&lt;extension&gt;</code>).
     */
    public void setExtension(String extension) {
        setServletMapping("*" + extension);
    }

    private void parseServletMapping() {
        extension = null;
        prefix = null;
        if (servletMapping == null) {
            return;
        }
        if (servletMapping.startsWith("*")) {
            extension = servletMapping.substring(1);
        } else if (servletMapping.endsWith("/*")) {
            prefix = servletMapping.substring(0,
                servletMapping.length() - 2);
        } else {
            throw new IllegalArgumentException(
                "Unsupported servlet mapping: " + servletMapping
                    + " (use *.ext or /prefix/*)");
        }
    }

    @Override
    protected void initApplicationContext() {
        super.initApplicationContext();
        // Expose the mapping for URL construction in the tag libraries
        getServletContext().setAttribute(Globals.SERVLET_KEY, servletMapping);
        parseServletMapping();
    }

    @Override
    protected Object getHandlerInternal(HttpServletRequest request)
        throws Exception {
        // For extension mappings the servlet path holds the whole path; for
        // prefix mappings (e.g. /action/*) the trailing part arrives as the
        // path info and must be appended.
        String path = request.getServletPath();
        if (request.getPathInfo() != null) {
            path = ((path == null) ? "" : path) + request.getPathInfo();
        }
        if (path == null) {
            return null;
        }

        String actionPath = stripMapping(path);
        if (actionPath == null) {
            log.debug("Request path {} does not match servlet mapping {}",
                path, servletMapping);
            return null;
        }

        // Select the module by request path prefix (default module otherwise)
        ModuleUtils.getInstance().selectModule(request, getServletContext());
        ModuleConfig moduleConfig = ModuleUtils.getInstance()
            .getModuleConfig(request);
        if (moduleConfig == null) {
            moduleConfig = actionServlet.getModuleConfig();
        }

        // The module prefix is part of the request path but not of the
        // mapping path: strip it before the lookup.
        String modulePrefix = moduleConfig.getPrefix();
        if (modulePrefix != null && modulePrefix.length() > 1
            && actionPath.startsWith(modulePrefix)) {
            actionPath = actionPath.substring(modulePrefix.length());
        }

        ActionConfig config = moduleConfig.findActionConfig(actionPath);
        if (log.isDebugEnabled()) {
            log.debug("Routing path={} actionPath={} module={} config={}",
                path, actionPath, moduleConfig.getPrefix(),
                (config == null ? "NOT FOUND" : config.getType()));
        }
        if (config == null) {
            return null;
        }
        if (!(config instanceof ActionMapping)) {
            throw new ServletException(
                "ActionConfig of path " + actionPath
                    + " is not an ActionMapping: the action-mappings type must extend"
                + " org.apache.struts.action.ActionMapping");
        }
        return config;
    }

    /**
     * Strip the servlet mapping (extension or path prefix) from the request
     * path, returning the action path, or null when the path does not match
     * the mapping.
     */
    private String stripMapping(String path) {
        if (extension != null) {
            if (!path.endsWith(extension)) {
                return null;
            }
            return path.substring(0, path.length() - extension.length());
        }
        if (prefix != null) {
            if (!path.startsWith(prefix)) {
                return null;
            }
            String stripped = path.substring(prefix.length());
            // An optional extension may still be present (e.g. /action/X.do)
            int slash = stripped.lastIndexOf('/');
            int dot = stripped.lastIndexOf('.');
            if ((dot > slash) && (dot >= 0)) {
                stripped = stripped.substring(0, dot);
            }
            return stripped;
        }
        return path;
    }
}
