package org.apache.struts.taglib.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Mapping dispatch fixture: the mapping's <code>parameter</code> attribute
 * names the method directly (one mapping per method).
 */
public class ITMappingDispatchAction
    extends org.apache.struts.extras.actions.MappingDispatchAction {

    public ActionForward edit(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.getWriter().print("method:edit");
        return null;
    }

    public ActionForward save(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.getWriter().print("method:save");
        return null;
    }
}
