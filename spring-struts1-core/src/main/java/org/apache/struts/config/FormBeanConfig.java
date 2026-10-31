package org.apache.struts.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.struts.action.ActionForm;

/**
 * A JavaBean representing the configuration information of a
 * <code>&lt;form-bean&gt;</code> element from a Struts configuration file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.FormBeanConfig);
 * dynamic forms are out of scope for phase 1.</p>
 */
public class FormBeanConfig extends BaseConfig {

    private static final long serialVersionUID = -8943345424278622801L;

    /** The unique identifier of this form bean. */
    protected String name;

    /** The fully qualified Java class name of the implementation class. */
    protected String type;

    /** Has this component been completely configured? */
    protected volatile boolean configured = false;

    /** The set of form properties for this form bean, keyed by property name. */
    protected Map<String, FormPropertyConfig> formProperties =
        new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        throwIfConfigured();
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        throwIfConfigured();
        this.type = type;
    }

    public void freeze() {
        configured = true;
    }

    /**
     * The path or action id of the form bean that this object should
     * inherit properties from (classic <code>extends</code> attribute).
     */
    protected String inherit;

    /** Whether the inheritance of this config has been processed. */
    protected volatile boolean extensionProcessed = false;

    public String getExtends() {
        return inherit;
    }

    public void setExtends(String inherit) {
        throwIfConfigured();
        this.inherit = inherit;
    }

    public boolean isExtensionProcessed() {
        return extensionProcessed;
    }

    /**
     * Inherit configuration information from the FormBeanConfig that this
     * instance is extending (classic multi-page config inheritance).
     */
    public void processExtends(ModuleConfig moduleConfig)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, java.lang.reflect.InvocationTargetException {
        throwIfConfigured();

        String ancestor = getExtends();

        if ((!extensionProcessed) && (ancestor != null)) {
            FormBeanConfig baseConfig = moduleConfig.findFormBeanConfig(ancestor);

            if (baseConfig == null) {
                throw new NullPointerException("Unable to find "
                    + "form bean '" + ancestor + "' to extend.");
            }

            if (checkCircularInheritance(moduleConfig)) {
                throw new IllegalArgumentException(
                    "Circular inheritance detected for form bean " + getName());
            }

            // Make sure the ancestor's own extension has been processed.
            if (!baseConfig.isExtensionProcessed()) {
                baseConfig.processExtends(moduleConfig);
            }

            inheritFrom(baseConfig);
        }

        extensionProcessed = true;
    }

    /**
     * Traces the hierarchy of this object to check if any of the ancestors
     * is extending this instance.
     */
    protected boolean checkCircularInheritance(ModuleConfig moduleConfig) {
        String ancestorName = getExtends();

        while (ancestorName != null) {
            if (getName().equals(ancestorName)) {
                return true;
            }
            FormBeanConfig ancestor =
                moduleConfig.findFormBeanConfig(ancestorName);
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
    public void inheritFrom(FormBeanConfig config)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, java.lang.reflect.InvocationTargetException {
        throwIfConfigured();

        if (getType() == null) {
            setType(config.getType());
        }

        inheritFormProperties(config);
        inheritProperties(config);
    }

    /**
     * Compare the form properties of this bean with that of the given and
     * copy those that are not present.
     */
    protected void inheritFormProperties(FormBeanConfig config) {
        throwIfConfigured();

        FormPropertyConfig[] baseFpcs = config.findFormPropertyConfigs();

        for (FormPropertyConfig baseFpc : baseFpcs) {
            FormPropertyConfig prop =
                this.findFormPropertyConfig(baseFpc.getName());

            if (prop == null) {
                prop = new FormPropertyConfig();
                try {
                    org.apache.commons.beanutils.BeanUtils.copyProperties(
                        prop, baseFpc);
                } catch (IllegalAccessException
                        | java.lang.reflect.InvocationTargetException ex) {
                    throw new IllegalStateException(ex);
                }
                this.addFormPropertyConfig(prop);
                prop.setProperties(baseFpc.copyProperties());
            }
        }
    }

    /**
     * Throw <code>IllegalStateException</code> if configuration is frozen.
     */
    protected void throwIfConfigured() {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
    }

    /**
     * Add a new <code>FormPropertyConfig</code> instance to the set
     * associated with this module.
     */
    public void addFormPropertyConfig(FormPropertyConfig config) {
        throwIfConfigured();
        formProperties.put(config.getName(), config);
    }

    /**
     * Return the form property configuration for the specified property name,
     * if any; otherwise return <code>null</code>.
     */
    public FormPropertyConfig findFormPropertyConfig(String name) {
        return formProperties.get(name);
    }

    /**
     * Return the form property configurations for this module. If there are
     * none, a zero-length array is returned.
     */
    public FormPropertyConfig[] findFormPropertyConfigs() {
        List<FormPropertyConfig> results =
            new ArrayList<>(formProperties.values());
        return results.toArray(new FormPropertyConfig[0]);
    }

    /**
     * Create and return an <code>ActionForm</code> instance appropriate to
     * the information in this <code>FormBeanConfig</code>: dynamic forms
     * (DynaActionForm and subclasses) are instantiated through their
     * <code>DynaActionFormClass</code> and initialized from the declared
     * form properties, other types are instantiated directly.
     */
    public ActionForm createActionForm() {
        try {
            if (isDynamic()) {
                org.apache.struts.action.DynaActionForm instance =
                    (org.apache.struts.action.DynaActionForm)
                        getDynaActionFormClass().newInstance();
                instance.initialize(this);
                return instance;
            }
            Class<?> clazz = formBeanClass();
            Object instance = clazz.getDeclaredConstructor().newInstance();
            if (!(instance instanceof ActionForm)) {
                throw new IllegalStateException("Type [" + type
                    + "] is not an ActionForm");
            }
            return (ActionForm) instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "Cannot create ActionForm of type " + type, e);
        }
    }

    /**
     * Return true when the configured type is <code>DynaActionForm</code>
     * or a subclass of it.
     */
    public boolean isDynamic() {
        try {
            return org.apache.struts.action.DynaActionForm.class
                .isAssignableFrom(formBeanClass());
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Return the <code>DynaActionFormClass</code> for this form bean
     * configuration, creating and caching it on first use.
     */
    public org.apache.struts.action.DynaActionFormClass
        getDynaActionFormClass() {
        if (dynaActionFormClass == null) {
            synchronized (this) {
                if (dynaActionFormClass == null) {
                    dynaActionFormClass =
                        new org.apache.struts.action.DynaActionFormClass(this);
                }
            }
        }
        return dynaActionFormClass;
    }

    /** The cached DynaActionFormClass for dynamic forms. */
    private transient volatile org.apache.struts.action.DynaActionFormClass
        dynaActionFormClass;

    /**
     * Return the <code>Class</code> instance for the form bean implementation
     * configured by this <code>FormBeanConfig</code>.
     */
    protected Class<?> formBeanClass() {
        try {
            return Thread.currentThread().getContextClassLoader() != null
                ? Thread.currentThread().getContextClassLoader().loadClass(type)
                : Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find form bean class "
                + type, e);
        }
    }

    @Override
    public String toString() {
        return "FormBeanConfig[name=" + name + ",type=" + type
            + ",properties=" + formProperties.size() + "]";
    }
}
