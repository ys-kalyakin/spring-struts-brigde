package org.apache.struts.config;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.action.Action;
import org.apache.struts.util.RequestUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A JavaBean representing the configuration information of an
 * <code>&lt;action&gt;</code> element from a Struts configuration file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.ActionConfig);
 * chain-specific and 1.4-only fields (command/catalog, populate/reset events,
 * acceptPage, dispatcher) are out of scope for phase 1.</p>
 */
public class ActionConfig extends BaseConfig {

    private static final long serialVersionUID = -7122045788425377964L;

    /** The module configuration with which we are associated. */
    protected ModuleConfig moduleConfig;

    /** The request scope or session scope attribute name under which our form bean is accessed. */
    protected String attribute;

    /** Boolean flag determining whether "cancel" is allowed. */
    protected boolean cancellable;

    /** Has this component been completely configured? */
    protected boolean configured;

    /** The internal action identifier for this action mapping. */
    protected String actionId;

    /** Module-relative path of the action or resource to be forwarded to. */
    protected String forward;

    /** Module-relative path of the action or resource to be included. */
    protected String include;

    /** Module-relative path of the input form for this mapping. */
    protected String input;

    /** The fully qualified Java class name of the MultipartRequestHandler class. */
    protected String multipartClass;

    /** The name of the form bean (if any) associated with this action. */
    protected String name;

    /** General purpose configuration parameter. */
    protected String parameter;

    /** The context-relative URI of the action. */
    protected String path;

    /** The module prefix (if any) of this action's module. */
    protected String prefix;

    /** Comma-delimited list of security roles allowed to service this request. */
    protected String roles;

    /** The security role names (parsed from roles). */
    protected String[] roleNames = new String[0];

    /** The scope ("request" or "session") under which the associated form bean is accessed. */
    protected String scope = "session";

    /** The URL suffix (if any) matched by this mapping. */
    protected String suffix;

    /** The fully qualified Java class name of the Action class. */
    protected String type;

    /** Should this action be the default for the module? */
    protected boolean unknown;

    /** Should the validate() method of the form bean associated with this action be called? */
    protected boolean validate = true;

    /**
     * Accepted page value for multi-page validation. If multi-page
     * validation is not used, acceptPage is not set; multi-page validation
     * is then disabled (all field pages are validated).
     */
    protected Integer acceptPage = null;

    /** The set of exception handling configurations for this action, keyed by fully qualified class name. */
    protected Map<String, ExceptionConfig> exceptions = new LinkedHashMap<>();

    /** The set of local forward configurations for this action, keyed by name. */
    protected Map<String, ForwardConfig> forwards = new LinkedHashMap<>();

    /**
     * The path or actionId of the action mapping this config inherits from,
     * or <code>null</code> if there is no inheritance.
     */
    protected String inherit = null;

    /** Indicates whether the cancellable property was explicitly set. */
    private boolean cancellableSet = false;

    /** Indicates whether this config has had its extension processed. */
    protected boolean extensionProcessed = false;

    /** Indicates whether the validate property was explicitly set. */
    private boolean validateSet = false;

    public ModuleConfig getModuleConfig() {
        return moduleConfig;
    }

    public void setModuleConfig(ModuleConfig moduleConfig) {
        this.moduleConfig = moduleConfig;
    }

    public String getAttribute() {
        if (this.attribute == null) {
            return (this.name);
        } else {
            return (this.attribute);
        }
    }

    public void setAttribute(String attribute) {
        throwIfConfigured();
        this.attribute = attribute;
    }

    public boolean getCancellable() {
        return cancellable;
    }

    public void setCancellable(boolean cancellable) {
        throwIfConfigured();
        this.cancellable = cancellable;
        this.cancellableSet = true;
    }

    /**
     * Return the path or actionId of the ActionConfig this config
     * inherits properties from.
     */
    public String getExtends() {
        return this.inherit;
    }

    /**
     * Set the path or actionId of the ActionConfig this config
     * inherits properties from.
     */
    public void setExtends(String inherit) {
        throwIfConfigured();
        this.inherit = inherit;
    }

    /**
     * Indicates whether this config has had its extension processed.
     */
    public boolean isExtensionProcessed() {
        return extensionProcessed;
    }

    public String getForward() {
        return forward;
    }

