package org.apache.struts.config;

import org.apache.struts.Globals;

/**
 * A JavaBean representing the configuration information of a
 * <code>&lt;message-resources&gt;</code> element from a Struts configuration
 * file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.MessageResourcesConfig).</p>
 */
public class MessageResourcesConfig extends BaseConfig {

    private static final long serialVersionUID = -2997679878186259584L;

    /** The fully qualified Java class name of the MessageResourcesFactory. */
    protected String factory =
        "org.apache.struts.util.PropertyMessageResourcesFactory";

    /** The servlet context attributes key under which this MessageResources is stored. */
    protected String key = Globals.MESSAGES_KEY;

    /** The configuration parameter (bundle base name). */
    protected String parameter;

    /** Should we return <code>null</code> for missing message keys? */
    protected boolean nullValue = false;

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public boolean getNull() {
        return nullValue;
    }

    public void setNull(boolean nullValue) {
        this.nullValue = nullValue;
    }

    @Override
    public String toString() {
        return "MessageResourcesConfig[key=" + key + ",parameter=" + parameter
            + ",factory=" + factory + "]";
    }
}
