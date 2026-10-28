package org.apache.struts.action;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.apache.struts.Globals;
import org.apache.struts.config.MessageResourcesConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.config.ModuleConfigFactory;
import org.apache.struts.config.PlugInConfig;
import org.apache.struts.config.StrutsConfigParser;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.MessageResourcesFactory;
import org.apache.struts.util.RequestUtils;

/**
 * <p><strong>ActionServlet</strong> is the facade of compatibility with
 * legacy Struts 1 code. Request processing itself is performed by the Spring
 * MVC engine ({@code StrutsHandlerMapping} + {@code StrutsHandlerAdapter});
 * the servlet owns the parsed {@link ModuleConfig}, initializes
 * <code>MessageResources</code> and <code>PlugIn</code> instances at startup,
 * and exposes the classic init-param contract (notably <code>config</code>).</p>
 *
 * <p>The servlet may be initialized either by the servlet container (when
 * registered in web.xml) or by the engine (as a Spring bean), in which case
 * it receives the init-params of the front <code>DispatcherServlet</code>.</p>
 */
public class ActionServlet extends HttpServlet
    implements ApplicationContextAware, DisposableBean {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
        LoggerFactory.getLogger(ActionServlet.class);

    /** Comma-separated list of configuration resources (init-param <code>config</code>). */
    protected String config = "/WEB-INF/struts-config.xml";

    /** Should the Digester validate the configuration XML against its DTD? */
    protected boolean validating = true;

    /** The parsed module configurations, keyed by prefix (default module: ""). */
    protected Map<String, ModuleConfig> moduleConfigs = new HashMap<>();

    /** The initialized plugin instances, in registration order. */
    protected List<PlugIn> plugIns = new ArrayList<>();

    /** The Spring context (used to resolve plugin beans and message resources). */
    protected transient ApplicationContext applicationContext;

    /** Guard against double initialization (container + engine initializer). */
    private boolean initialized;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initialize this servlet with the given configuration (skipped when
     * already initialized).
     */
    @Override
    public void init(jakarta.servlet.ServletConfig servletConfig)
        throws ServletException {
        if (initialized) {
            return;
        }
        super.init(servletConfig);
        initialized = true;
    }

    /**
     * Initialize this servlet: read init-params, parse the configuration,
     * initialize message resources and plugins.
     */
    @Override
    public void init() throws ServletException {
        initInternal();
        initModuleConfig();
        // Classic behavior: message resources and plugins are initialized
        // for EVERY module, the default module first.
        for (String prefix : orderedModulePrefixes()) {
            ModuleConfig mconfig = moduleConfigs.get(prefix);
            initModuleMessageResources(mconfig);
            initModulePlugIns(mconfig);
        }
        getServletContext().setAttribute(Globals.ACTION_SERVLET_KEY, this);
        getServletContext().setAttribute(Globals.MODULE_KEY, defaultModule());
        if (log.isDebugEnabled()) {
            log.debug("ActionServlet initialized with config={}", config);
        }
    }

    /**
     * All configured module prefixes in deterministic order: the default
     * module ("") first, then the other prefixes sorted lexicographically.
     */
    private List<String> orderedModulePrefixes() {
        List<String> prefixes = new ArrayList<>(moduleConfigs.keySet());
        prefixes.remove("");
        prefixes.sort(String::compareTo);
        prefixes.add(0, "");
        return prefixes;
    }

    /**
     * Gracefully shut down this servlet, releasing plugin resources in
     * reverse registration order.
     */
    @Override
    public void destroy() {
        for (int i = plugIns.size() - 1; i >= 0; i--) {
            try {
                plugIns.get(i).destroy();
            } catch (Throwable e) {
                log.error("Error destroying plugin {}",
                    plugInClassName(plugIns.get(i)), e);
            }
        }
        plugIns.clear();
    }

    private String plugInClassName(PlugIn plugin) {
        return (plugin == null) ? "null" : plugin.getClass().getName();
    }

    /**
     * Read the engine init-params: <code>config</code> and <code>validating</code>.
     */
    protected void initInternal() throws ServletException {
        String value = getInitParameter("config");
        if (value != null) {
            config = value;
        }
        value = getInitParameter("validating");
        if (value != null) {
            validating = Boolean.parseBoolean(value);
        }
    }

    /**
     * Parse the configuration resources named by the <code>config</code>
     * init-param (a comma-separated list), merging them into the default
     * module configuration. A missing or unparsable file fails startup with
     * the file name in the error.
     */
    protected void initModuleConfig() throws ServletException {
        initModuleConfig("", config);
        jakarta.servlet.ServletConfig servletConfig = getServletConfig();
        java.util.Enumeration<String> names = servletConfig.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith("config/")) {
                String prefix = "/" + name.substring("config/".length());
                initModuleConfig(prefix, servletConfig.getInitParameter(name));
            }
        }

        getServletContext().setAttribute(Globals.MODULE_PREFIXES_KEY,
            moduleConfigs.keySet().stream()
                .filter(prefix -> !prefix.isEmpty())
                .sorted()
                .toArray(String[]::new));
    }

    /**
     * Parse the comma-separated list of configuration files for one module
     * with the given prefix. A missing or unparsable file fails startup with
     * the file name in the error.
     */
    protected void initModuleConfig(String prefix, String configValue)
        throws ServletException {
        ModuleConfigFactory factoryObject = ModuleConfigFactory.createFactory();
        ModuleConfig mconfig = factoryObject.createModuleConfig(prefix);

        for (String file : configValue.split("\\s*,\\s*")) {
            String path = file.trim();
            if (path.isEmpty()) {
                continue;
            }
            URL url = null;
            try {
                url = getServletContext().getResource(path);
            } catch (java.net.MalformedURLException e) {
                throw new UnavailableException(
                    "Malformed configuration resource path " + path);
            }
            if (url == null) {
                throw new UnavailableException(
                    "Missing configuration resource " + path);
            }
            parseModuleConfigFile(url, mconfig);
        }

        mconfig.freeze();
        moduleConfigs.put(prefix, mconfig);
        getServletContext().setAttribute(Globals.MODULE_KEY + prefix, mconfig);
    }

    /**
     * Parse a single configuration file into the given module configuration.
     */
    protected void parseModuleConfigFile(URL url, ModuleConfig mconfig)
        throws ServletException {
        StrutsConfigParser.parse(url, mconfig, validating);
    }

    /**
     * Initialize the message resources for the module: create a
     * <code>MessageResources</code> for each <code>message-resources</code>
     * element and store it in the servlet context under its key, suffixed
     * with the module prefix (classic per-module storage).
     */
    protected void initModuleMessageResources(ModuleConfig mconfig)
        throws ServletException {
        String prefix = mconfig.getPrefix();
        for (MessageResourcesConfig mrc : mconfig
            .findMessageResourcesConfigs()) {
            try {
                MessageResourcesFactory factoryObject =
                    (MessageResourcesFactory) RequestUtils
                        .applicationInstance(mrc.getFactory(),
                            Thread.currentThread().getContextClassLoader());
                factoryObject.setConfig(mrc);
                factoryObject.setReturnNull(mrc.getNull());
                MessageResources resources =
                    factoryObject.createResources(mrc.getParameter());
                resources.setReturnNull(mrc.getNull());
                getServletContext().setAttribute(mrc.getKey() + prefix,
                    resources);
                if (prefix.isEmpty()) {
                    getServletContext().setAttribute(mrc.getKey(), resources);
                }
            } catch (Exception e) {
                throw new ServletException(
                    "Cannot initialize MessageResources of key "
                        + mrc.getKey() + " (parameter "
                        + mrc.getParameter() + ")", e);
            }
        }
    }

    /**
     * Initialize the plugins for the module in registration order. A plugin
     * class registered as a Spring bean is resolved from the context (with
     * dependency injection); otherwise it is created directly. Declared
     * <code>set-property</code> values are applied to the instance.
     */
    protected void initModulePlugIns(ModuleConfig mconfig)
        throws ServletException {
        List<PlugIn> modulePlugIns = new ArrayList<>();
        getServletContext().setAttribute(
            Globals.PLUG_INS_KEY + mconfig.getPrefix(), modulePlugIns);
        for (PlugInConfig plugInConfig : mconfig.findPlugInConfigs()) {
            String className = plugInConfig.getClassName();
            try {
                PlugIn plugin = resolvePlugIn(className);
                applyPluginProperties(plugin, plugInConfig);
                plugInConfig.freeze();
                plugin.init(this);
                modulePlugIns.add(plugin);
                plugIns.add(plugin);
            } catch (Exception e) {
                throw new ServletException(
                    "Cannot initialize plugin " + className, e);
            }
        }
    }

    /**
     * Resolve a plugin instance: from the Spring context if a bean of that
     * class is registered, otherwise by direct creation.
     */
    protected PlugIn resolvePlugIn(String className) throws Exception {
        if (applicationContext != null) {
            for (String beanName : applicationContext
                .getBeanNamesForType(PlugIn.class)) {
                Class<?> beanClass = applicationContext.getType(beanName);
                if (beanClass != null
                    && className.equals(beanClass.getName())) {
                    return applicationContext.getBean(beanName, PlugIn.class);
                }
            }
        }
        return (PlugIn) RequestUtils.applicationInstance(className,
            Thread.currentThread().getContextClassLoader());
    }

    private void applyPluginProperties(PlugIn plugin,
        PlugInConfig plugInConfig) throws Exception {
        Map<String, Object> properties = plugInConfig.getProperties();
        if (properties.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            BeanUtils.setProperty(plugin, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Return the default module configuration.
     */
    public ModuleConfig getModuleConfig() {
        return defaultModule();
    }

    /**
     * Return the module configuration for the given prefix (the default
     * module has prefix "").
     */
    public ModuleConfig getModuleConfig(String prefix) {
        return moduleConfigs.get(prefix);
    }

    private ModuleConfig defaultModule() {
        ModuleConfig mconfig = moduleConfigs.get("");
        if (mconfig == null) {
            throw new IllegalStateException(
                "Module configuration is not initialized yet");
        }
        return mconfig;
    }
}
