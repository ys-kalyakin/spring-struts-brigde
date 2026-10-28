package org.apache.struts.config;

/**
 * A JavaBean representing the configuration information of a
 * <code>&lt;form-property&gt;</code> element from a Struts configuration file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.FormPropertyConfig).</p>
 */
public class FormPropertyConfig extends BaseConfig {

    private static final long serialVersionUID = 4313137096646949723L;

    /** The property name of this JavaBean. */
    protected String name;

    /** The fully qualified Java class name expected for this property. */
    protected String type;

    /** The initial value of this property, if configured. */
    protected String initial;

    /** The size of the array to be created if this property is an array type. */
    protected int size = 0;

    /** The role or roles allowed to access this property. */
    protected String role;

    /** Conditions under which the property should be reset to its initial value. */
    protected String reset;

    public FormPropertyConfig() {
    }

    public FormPropertyConfig(String name, String type, String initial) {
        setName(name);
        setType(type);
        setInitial(initial);
    }

    public FormPropertyConfig(String name, String type, String initial,
        int size) {
        this(name, type, initial);
        setSize(size);
    }

    public FormPropertyConfig(String name, String type, String initial,
        String role) {
        this(name, type, initial);
        setRole(role);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getReset() {
        return reset;
    }

    public void setReset(String reset) {
        this.reset = reset;
    }

    /**
     * Return the initial value of this property as an instance of the
     * configured type: the string <code>initial</code> converted to the
     * type, the default value of the type when no initial is configured, or
     * an array of the configured size for array types.
     */
    public Object initial() {
        Object initialValue = null;

        try {
            Class<?> clazz = loadTypeClass();

            if (clazz.isArray()) {
                if (initial != null) {
                    initialValue =
                        org.apache.commons.beanutils.ConvertUtils.convert(
                            initial, clazz);
                } else {
                    initialValue =
                        java.lang.reflect.Array.newInstance(
                            clazz.getComponentType(), size);
                }
            } else {
                if (initial != null) {
                    initialValue =
                        org.apache.commons.beanutils.ConvertUtils.convert(
                            initial, clazz);
                } else {
                    initialValue = clazz.getDeclaredConstructor().newInstance();
                }
            }
        } catch (Throwable t) {
            initialValue = null;
        }

        return initialValue;
    }

    /**
     * Return the configured property type as a class.
     */
    public Class<?> getTypeClass() throws ClassNotFoundException {
        return loadTypeClass();
    }

    private Class<?> loadTypeClass() throws ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return (cl != null) ? cl.loadClass(type) : Class.forName(type);
    }

    @Override
    public String toString() {
        return "FormPropertyConfig[name=" + name + ",type=" + type
            + ",initial=" + initial + "]";
    }
}
