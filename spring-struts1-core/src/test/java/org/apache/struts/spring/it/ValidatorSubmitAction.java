package org.apache.struts.spring.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorFixtureForm;

/**
 * Validator fixture action: reports the validated form fields.
 */
public class ValidatorSubmitAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        ValidatorFixtureForm fixture = (ValidatorFixtureForm) form;
        response.getWriter().print("validator-ok:firstName="
            + fixture.getFirstName() + ":zip=" + fixture.getZip());
        return null;
    }
}
