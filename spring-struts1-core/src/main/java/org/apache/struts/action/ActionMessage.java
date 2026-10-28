package org.apache.struts.action;

import java.io.Serializable;

/**
 * An encapsulation of an individual message returned by the
 * <code>validate()</code> method of an <code>ActionForm</code>.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.action.ActionMessage).</p>
 */
public class ActionMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The message key for this message.
     */
    protected String key;

    /**
     * The replacement values for this message.
     */
    protected Object[] values;

    /**
     * Indicates whether the key is a message resources key or a literal value.
     */
    protected boolean resource = true;

    public ActionMessage(String key) {
        this(key, (Object[]) null);
    }

    public ActionMessage(String key, Object value0) {
        this(key, new Object[] { value0 });
    }

    public ActionMessage(String key, Object value0, Object value1) {
        this(key, new Object[] { value0, value1 });
    }

    public ActionMessage(String key, Object value0, Object value1,
        Object value2) {
        this(key, new Object[] { value0, value1, value2 });
    }

    public ActionMessage(String key, Object value0, Object value1,
        Object value2, Object value3) {
        this(key, new Object[] { value0, value1, value2, value3 });
    }

    public ActionMessage(String key, Object[] values) {
        this.key = key;
        this.values = values;
    }

    public ActionMessage(String key, boolean resource) {
        this.key = key;
        this.resource = resource;
    }

    public String getKey() {
        return key;
    }

    public Object[] getValues() {
        return values;
    }

    public boolean isResource() {
        return resource;
    }

    public void setResource(boolean resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "ActionMessage[key=" + key + ",values="
            + java.util.Arrays.toString(values) + ",resource=" + resource
            + "]";
    }
}
