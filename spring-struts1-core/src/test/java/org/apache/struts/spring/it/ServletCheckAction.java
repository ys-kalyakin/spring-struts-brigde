package org.apache.struts.spring.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;

/**
 * Verifies the ActionServlet facade contract from inside an action.
 */
public class ServletCheckAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        ActionServlet servlet = getServlet();
        String configParam = (servlet != null)
            ? servlet.getInitParameter("config")
            : null;
        response.getWriter().print("servlet:" + (servlet != null) + ":config="
            + configParam);
        return null;
    }
}
