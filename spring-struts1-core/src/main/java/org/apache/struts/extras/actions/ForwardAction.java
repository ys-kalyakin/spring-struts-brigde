package org.apache.struts.extras.actions;

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
 * An <strong>Action</strong> that forwards to the context-relative URI
 * specified by the <code>parameter</code> property of the associated
 * ActionMapping. Useful for pages that must render behind a mapped action
 * path without their own logic.
 *
 * <p>Ported to the spring-struts-bridge engine with the classic Struts 1.3
 * API.</p>
 */
public class ForwardAction extends Action {

    private static final Logger log =
        LoggerFactory.getLogger(ForwardAction.class);

    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
        MessageResources.getMessageResources(
            "org.apache.struts.extras.actions.LocalStrings");

    /**
     * Forward to the resource named by the mapping's <code>parameter</code>.
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        // The "forward" path comes from the mapping parameter
        String path = mapping.getParameter();

        if (path == null) {
            String message =
                messages.getMessage("forward.path", mapping.getPath());
            log.error(message);
            throw new ServletException(message);
        }

        return new ActionForward(path);
    }
}
