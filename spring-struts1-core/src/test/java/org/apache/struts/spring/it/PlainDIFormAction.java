package org.apache.struts.spring.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Form fixture action for the unannotated {@link PlainDIForm}: reports the
 * form identity, the injected dependency and the populated property.
 */
public class PlainDIFormAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        String body = "plaindif:"
            + Integer.toHexString(System.identityHashCode(form));
        if (form instanceof PlainDIForm plainForm) {
            body += ":" + plainForm.greet() + ":name=" + plainForm.getName();
        }
        response.getWriter().print(body);
        return null;
    }
}
