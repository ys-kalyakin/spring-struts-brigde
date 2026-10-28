package org.apache.struts.util;

import org.apache.struts.action.ActionMessage;

/**
 * Used for specialized input handling of exception handling.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.util.ModuleException).</p>
 */
public class ModuleException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * The underlying ActionMessage.
     */
    protected ActionMessage message;

    /**
     * The module-relative path under which this error occurred.
     */
    protected String property = null;

    public ModuleException(String key) {
        super(key);
        message = new ActionMessage(key);
    }

    public ModuleException(String key, Object value) {
        super(key);
        message = new ActionMessage(key, value);
    }

    public ModuleException(String key, Object value0, Object value1) {
        super(key);
        message = new ActionMessage(key, value0, value1);
    }

    public ModuleException(String key, Object value0, Object value1,
        Object value2) {
        super(key);
        message = new ActionMessage(key, value0, value1, value2);
    }

    public ModuleException(String key, Object[] values) {
        super(key);
        message = new ActionMessage(key, values);
    }

    /**
     * Returns an error suitable for rendering by the input form.
     */
    public ActionMessage getActionMessage() {
        return message;
    }

    /**
     * Returns the property name to which the error applies.
     */
    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
