package org.apache.struts.action;

import org.apache.struts.config.ForwardConfig;

/**
 * An <strong>ActionForward</strong> represents a destination to which the
 * controller, <code>RequestProcessor</code>, might be directed to perform a
 * RequestDispatcher.forward or HttpServletResponse.sendRedirect to, as a
 * result of processing activities of an <code>Action</code>.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.action.ActionForward).</p>
 */
public class ActionForward extends ForwardConfig {

    private static final long serialVersionUID = 1L;

    public ActionForward() {
    }

    /**
     * Construct a new instance with the specified path.
     */
    public ActionForward(String path) {
        this(null, path, false);
    }

    /**
     * Construct a new instance with the specified path and redirect flag.
     */
    public ActionForward(String path, boolean redirect) {
        this(null, path, redirect);
    }

    public ActionForward(String name, String path, boolean redirect) {
        setName(name);
        setPath(path);
        setRedirect(redirect);
    }

    public ActionForward(String name, String path, boolean redirect,
        String module) {
        this(name, path, redirect);
        setModule(module);
    }

    public ActionForward(ActionForward copyMe) {
        super(copyMe);
    }
}
