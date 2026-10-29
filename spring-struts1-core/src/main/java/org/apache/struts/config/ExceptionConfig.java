package org.apache.struts.config;

/**
 * A JavaBean representing the configuration information of an
 * <code>&lt;exception&gt;</code> element from a Struts configuration file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.ExceptionConfig).</p>
 */
public class ExceptionConfig extends BaseConfig {

    private static final long serialVersionUID = 8977916943183890264L;

    /** The servlet context attribute key for the MessageResources instance. */
    protected String bundle;

    /** The fully qualified Java class name of the exception handler class. */
    protected String handler = "org.apache.struts.action.ExceptionHandler";

    /** The message resources key specifying the error message. */
    protected String key;

    /** The module-relative path of the resource for error handling. */
    protected String path;

    /** The scope ("request" or "session") in which the error is stored. */
    protected String scope = "request";

    /** The fully qualified Java class name of the exception. */
    protected String type;

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ExceptionConfig[type=" + type + ",key=" + key + ",path="
            + path + ",scope=" + scope + ",handler=" + handler + "]";
    }

    /**
     * The type of the exception config that this object should inherit
     * properties from (classic <code>extends</code> attribute).
     */
    protected String inherit;

    /** Whether the inheritance of this config has been processed. */
    protected boolean extensionProcessed = false;

    public String getExtends() {
        return inherit;
    }

    public void setExtends(String inherit) {
        this.inherit = inherit;
    }

    public boolean isExtensionProcessed() {
        return extensionProcessed;
    }

    /**
     * Inherit configuration information from the ExceptionConfig that this
     * instance is extending.
     */
    public void processExtends(ModuleConfig moduleConfig,
        ActionConfig actionConfig)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, java.lang.reflect.InvocationTargetException {
        String ancestor = getExtends();

        if ((!extensionProcessed) && (ancestor != null)) {
            ExceptionConfig baseConfig = null;

            if (actionConfig != null) {
                baseConfig = actionConfig.findExceptionConfig(ancestor);
            }
            if ((baseConfig == null) && (moduleConfig != null)) {
                baseConfig = moduleConfig.findExceptionConfig(ancestor);
            }
            if (baseConfig == null) {
                throw new NullPointerException("Unable to find "
                    + "exception handler '" + ancestor + "' to extend.");
            }

            if (checkCircularInheritance(moduleConfig, actionConfig)) {
                throw new IllegalArgumentException(
                    "Circular inheritance detected for exception " + getType());
            }

            if (!baseConfig.isExtensionProcessed()) {
                baseConfig.processExtends(moduleConfig, actionConfig);
            }

            inheritFrom(baseConfig);
        }

        extensionProcessed = true;
    }

    /**
     * Traces the hierarchy of this object to check if any of the ancestors
     * is extending this instance.
     */
    protected boolean checkCircularInheritance(ModuleConfig moduleConfig,
        ActionConfig actionConfig) {
        String ancestorName = getExtends();

        while (ancestorName != null) {
            if (getType().equals(ancestorName)) {
                return true;
            }
            ExceptionConfig ancestor = null;
            if (actionConfig != null) {
                ancestor = actionConfig.findExceptionConfig(ancestorName);
            }
            if ((ancestor == null) && (moduleConfig != null)) {
                ancestor = moduleConfig.findExceptionConfig(ancestorName);
            }
            if (ancestor == null) {
                ancestorName = null;
            } else {
                ancestorName = ancestor.getExtends();
            }
        }

        return false;
    }

    /**
     * Copy the not-overridden values of the given base config into this one.
     */
    public void inheritFrom(ExceptionConfig config) {
        if (getBundle() == null) {
            setBundle(config.getBundle());
        }
        if (getHandler() == null) {
            setHandler(config.getHandler());
        }
        if (getKey() == null) {
            setKey(config.getKey());
        }
        if (getPath() == null) {
            setPath(config.getPath());
        }
        setScope(config.getScope());
        setType(config.getType());

        inheritProperties(config);
    }
}
