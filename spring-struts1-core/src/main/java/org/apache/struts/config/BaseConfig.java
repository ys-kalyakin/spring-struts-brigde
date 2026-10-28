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
     * For each property in the source, set the same property in the target.
     */
    public static void copyProperties(BaseConfig source, BaseConfig target) {
        target.properties.putAll(source.properties);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + properties + "]";
    }
}
