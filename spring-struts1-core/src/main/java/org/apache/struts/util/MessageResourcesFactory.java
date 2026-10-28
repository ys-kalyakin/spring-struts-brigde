package org.apache.struts.util;

import java.io.Serializable;

import org.apache.struts.config.MessageResourcesConfig;

/**
 * Factory for <code>MessageResources</code> instances.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.util.MessageResourcesFactory).</p>
 */
public abstract class MessageResourcesFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The fully qualified class name to be used for the factory instance.
     */
    protected static String factoryClass =
        "org.apache.struts.util.PropertyMessageResourcesFactory";

    /**
     * The configuration bean for this factory instance.
     */
    protected MessageResourcesConfig config;

    /**
     * The "return null" property value to be applied to created instances.
     */
    protected boolean returnNull = true;

    public MessageResourcesConfig getConfig() {
        return config;
    }

    public void setConfig(MessageResourcesConfig config) {
        this.config = config;
    }

    public boolean getReturnNull() {
        return returnNull;
    }

    public void setReturnNull(boolean returnNull) {
        this.returnNull = returnNull;
    }

    /**
     * Create and return a newly instantiated <code>MessageResources</code>
     * instance for the specified configuration parameter.
     */
    public abstract MessageResources createResources(String config);

    /**
     * Return the fully qualified class name of the factory instance.
     */
    public static String getFactoryClass() {
        return factoryClass;
    }

    /**
     * Set the fully qualified class name of the factory instance.
     */
    public static void setFactoryClass(String factoryClass) {
        MessageResourcesFactory.factoryClass = factoryClass;
    }

    /**
     * Create and return a newly instantiated <code>MessageResourcesFactory</code>.
     */
    public static MessageResourcesFactory createFactory() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> clazz = (cl != null) ? cl.loadClass(factoryClass)
                : Class.forName(factoryClass);
            return (MessageResourcesFactory) clazz.getDeclaredConstructor()
                .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "Cannot instantiate MessageResourcesFactory " + factoryClass,
                e);
        }
    }
}
