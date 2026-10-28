package org.apache.struts.spring.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Form fixture action: reports the form instance identity and the populated
 * property so tests can verify scope semantics (request vs session) and
 * populate.
 */
public class FormAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        String body = "form:" + Integer.toHexString(
            System.identityHashCode(form));
        if (form instanceof PersonForm person) {
            body += ":name=" + person.getName() + ":age=" + person.getAge();
        }
        if (form instanceof ValidatingForm validating) {
            body += ":validateCalled=" + validating.isValidateCalled();
        }
        response.getWriter().print(body);
        return null;
    }
}
