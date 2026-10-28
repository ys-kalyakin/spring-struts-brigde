package org.apache.struts.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A JavaBean representing the configuration information of a
 * <code>&lt;plug-in&gt;</code> element from a Struts configuration file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.PlugInConfig).</p>
 */
public class PlugInConfig implements Serializable {

    private static final long serialVersionUID = 6323603250937532319L;

    /** The fully qualified Java class name of the plugin implementation class. */
    protected String className;

    /** The properties that will be used to configure the plugin instance. */
    protected Map<String, Object> properties = new HashMap<>();

    /** Has this component been completely configured? */
    protected boolean configured = false;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Add a new <code>set-property</code> element to the set that will be used
     * to configure the plugin instance.
     */
    public void addProperty(String name, String value) {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
        properties.put(name, value);
    }

    public void freeze() {
        configured = true;
    }

    /**
     * Return the properties that will be used to configure a
     * <code>PlugIn</code> instance.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "PlugInConfig[className=" + className + ",properties="
            + properties + "]";
    }
}
