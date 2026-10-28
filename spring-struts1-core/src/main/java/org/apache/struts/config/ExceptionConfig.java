package org.apache.struts.config;

/**
 * A JavaBean representing the configuration information of an
 * <code>&lt;exception&gt;</code> element from a Struts configuration file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.ExceptionConfig).</p>
 */
public class ExceptionConfig extends BaseConfig {

    private static final long serialVersionUID = 8977916943183890264L;

    /** The servlet context attribute key for the MessageResources instance. */
    protected String bundle;

    /** The fully qualified Java class name of the exception handler class. */
    protected String handler = "org.apache.struts.action.ExceptionHandler";

    /** The message resources key specifying the error message. */
    protected String key;

    /** The module-relative path of the resource for error handling. */
    protected String path;

    /** The scope ("request" or "session") in which the error is stored. */
    protected String scope = "request";

    /** The fully qualified Java class name of the exception. */
    protected String type;

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ExceptionConfig[type=" + type + ",key=" + key + ",path="
            + path + ",scope=" + scope + ",handler=" + handler + "]";
    }
}
