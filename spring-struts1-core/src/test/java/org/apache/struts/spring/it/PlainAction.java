package org.apache.struts.spring.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Plain (unregistered) action fixture: writes a marker directly into the
 * response and returns null.
 */
public class PlainAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.getWriter().print("plain-ok:" + mapping.getPath());
        return null;
    }
}
