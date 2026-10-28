package org.apache.struts.taglib.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Dispatch fixture action: writes the dispatched method name.
 */
public class ITDispatchAction
    extends org.apache.struts.extras.actions.DispatchAction {

    private ActionForward respond(HttpServletRequest request,
        HttpServletResponse response, String method) throws IOException {
        response.getWriter().print("method:" + method);
        return null;
    }

    public ActionForward save(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        return respond(request, response, "save");
    }

    public ActionForward cancel(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        return respond(request, response, "cancel");
    }

    public ActionForward edit(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        return respond(request, response, "edit");
    }

    public ActionForward go(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        return respond(request, response, "go");
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        response.getWriter().print("method:unspecified");
        return null;
    }
}
