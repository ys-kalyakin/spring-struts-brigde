package org.apache.struts.spring.it;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;

/**
 * Dyna validator fixture action: reports the dynamic property.
 */
public class DynaValidatorSubmitAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        DynaValidatorForm dyna = (DynaValidatorForm) form;
        response.getWriter().print("dyna-validator-ok:firstName="
            + dyna.get("firstName"));
        return null;
    }
}
