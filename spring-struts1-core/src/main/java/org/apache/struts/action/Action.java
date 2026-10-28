package org.apache.struts.action;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.struts.Globals;

/**
 * An <strong>Action</strong> is an adapter between the contents of an
 * incoming HTTP request and the corresponding business logic that should be
 * executed to process this request.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.action.Action).</p>
 */
public abstract class Action {

    /**
     * The <code>ActionServlet</code> that owns this class.
     */
    protected transient ActionServlet servlet = null;

    /**
     * Return the ActionServlet instance that owns this Action.
     */
    public ActionServlet getServlet() {
        return servlet;
    }

    /**
     * Set the ActionServlet instance that owns this Action.
     */
    public void setServlet(ActionServlet servlet) {
        this.servlet = servlet;
    }

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response, returning an <code>ActionForward</code> object describing
     * where and how control should be forwarded, or <code>null</code> if the
     * response has already been completed.
     */
    public abstract ActionForward execute(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception;

    /**
     * Save the specified error messages keys into the appropriate request
     * attribute for use by the &lt;html:errors&gt; tag, if any messages are
     * required. Otherwise, ensure that the request attribute is not created.
     */
    protected void saveErrors(HttpServletRequest request,
        ActionMessages errors) {
        if (errors == null || errors.isEmpty()) {
            request.removeAttribute(Globals.ERROR_KEY);
            return;
        }
        request.setAttribute(Globals.ERROR_KEY, errors);
    }

    /**
     * Save the specified error messages keys into the appropriate session
     * attribute for use by the &lt;html:errors&gt; tag, if any messages are
     * required. Otherwise, ensure that the session attribute is not created.
     */
    protected void saveErrors(HttpSession session, ActionMessages errors) {
        if (errors == null || errors.isEmpty()) {
            session.removeAttribute(Globals.ERROR_KEY);
            return;
        }
        session.setAttribute(Globals.ERROR_KEY, errors);
    }

    /**
     * Save the specified messages keys into the appropriate request attribute
     * for use by the &lt;html:messages&gt; tag, if any messages are required.
     * Otherwise, ensure that the request attribute is not created.
     */
    protected void saveMessages(HttpServletRequest request,
        ActionMessages messages) {
        if (messages == null || messages.isEmpty()) {
            request.removeAttribute(Globals.MESSAGE_KEY);
            return;
        }
        request.setAttribute(Globals.MESSAGE_KEY, messages);
    }
}
