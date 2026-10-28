package org.apache.struts.taglib.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.apache.struts.extras.actions.EventDispatchAction;

/**
 * Event dispatch fixture action.
 */
public class ITEventDispatchAction extends EventDispatchAction {

    public ActionForward save(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.getWriter().print("method:save");
        return null;
    }

    public ActionForward cancel(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.getWriter().print("method:cancel");
        return null;
    }

    public ActionForward go(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.getWriter().print("method:go");
        return null;
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        response.getWriter().print("method:unspecified");
        return null;
    }
}
