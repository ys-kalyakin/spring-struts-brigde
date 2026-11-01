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
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.FormPropertyConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.MessageResourcesConfig;
import org.apache.struts.config.ControllerConfigValidator;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.config.ModuleConfigFactory;
import org.apache.struts.config.PlugInConfig;
import org.apache.struts.config.StrutsConfigParser;
import org.apache.struts.config.UnsupportedControllerConfigException;
import org.apache.struts.spring.EnvironmentServletConfig;
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

    /**
     * The Spring application context this facade was created in (used to
     * resolve handler beans and extension hooks).
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

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
     * The internal <code>MessageResources</code> of the engine, exposed by
     * the classic <code>getInternal()</code> accessor.
     */
    protected MessageResources internal = null;

    /**
     * The name of the framework resources bundle (classic default).
     */
    protected String internalName = "org.apache.struts.action.ActionResources";

    /**
     * Gracefully shut down the internal resources (classic
     * <code>destroyInternal</code> counterpart).
     */
    public void destroyInternal() {
        internal = null;
    }

    /**
     * Return the <code>MessageResources</code> instance containing our
     * internal message strings.
     */
    public MessageResources getInternal() {
        return internal;
    }

    /**
     * Read the engine init-params: <code>config</code> and <code>validating</code>,
     * and load the internal message resources.
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
        value = getInitParameter("internal");
        if (value != null) {
            internalName = value;
        }
        internal = MessageResources.getMessageResources(internalName);
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
                // The environment-backed adapter of Boot mode resolves
                // classpath: resources itself; the classic servlet resolves
                // against the servlet context.
                if (getServletConfig()
                    instanceof EnvironmentServletConfig envConfig) {
                    url = envConfig.resolveResource(path);
                } else {
                    url = getServletContext().getResource(path);
                }
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

        validateControllerConfig(mconfig);

        // Process config inheritance ("extends") before freezing: form
        // beans, then global forwards/exceptions, then actions (whose local
        // forwards/exceptions are processed in cascade).
        for (FormBeanConfig beanConfig : mconfig.findFormBeanConfigs()) {
            processFormBeanExtension(beanConfig, mconfig);
        }
        for (ForwardConfig forwardConfig : mconfig.findForwardConfigs()) {
            processForwardExtension(forwardConfig, mconfig, null);
        }
        for (ExceptionConfig exceptionConfig : mconfig.findExceptionConfigs()) {
            processExceptionExtension(exceptionConfig, mconfig, null);
        }
        for (ActionConfig actionConfig : mconfig.findActionConfigs()) {
            processActionConfigExtension(actionConfig, mconfig);
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
     * Validate the module's controller configuration against the controller
     * properties this engine implements, failing startup with an actionable
     * error for settings that would be silently skipped.
     */
    protected void validateControllerConfig(ModuleConfig mconfig)
        throws ServletException {
        try {
            ControllerConfigValidator.validate(mconfig);
        } catch (UnsupportedControllerConfigException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------- config inheritance

    /**
     * Extend the form bean's configuration as necessary.
     */
    protected void processFormBeanExtension(FormBeanConfig beanConfig,
        ModuleConfig moduleConfig) throws ServletException {
        try {
            if (!beanConfig.isExtensionProcessed()) {
                log.debug("Processing extensions for '{}'",
                    beanConfig.getName());

                beanConfig =
                    processFormBeanConfigClass(beanConfig, moduleConfig);

                beanConfig.processExtends(moduleConfig);
            }
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            handleGeneralExtensionException("FormBeanConfig",
                beanConfig.getName(), e);
        }
    }

    /**
     * Checks if the current beanConfig is using the correct class based on
     * the class of its ancestor form bean config.
     */
    protected FormBeanConfig processFormBeanConfigClass(
        FormBeanConfig beanConfig, ModuleConfig moduleConfig)
        throws ServletException {
        String ancestor = beanConfig.getExtends();

        if (ancestor == null) {
            // Nothing to do, then
            return beanConfig;
        }

        // Make sure that this bean is of the right class
        FormBeanConfig baseConfig = moduleConfig.findFormBeanConfig(ancestor);

        if (baseConfig == null) {
            throw new UnavailableException("Unable to find " + "form bean '"
                + ancestor + "' to extend.");
        }

        // Was our bean's class overridden already?
        if (beanConfig.getClass().equals(FormBeanConfig.class)) {
            // Ensure that our bean is using the correct class
            if (!baseConfig.getClass().equals(beanConfig.getClass())) {
                // Replace the bean with an instance of the correct class
                FormBeanConfig newBeanConfig = null;
                String baseConfigClassName = baseConfig.getClass().getName();

                try {
                    newBeanConfig =
                        (FormBeanConfig) RequestUtils.applicationInstance(baseConfigClassName);

                    // copy the values
                    BeanUtils.copyProperties(newBeanConfig, beanConfig);

                    FormPropertyConfig[] fpc =
                        beanConfig.findFormPropertyConfigs();

                    for (FormPropertyConfig element : fpc) {
                        newBeanConfig.addFormPropertyConfig(element);
                    }
                } catch (Exception e) {
                    handleCreationException(baseConfigClassName, e);
                }

                // replace beanConfig with newBeanConfig
                moduleConfig.removeFormBeanConfig(beanConfig);
                moduleConfig.addFormBeanConfig(newBeanConfig);
                beanConfig = newBeanConfig;
            }
        }

        return beanConfig;
    }

    /**
     * Extend the forward's configuration as necessary. If actionConfig is
     * provided, the forwardConfig is processed as part of that actionConfig;
     * otherwise it is processed as a global forward.
     */
    protected void processForwardExtension(ForwardConfig forwardConfig,
        ModuleConfig moduleConfig, ActionConfig actionConfig)
        throws ServletException {
        try {
            if (!forwardConfig.isExtensionProcessed()) {
                log.debug("Processing extensions for '{}'",
                    forwardConfig.getName());

                forwardConfig =
                    processForwardConfigClass(forwardConfig, moduleConfig,
                        actionConfig);

                forwardConfig.processExtends(moduleConfig, actionConfig);
            }
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            handleGeneralExtensionException("Forward",
                forwardConfig.getName(), e);
        }
    }

    /**
     * Checks if the current forwardConfig is using the correct class based
     * on the class of its configuration ancestor.
     */
    protected ForwardConfig processForwardConfigClass(
        ForwardConfig forwardConfig, ModuleConfig moduleConfig,
        ActionConfig actionConfig) throws ServletException {
        String ancestor = forwardConfig.getExtends();

        if (ancestor == null) {
            // Nothing to do, then
            return forwardConfig;
        }

        // Make sure that this config is of the right class
        ForwardConfig baseConfig = null;
        if (actionConfig != null) {
            // Look for this in the actionConfig
            baseConfig = actionConfig.findForwardConfig(ancestor);
        }

        if (baseConfig == null) {
            // Either this is a forwardConfig that inherits a global config,
            //  or actionConfig is null
            baseConfig = moduleConfig.findForwardConfig(ancestor);
        }

        if (baseConfig == null) {
            throw new UnavailableException("Unable to find " + "forward '"
                + ancestor + "' to extend.");
        }

        // Was our forwards's class overridden already?
        if (forwardConfig.getClass().equals(ActionForward.class)) {
            // Ensure that our forward is using the correct class
            if (!baseConfig.getClass().equals(forwardConfig.getClass())) {
                // Replace the config with an instance of the correct class
                ForwardConfig newForwardConfig = null;
                String baseConfigClassName = baseConfig.getClass().getName();

                try {
                    newForwardConfig =
                        (ForwardConfig) RequestUtils.applicationInstance(
                                baseConfigClassName);

                    // copy the values
                    BeanUtils.copyProperties(newForwardConfig, forwardConfig);
                } catch (Exception e) {
                    handleCreationException(baseConfigClassName, e);
                }

                // replace forwardConfig with newForwardConfig
                if (actionConfig != null) {
                    actionConfig.removeForwardConfig(forwardConfig);
                    actionConfig.addForwardConfig(newForwardConfig);
                } else {
                    moduleConfig.removeForwardConfig(forwardConfig);
                    moduleConfig.addForwardConfig(newForwardConfig);
                }
                forwardConfig = newForwardConfig;
            }
        }

        return forwardConfig;
    }

    /**
     * Extend the exception's configuration as necessary. If actionConfig is
     * provided, the exceptionConfig is processed as part of that actionConfig;
     * otherwise it is processed as a global exception handler.
     */
    protected void processExceptionExtension(ExceptionConfig exceptionConfig,
        ModuleConfig moduleConfig, ActionConfig actionConfig)
        throws ServletException {
        try {
            if (!exceptionConfig.isExtensionProcessed()) {
                log.debug("Processing extensions for '{}'",
                    exceptionConfig.getType());

                exceptionConfig =
                    processExceptionConfigClass(exceptionConfig, moduleConfig,
                        actionConfig);

                exceptionConfig.processExtends(moduleConfig, actionConfig);
            }
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            handleGeneralExtensionException("Exception",
                exceptionConfig.getType(), e);
        }
    }

    /**
     * Checks if the current exceptionConfig is using the correct class based
     * on the class of its configuration ancestor.
     */
    protected ExceptionConfig processExceptionConfigClass(
        ExceptionConfig exceptionConfig, ModuleConfig moduleConfig,
        ActionConfig actionConfig) throws ServletException {
        String ancestor = exceptionConfig.getExtends();

        if (ancestor == null) {
            // Nothing to do, then
            return exceptionConfig;
        }

        // Make sure that this config is of the right class
        ExceptionConfig baseConfig = null;
        if (actionConfig != null) {
            baseConfig = actionConfig.findExceptionConfig(ancestor);
        }

        if (baseConfig == null) {
            // This means either there's no actionConfig anyway, or the
            // ancestor is not defined within the action.
            baseConfig = moduleConfig.findExceptionConfig(ancestor);
        }

        if (baseConfig == null) {
            throw new UnavailableException("Unable to find "
                + "exception config '" + ancestor + "' to extend.");
        }

        // Was our config's class overridden already?
        if (exceptionConfig.getClass().equals(ExceptionConfig.class)) {
            // Ensure that our config is using the correct class
            if (!baseConfig.getClass().equals(exceptionConfig.getClass())) {
                // Replace the config with an instance of the correct class
                ExceptionConfig newExceptionConfig = null;
                String baseConfigClassName = baseConfig.getClass().getName();

                try {
                    newExceptionConfig =
                        (ExceptionConfig) RequestUtils.applicationInstance(
                            baseConfigClassName);

                    // copy the values
                    BeanUtils.copyProperties(newExceptionConfig,
                        exceptionConfig);
                } catch (Exception e) {
                    handleCreationException(baseConfigClassName, e);
                }

                // replace exceptionConfig with newExceptionConfig
                if (actionConfig != null) {
                    actionConfig.removeExceptionConfig(exceptionConfig);
                    actionConfig.addExceptionConfig(newExceptionConfig);
                } else {
                    moduleConfig.removeExceptionConfig(exceptionConfig);
                    moduleConfig.addExceptionConfig(newExceptionConfig);
                }
                exceptionConfig = newExceptionConfig;
            }
        }

        return exceptionConfig;
    }

    /**
     * Extend the action's configuration as necessary, then process any
     * extensions declared by its local forwards and exception handlers.
     */
    protected void processActionConfigExtension(ActionConfig actionConfig,
        ModuleConfig moduleConfig) throws ServletException {
        try {
            if (!actionConfig.isExtensionProcessed()) {
                log.debug("Processing extensions for '{}'",
                    actionConfig.getPath());

                actionConfig =
                    processActionConfigClass(actionConfig, moduleConfig);

                actionConfig.processExtends(moduleConfig);
            }

            // Process forwards extensions.
            ForwardConfig[] forwards = actionConfig.findForwardConfigs();
            for (ForwardConfig forward : forwards) {
                processForwardExtension(forward, moduleConfig, actionConfig);
            }

            // Process exception extensions.
            ExceptionConfig[] exceptions = actionConfig.findExceptionConfigs();
            for (ExceptionConfig exception : exceptions) {
                processExceptionExtension(exception, moduleConfig,
                    actionConfig);
            }
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            handleGeneralExtensionException("Action", actionConfig.getPath(),
                e);
        }
    }

    /**
     * Checks if the current actionConfig is using the correct class based
     * on the class of its ancestor ActionConfig.
     */
    protected ActionConfig processActionConfigClass(ActionConfig actionConfig,
        ModuleConfig moduleConfig) throws ServletException {
        String ancestor = actionConfig.getExtends();

        if (ancestor == null) {
            // Nothing to do, then
            return actionConfig;
        }

        // Make sure that this config is of the right class
        ActionConfig baseConfig = moduleConfig.findActionConfig(ancestor);
        if (baseConfig == null) {
            baseConfig = moduleConfig.findActionConfigId(ancestor);
        }

        if (baseConfig == null) {
            throw new UnavailableException("Unable to find "
                + "action config for '" + ancestor + "' to extend.");
        }

        // Was our actionConfig's class overridden already?
        if (actionConfig.getClass().equals(ActionMapping.class)) {
            // Ensure that our config is using the correct class
            if (!baseConfig.getClass().equals(actionConfig.getClass())) {
                // Replace the config with an instance of the correct class
                ActionConfig newActionConfig = null;
                String baseConfigClassName = baseConfig.getClass().getName();

                try {
                    newActionConfig =
                        (ActionConfig) RequestUtils.applicationInstance(baseConfigClassName);

                    // copy the values
                    BeanUtils.copyProperties(newActionConfig, actionConfig);

                    // copy the forward and exception configs, too
                    ForwardConfig[] forwards =
                        actionConfig.findForwardConfigs();

                    for (ForwardConfig forwardsConfig : forwards) {
                        newActionConfig.addForwardConfig(forwardsConfig);
                    }

                    ExceptionConfig[] exceptions =
                        actionConfig.findExceptionConfigs();

                    for (ExceptionConfig exceptionsConfig : exceptions) {
                        newActionConfig.addExceptionConfig(exceptionsConfig);
                    }
                } catch (Exception e) {
                    handleCreationException(baseConfigClassName, e);
                }

                // replace actionConfig with newActionConfig
                moduleConfig.removeActionConfig(actionConfig);
                moduleConfig.addActionConfig(newActionConfig);
                actionConfig = newActionConfig;
            }
        }

        return actionConfig;
    }

    /**
     * General handling for exceptions caught while inheriting config
     * information.
     */
    private void handleGeneralExtensionException(String configType,
        String configName, Exception e) throws ServletException {
        String errorMessage =
            internal.getMessage("configExtends", configType, configName);

        log.error(errorMessage, e);
        UnavailableException e2 = new UnavailableException(errorMessage);
        e2.initCause(e);
        throw e2;
    }

    /**
     * Handle errors caused by configuration classes that could not be
     * created during extension processing.
     */
    private void handleCreationException(String className, Exception e)
        throws ServletException {
        String errorMessage =
            internal.getMessage("configExtends.creation", className);

        log.error(errorMessage, e);
        UnavailableException e2 = new UnavailableException(errorMessage);
        e2.initCause(e);
        throw e2;
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
                // Pass the current plugIn config object to the PlugIn.
                // The property is set only if the plugin declares it
                // (classic contract, needed by TilesPlugin).
                try {
                    org.apache.commons.beanutils.PropertyUtils.setProperty(
                        plugin, "currentPlugInConfigObject", plugInConfig);
                } catch (Exception e) {
                    // fail silently like the classic ActionServlet
                }
                plugin.init(this, mconfig);
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
