package org.apache.struts.config;

/**
 * A factory interface for creating {@link ModuleConfig} instances.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.ModuleConfigFactory).</p>
 */
public abstract class ModuleConfigFactory {

    /**
     * The fully qualified class name to be used for the factory instance.
     */
    protected static String factoryClass =
        "org.apache.struts.config.impl.DefaultModuleConfigFactory";

    /**
     * Create and return a newly instantiated <code>ModuleConfig</code>.
     *
     * @param prefix The module prefix for the new configuration
     */
    public abstract ModuleConfig createModuleConfig(String prefix);

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
        ModuleConfigFactory.factoryClass = factoryClass;
    }

    /**
     * Create and return a newly instantiated <code>ModuleConfigFactory</code>.
     */
    public static ModuleConfigFactory createFactory() {
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader()
                != null
                    ? Thread.currentThread().getContextClassLoader()
                        .loadClass(factoryClass)
                    : Class.forName(factoryClass);
            return (ModuleConfigFactory) clazz.getDeclaredConstructor()
                .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "Cannot instantiate ModuleConfigFactory " + factoryClass, e);
        }
    }
}
