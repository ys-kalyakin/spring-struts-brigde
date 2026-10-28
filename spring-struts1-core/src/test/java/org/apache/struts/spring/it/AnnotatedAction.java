package org.apache.struts.spring.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.StrutsAction;

/**
 * Annotated action fixture: a singleton Spring bean with an injected
 * dependency — the same instance must serve every request.
 */
@StrutsAction
public class AnnotatedAction extends Action {

    @Autowired
    private transient GreeterService greeterService;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        response.getWriter().print("annotated:"
            + Integer.toHexString(System.identityHashCode(this)) + ":"
            + greeterService.greet());
        return null;
    }
}
