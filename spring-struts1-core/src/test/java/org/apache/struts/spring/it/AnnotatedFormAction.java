package org.apache.struts.spring.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Serves the annotated form: reports the instance identity and the
 * injected-dependency output.
 */
public class AnnotatedFormAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        AnnotatedForm annotated = (AnnotatedForm) form;
        response.getWriter().print("annotated-form:"
            + Integer.toHexString(System.identityHashCode(annotated)) + ":"
            + annotated.describe());
        return null;
    }
}