    public void setForward(String forward) {
        throwIfConfigured();
        this.forward = forward;
    }

    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        throwIfConfigured();
        this.include = include;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        throwIfConfigured();
        this.input = input;
    }

    public String getMultipartClass() {
        return multipartClass;
    }

    public void setMultipartClass(String multipartClass) {
        throwIfConfigured();
        this.multipartClass = multipartClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        throwIfConfigured();
        this.name = name;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        throwIfConfigured();
        this.parameter = parameter;
    }

    /**
     * Returns the internal action identifier, if any (the Struts 1.3.6+.
     * <code>actionId</code> attribute used to alias actions in URLs).
     */
    public String getActionId() {
        return this.actionId;
    }

    /**
     * Set the internal action identifier of this action mapping. The
     * identifier may not contain a forward slash.
     */
    public void setActionId(String actionId) {
        throwIfConfigured();

        if ((actionId != null) && (actionId.indexOf("/") > -1)) {
            throw new IllegalArgumentException("actionId '" + actionId
                + "' may not contain a forward slash");
        }

        this.actionId = actionId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        throwIfConfigured();
        this.path = path;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        throwIfConfigured();
        this.prefix = prefix;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        throwIfConfigured();
        this.roles = roles;
        this.roleNames = (roles == null) ? new String[0]
            : roles.replace(" ", "").split(",");
    }

    public String[] getRoleNames() {
        return roleNames;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        throwIfConfigured();
        this.scope = scope;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        throwIfConfigured();
        this.suffix = suffix;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        throwIfConfigured();
        this.type = type;
    }

    public boolean getUnknown() {
        return unknown;
    }

    public void setUnknown(boolean unknown) {
        throwIfConfigured();
        this.unknown = unknown;
    }

    public boolean getValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        throwIfConfigured();
        this.validate = validate;
        this.validateSet = true;
    }

    /**
     * Return the accepted page value for multi-page validation.
     */
    public Integer getAcceptPage() {
        return acceptPage;
    }

    /**
     * Set the accepted page value for multi-page validation.
     */
    public void setAcceptPage(Integer acceptPage) {
        throwIfConfigured();
        this.acceptPage = acceptPage;
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Creates a config instance of the given class name. The ported
     * {@code RequestUtils.applicationInstance} keeps a wider classic throws
     * clause; the two exceptions a plain {@code newInstance()} cannot throw
     * are wrapped here so the inheritance methods keep their signatures.
     */
    private static Object newConfigInstance(String className)
        throws ClassNotFoundException, InstantiationException,
        IllegalAccessException {
        try {
            return RequestUtils.applicationInstance(className);
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Copies bean properties between config objects, wrapping the checked
     * reflection exceptions so the inheritance methods keep their classic
     * signatures where the value copy itself is incidental.
     */
    private static void copyConfigProperties(Object dest, Object orig) {
        try {
            BeanUtils.copyProperties(dest, orig);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Traces the hierarchy of this object to check if any of the ancestors
     * is extending this instance.
     */
    protected boolean checkCircularInheritance(ModuleConfig moduleConfig) {
        String ancestor = getExtends();

        while (ancestor != null) {
            // check if we have the same path or id as an ancestor
            if (getPath().equals(ancestor) || ancestor.equals(getActionId())) {
                return true;
            }

            // get our ancestor's config
            ActionConfig baseConfig = moduleConfig.findActionConfig(ancestor);
            if (baseConfig == null) {
                baseConfig = moduleConfig.findActionConfigId(ancestor);
            }

            if (baseConfig != null) {
                ancestor = baseConfig.getExtends();
            } else {
                ancestor = null;
            }
        }

        return false;
    }

    /**
     * Compare the exception handlers of this action with that of the given
     * and copy those that are not present.
     */
    protected void inheritExceptionHandlers(ActionConfig baseConfig)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }

        // Inherit exception handler configs
        ExceptionConfig[] baseHandlers = baseConfig.findExceptionConfigs();

        for (ExceptionConfig baseHandler : baseHandlers) {
            // Do we have this handler?
            ExceptionConfig copy =
                this.findExceptionConfig(baseHandler.getType());

            if (copy == null) {
                // We don't have this, so let's copy it
                copy =
                    (ExceptionConfig) newConfigInstance(baseHandler.getClass()
                                                              .getName());

                copyConfigProperties(copy, baseHandler);
                this.addExceptionConfig(copy);
                copy.setProperties(baseHandler.copyProperties());
            } else {
                // process any extension that this config might have
                copy.processExtends(getModuleConfig(), this);
            }
        }
    }


    /**
     * Compare the forwards of this action with that of the given and copy
     * those that are not present.
     */
    protected void inheritForwards(ActionConfig baseConfig)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }

        // Inherit forward configs
        ForwardConfig[] baseForwards = baseConfig.findForwardConfigs();

        for (ForwardConfig baseForward : baseForwards) {
            // Do we have this forward?
            ForwardConfig copy = this.findForwardConfig(baseForward.getName());

            if (copy == null) {
                // We don't have this, so let's copy it
                copy =
                    (ForwardConfig) newConfigInstance(baseForward.getClass()
                                                            .getName());
                copyConfigProperties(copy, baseForward);

                this.addForwardConfig(copy);
                copy.setProperties(baseForward.copyProperties());
            } else {
                // process any extension for this forward
                copy.processExtends(getModuleConfig(), this);
            }
        }
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Inherit values that have not been overridden from the provided config
     * object. If the given config is extending another object, those
     * extensions should be resolved before it's used as a parameter to this
     * method.
     */
    public void inheritFrom(ActionConfig config)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }

        // Inherit values that have not been overridden
        if (getAttribute() == null) {
            setAttribute(config.getAttribute());
        }

        if (!cancellableSet) {
            setCancellable(config.getCancellable());
        }

        if (getForward() == null) {
            setForward(config.getForward());
        }

        if (getInclude() == null) {
            setInclude(config.getInclude());
        }

        if (getInput() == null) {
            setInput(config.getInput());
        }

        if (getMultipartClass() == null) {
            setMultipartClass(config.getMultipartClass());
        }

        if (getName() == null) {
            setName(config.getName());
        }

        if (getParameter() == null) {
            setParameter(config.getParameter());
        }

        if (getPath() == null) {
            setPath(config.getPath());
        }

        if (getPrefix() == null) {
            setPrefix(config.getPrefix());
        }

        if (getRoles() == null) {
            setRoles(config.getRoles());
        }

        if (getScope().equals("session")) {
            setScope(config.getScope());
        }

        if (getSuffix() == null) {
            setSuffix(config.getSuffix());
        }

        if (getType() == null) {
            setType(config.getType());
        }

        if (!getUnknown()) {
            setUnknown(config.getUnknown());
        }

        if (!validateSet) {
            setValidate(config.getValidate());
        }

        // 2014/07/02 - security problem patch.
        // Author: NTT DATA Corporation
        if (getAcceptPage() == null) {
            setAcceptPage(config.getAcceptPage());
        }

        inheritExceptionHandlers(config);
        inheritForwards(config);
        inheritProperties(config);
    }

    /**
     * Inherit configuration information from the ActionConfig that this
     * instance is extending. This method verifies that any action config
     * object that it inherits from has also had its processExtends() method
     * called.
     */
    public void processExtends(ModuleConfig moduleConfig)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, InvocationTargetException {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }

        String ancestor = getExtends();

        if ((!extensionProcessed) && (ancestor != null)) {
            ActionConfig baseConfig =
                moduleConfig.findActionConfig(ancestor);
            if (baseConfig == null) {
                baseConfig = moduleConfig.findActionConfigId(ancestor);
            }

            if (baseConfig == null) {
                throw new NullPointerException("Unable to find "
                    + "action for '" + ancestor + "' to extend.");
            }

            // Check against circular inheritance and make sure the base
            //  config's own extends has been processed already
            if (checkCircularInheritance(moduleConfig)) {
                throw new IllegalArgumentException(
                    "Circular inheritance detected for action " + getPath());
            }

            // Make sure the ancestor's own extension has been processed.
            if (!baseConfig.isExtensionProcessed()) {
                baseConfig.processExtends(moduleConfig);
            }

            // Copy values from the base config
            inheritFrom(baseConfig);
        }

        extensionProcessed = true;
    }

    public void freeze() {
        configured = true;
    }

    protected void throwIfConfigured() {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
    }

    /**
     * Add a new <code>ExceptionConfig</code> instance to the set associated
     * with this action.
     */
    public void addExceptionConfig(ExceptionConfig config) {
        throwIfConfigured();
        exceptions.put(config.getType(), config);
    }

    /**
     * Return the exception configuration for the specified type, if any;
     * otherwise return <code>null</code>.
     */
    public ExceptionConfig findExceptionConfig(String type) {
        return exceptions.get(type);
    }

    /**
     * Return the exception configurations for this action. If there are none,
     * a zero-length array is returned.
     */
    public ExceptionConfig[] findExceptionConfigs() {
        return exceptions.values().toArray(new ExceptionConfig[0]);
    }

    /**
     * Find and return the <code>ExceptionConfig</code> instance defining how
     * <code>Exceptions</code> of the specified type should be handled. This
     * is performed by checking local and then global configurations for the
     * specified exception's class, and then looking up the superclass chain.
     */
    public ExceptionConfig findException(Class<?> type) {
        while (type != null) {
            String name = type.getName();

            ExceptionConfig config = findExceptionConfig(name);
            if (config != null) {
                return config;
            }

            if (moduleConfig != null) {
                config = moduleConfig.findExceptionConfig(name);
                if (config != null) {
                    return config;
                }
            }

            type = type.getSuperclass();
        }
        return null;
    }

    /**
     * Add a new <code>ForwardConfig</code> instance to the set of local
     * forwards associated with this action.
     */
    public void addForwardConfig(ForwardConfig config) {
        throwIfConfigured();
        forwards.put(config.getName(), config);
    }

    /**
     * Remove the exception configuration instance for the specified type.
     */
    public void removeExceptionConfig(ExceptionConfig config) {
        throwIfConfigured();
        exceptions.remove(config.getType());
    }

    /**
     * Remove the local forward configuration with the specified name.
     */
    public void removeForwardConfig(ForwardConfig config) {
        throwIfConfigured();
        forwards.remove(config.getName());
    }

    /**
     * Return the local forward configuration for the specified key, if any;
     * otherwise return <code>null</code>.
     */
    public ForwardConfig findForwardConfig(String name) {
        return forwards.get(name);
    }

    /**
     * Return all local forward configurations for this action. If there are
     * none, a zero-length array is returned.
     */
    public ForwardConfig[] findForwardConfigs() {
        return forwards.values().toArray(new ForwardConfig[0]);
    }

    /**
     * Create and return a <code>ForwardConfig</code> representing the
     * <code>input</code> of this action. If the controller is configured
     * with <code>inputForward=true</code>, the input names a forward —
     * an action-local forward first, a global forward second, then the
     * classic <code>Action.INPUT</code> fallback; otherwise the input is
     * used as a module-relative path.
     */
    public ForwardConfig getInputForward() {
        if (moduleConfig == null) {
            throw new IllegalStateException("moduleConfig cannot be null");
        }
        String input = getInput();
        if (moduleConfig.getControllerConfig().getInputForward()) {
            if (input != null) {
                ForwardConfig local = findForwardConfig(input);
                if (local != null) {
                    return local;
                }
                ForwardConfig global = moduleConfig.findForwardConfig(input);
                if (global != null) {
                    return global;
                }
                throw new IllegalStateException(
                    "Cannot find forward named " + input);
            }
            ForwardConfig inputFallback = findForwardConfig(Action.INPUT);
            if (inputFallback != null) {
                return inputFallback;
            }
            ForwardConfig globalFallback =
                moduleConfig.findForwardConfig(Action.INPUT);
            if (globalFallback != null) {
                return globalFallback;
            }
            throw new IllegalStateException(
                "Cannot find forward named " + Action.INPUT);
        }
        return new org.apache.struts.action.ActionForward(input);
    }

    @Override
    public ActionConfig clone() {
        try {
            ActionConfig copy = (ActionConfig) super.clone();
            copy.exceptions = new LinkedHashMap<>(exceptions);
            copy.forwards = new LinkedHashMap<>(forwards);
            BaseConfig.copyProperties(this, copy);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return "ActionConfig[path=" + path + ",type=" + type + ",name=" + name
            + ",scope=" + scope + ",validate=" + validate
            + ",forwards=" + forwards.size() + ",exceptions="
            + exceptions.size() + "]";
    }
}
