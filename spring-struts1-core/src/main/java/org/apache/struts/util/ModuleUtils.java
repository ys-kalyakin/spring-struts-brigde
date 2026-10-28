package org.apache.struts.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.Globals;
import org.apache.struts.config.MessageResourcesConfig;
import org.apache.struts.config.ModuleConfig;

/**
 * General purpose utility methods related to module processing.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.util.ModuleUtils).</p>
 */
public class ModuleUtils {

    /**
     * The request attribute under which the original servlet path of an
     * included request is stored (jakarta include semantics).
     */
    private static final String INCLUDE_SERVLET_PATH =
        "jakarta.servlet.include.servlet_path";

    /**
     * The Singleton instance.
     */
    private static final ModuleUtils instance = new ModuleUtils();

    private final Logger log = LoggerFactory.getLogger(ModuleUtils.class);

    protected ModuleUtils() {
        super();
    }

    /**
     * Returns the Singleton instance of ModuleUtils.
     */
    public static ModuleUtils getInstance() {
        return instance;
    }

    /**
     * Return the current ModuleConfig object stored in request, if it exists,
     * null otherwise.
     */
    public ModuleConfig getModuleConfig(HttpServletRequest request) {
        return (ModuleConfig) request.getAttribute(Globals.MODULE_KEY);
    }

    /**
     * Return the desired ModuleConfig object stored in context, if it exists,
     * null otherwise.
     */
    public ModuleConfig getModuleConfig(String prefix, ServletContext context) {
        if ((prefix == null) || "/".equals(prefix)) {
            return (ModuleConfig) context.getAttribute(Globals.MODULE_KEY);
        } else {
            return (ModuleConfig) context.getAttribute(Globals.MODULE_KEY
                + prefix);
        }
    }

    /**
     * Return the desired ModuleConfig object stored in context, if it exists,
     * otherwise return the current ModuleConfig.
     */
    public ModuleConfig getModuleConfig(String prefix,
        HttpServletRequest request, ServletContext context) {
        ModuleConfig moduleConfig = null;

        if (prefix != null) {
            moduleConfig = this.getModuleConfig(prefix, context);
        } else {
            moduleConfig = this.getModuleConfig(request, context);
        }

        return moduleConfig;
    }

    /**
     * Return the ModuleConfig object if it exists, null otherwise.
     */
    public ModuleConfig getModuleConfig(HttpServletRequest request,
        ServletContext context) {
        ModuleConfig moduleConfig = this.getModuleConfig(request);

        if (moduleConfig == null) {
            moduleConfig = this.getModuleConfig("", context);
            request.setAttribute(Globals.MODULE_KEY, moduleConfig);
        }

        return moduleConfig;
    }

    /**
     * Get the module name to which the specified request belongs.
     */
    public String getModuleName(HttpServletRequest request,
        ServletContext context) {
        String matchPath =
            (String) request.getAttribute(INCLUDE_SERVLET_PATH);

        if (matchPath == null) {
            matchPath = request.getServletPath();
        }

        return this.getModuleName(matchPath, context);
    }

    /**
     * Get the module name to which the specified uri belongs.
     */
    public String getModuleName(String matchPath, ServletContext context) {
        log.debug("Get module name for path {}", matchPath);

        String prefix = "";
        String[] prefixes = getModulePrefixes(context);

        int lastSlash = 0;

        while (prefix.isEmpty()
            && ((lastSlash = matchPath.lastIndexOf("/")) > 0)) {
            matchPath = matchPath.substring(0, lastSlash);

            for (int i = 0; i < prefixes.length; i++) {
                if (matchPath.equals(prefixes[i])) {
                    prefix = prefixes[i];

                    break;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Module name found: {}",
                prefix.isEmpty() ? "default" : prefix);
        }

        return prefix;
    }

    /**
     * Return the list of module prefixes that are defined for this web
     * application. The "" prefix for the default module is not included.
     */
    public String[] getModulePrefixes(ServletContext context) {
        return (String[]) context.getAttribute(Globals.MODULE_PREFIXES_KEY);
    }

    /**
     * Select the module to which the specified request belongs, and add
     * corresponding request attributes to this request.
     */
    public void selectModule(HttpServletRequest request, ServletContext context) {
        String prefix = getModuleName(request, context);

        this.selectModule(prefix, request, context);
    }

    /**
     * Select the module with the given prefix, and add the corresponding
     * request attributes to this request.
     */
    public void selectModule(String prefix, HttpServletRequest request,
        ServletContext context) {
        ModuleConfig config = getModuleConfig(prefix, context);

        if (config != null) {
            request.setAttribute(Globals.MODULE_KEY, config);

            MessageResourcesConfig[] mrConfig =
                config.findMessageResourcesConfigs();

            for (int i = 0; i < mrConfig.length; i++) {
                String key = mrConfig[i].getKey();
                MessageResources resources =
                    (MessageResources) context.getAttribute(key + prefix);

                if (resources != null) {
                    request.setAttribute(key, resources);
                } else {
                    request.removeAttribute(key);
                }
            }
        } else {
            request.removeAttribute(Globals.MODULE_KEY);
        }
    }
}
