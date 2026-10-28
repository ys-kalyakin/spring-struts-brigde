package org.apache.struts.taglib.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.extras.actions.ActionDispatcher;

/**
 * Plain action (no DispatchAction inheritance) using ActionDispatcher with
 * the mapping flavor.
 */
public class ITPlainDispatchAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        ActionDispatcher dispatcher =
            new ActionDispatcher(this, ActionDispatcher.MAPPING_FLAVOR);
        return dispatcher.execute(mapping, form, request, response);
    }

    public ActionForward go(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.getWriter().print("method:go");
        return null;
    }
}
