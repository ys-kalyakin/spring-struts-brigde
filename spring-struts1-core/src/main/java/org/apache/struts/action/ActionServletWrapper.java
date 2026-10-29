package org.apache.struts.action;

import java.io.Serializable;

import org.apache.struts.upload.MultipartRequestHandler;

/**
 * Provide a wrapper around an {@link ActionServlet} to expose only those
 * methods needed by other objects. When used with an {@link ActionForm},
 * subclasses must be careful that they do not return an object with public
 * getters and setters that could be exploited by automatic population of
 * properties.
 *
 * <p>Ported from weblegacy/struts1 1.5.x
 * (org.apache.struts.action.ActionServletWrapper).</p>
 */
public class ActionServletWrapper implements Serializable {
    private static final long serialVersionUID = 3775546877948572436L;

    /**
     * The servlet instance to which we are attached.
     */
    protected transient ActionServlet servlet = null;

    /**
     * Create object and set <code>servlet</code> property.
     *
     * @param servlet <code>ActionServlet</code> to wrap
     */
    public ActionServletWrapper(ActionServlet servlet) {
        super();
        this.servlet = servlet;
    }

    /**
     * Set servlet to a <code>MultipartRequestHandler</code>.
     *
     * @param object The MultipartRequestHandler
     */
    public void setServletFor(MultipartRequestHandler object) {
        object.setServlet(this.servlet);
    }
}
