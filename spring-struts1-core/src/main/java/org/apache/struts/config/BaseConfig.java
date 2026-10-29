package org.apache.struts.config;

import java.io.Serializable;
import java.util.Properties;

/**
 * A abstract base class for configuration beans which can have arbitrary
 * properties set.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.BaseConfig).</p>
 */
public abstract class BaseConfig implements Serializable {

    private static final long serialVersionUID = 4191902198253890200L;

    /**
     * A map of arbitrary properties configured for this component.
     */
    private Properties properties = new Properties();

    /**
     * Return the arbitrary property value with the given key, if any.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Set an arbitrary arbitrary property with the given key and value.
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Return the entire set of properties configured for this object.
     * At this time this only needs to be exposed to support inheritance
     * (wildcard config matching), so the access stays conservative.
     */
    protected Properties getProperties() {
        return this.properties;
    }

    protected void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * For each property in the source, set the same property in the target.
     */
    public static void copyProperties(BaseConfig source, BaseConfig target) {
        target.properties.putAll(source.properties);
    }

    /**
     * Compare the properties of this config with that of the given and
     * copy those that are not present. This method is used by subclasses
     * that support configuration inheritance.
     */
    protected void inheritProperties(BaseConfig baseConfig) {
        Properties baseProperties = baseConfig.getProperties();
        for (java.util.Enumeration<?> keys = baseProperties.propertyNames();
            keys.hasMoreElements();) {
            String key = (String) keys.nextElement();
            if (this.getProperty(key) == null) {
                setProperty(key, baseProperties.getProperty(key));
            }
        }
    }

    /**
     * Return a copy of the properties held by this object.
     */
    protected Properties copyProperties() {
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + properties + "]";
    }
}
