package org.apache.struts.config;

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

    /** The set of exception handling configurations for this action, keyed by fully qualified class name. */
    protected Map<String, ExceptionConfig> exceptions = new LinkedHashMap<>();

    /** The set of local forward configurations for this action, keyed by name. */
    protected Map<String, ForwardConfig> forwards = new LinkedHashMap<>();

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
     * with <code>inputForward=true</code>, the input names a global forward;
     * otherwise the input is used as a module-relative path.
     */
    public ForwardConfig getInputForward() {
        if (moduleConfig == null) {
            throw new IllegalStateException("moduleConfig cannot be null");
        }
        if (getInput() != null) {
            if (moduleConfig.getControllerConfig().getInputForward()) {
                ForwardConfig fc = moduleConfig.findForwardConfig(getInput());
                if (fc != null) {
                    return fc;
                }
                throw new IllegalStateException(
                    "Cannot find global forward named " + getInput());
            }
            return new ForwardConfig(null, getInput(), false);
        }
        throw new IllegalStateException(
            "ActionConfig has no input to forward to");
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
