package org.apache.struts.config;

/**
 * A JavaBean representing the configuration information of a
 * <code>&lt;forward&gt;</code> element from a Struts configuration file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.ForwardConfig).</p>
 */
public class ForwardConfig extends BaseConfig {

    private static final long serialVersionUID = 7621670814492368589L;

    /** The unique identifier of this forward. */
    protected String name;

    /** The URL to which this forward will be transferred. */
    protected String path;

    /** The module-relative path (if any) of an alternate module. */
    protected String module;

    /** Should this forward trigger an HTTP redirect? */
    protected boolean redirect;

    public ForwardConfig() {
    }

    public ForwardConfig(String name, String path, boolean redirect) {
        setName(name);
        setPath(path);
        setRedirect(redirect);
    }

    public ForwardConfig(String name, String path, boolean redirect,
        String module) {
        this(name, path, redirect);
        setModule(module);
    }

    public ForwardConfig(ForwardConfig copyMe) {
        setName(copyMe.getName());
        setPath(copyMe.getPath());
        setRedirect(copyMe.getRedirect());
        setModule(copyMe.getModule());
        BaseConfig.copyProperties(copyMe, this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean getRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    @Override
    public String toString() {
        return "ForwardConfig[name=" + name + ",path=" + path
            + ",redirect=" + redirect + ",module=" + module + "]";
    }

    /**
     * The path or action id of the forward that this object should inherit
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
     * Inherit configuration information from the ForwardConfig that this
     * instance is extending.
     *
     * @param moduleConfig The module this bean is from
     * @param actionConfig The action this bean belongs to, if any
     */
    public void processExtends(ModuleConfig moduleConfig,
        ActionConfig actionConfig)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, java.lang.reflect.InvocationTargetException {
        String ancestor = getExtends();

        if ((!extensionProcessed) && (ancestor != null)) {
            ForwardConfig baseConfig = null;

            if (actionConfig != null) {
                baseConfig = actionConfig.findForwardConfig(ancestor);
            }
            if ((baseConfig == null) && (moduleConfig != null)) {
                baseConfig = moduleConfig.findForwardConfig(ancestor);
            }
            if (baseConfig == null) {
                throw new NullPointerException("Unable to find forward '"
                    + ancestor + "' to extend.");
            }

            if (checkCircularInheritance(moduleConfig, actionConfig)) {
                throw new IllegalArgumentException(
                    "Circular inheritance detected for forward " + getName());
            }

            // Make sure the ancestor's own extension has been processed.
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
            if (getName().equals(ancestorName)) {
                return true;
            }
            ForwardConfig ancestor = null;
            if (actionConfig != null) {
                ancestor = actionConfig.findForwardConfig(ancestorName);
            }
            if ((ancestor == null) && (moduleConfig != null)) {
                ancestor = moduleConfig.findForwardConfig(ancestorName);
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
    public void inheritFrom(ForwardConfig config) {
        if (getModule() == null) {
            setModule(config.getModule());
        }
        if (getName() == null) {
            setName(config.getName());
        }
        if (getPath() == null) {
            setPath(config.getPath());
        }
        setRedirect(config.getRedirect());

        inheritProperties(config);
    }
}
