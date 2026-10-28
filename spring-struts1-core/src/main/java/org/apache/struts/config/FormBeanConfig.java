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
    protected boolean configured = false;

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
     * the information in this <code>FormBeanConfig</code>.
     */
    public ActionForm createActionForm() {
        try {
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
