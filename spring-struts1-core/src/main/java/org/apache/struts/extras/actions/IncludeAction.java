package org.apache.struts.extras.actions;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

/**
 * An <strong>Action</strong> that includes the context-relative URI
 * specified by the <code>parameter</code> property of the associated
 * ActionMapping in the current response.
 *
 * <p>Ported to the spring-struts-bridge engine with the classic Struts 1.3
 * API.</p>
 */
public class IncludeAction extends Action {

    private static final Logger log =
        LoggerFactory.getLogger(IncludeAction.class);

    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
        MessageResources.getMessageResources(
            "org.apache.struts.extras.actions.LocalStrings");

    /**
     * Include the resource named by the mapping's <code>parameter</code>.
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        // The "include" path comes from the mapping parameter
        String uri = mapping.getParameter();

        if (uri == null) {
            String message =
                messages.getMessage("include.path", mapping.getPath());
            log.error(message);
            throw new ServletException(message);
        }

        RequestDispatcher dispatcher =
            getServlet().getServletContext().getRequestDispatcher(uri);
        if (dispatcher == null) {
            String message =
                messages.getMessage("include.invalid", uri);
            log.error(message);
            throw new ServletException(message);
        }

        dispatcher.include(request, response);
        return null;
    }
}
