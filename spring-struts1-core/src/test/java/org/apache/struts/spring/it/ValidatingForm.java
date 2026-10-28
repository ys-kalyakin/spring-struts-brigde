package org.apache.struts.spring.it;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/**
 * Form fixture whose validate() reports an error when the name is empty.
 */
public class ValidatingForm extends ActionForm {

    private static final long serialVersionUID = 1L;

    private String name;

    private boolean validateCalled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValidateCalled() {
        return validateCalled;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping,
        HttpServletRequest request) {
        validateCalled = true;
        if (name == null || name.isBlank()) {
            ActionErrors errors = new ActionErrors();
            errors.add("name", new ActionMessage("errors.name.required"));
            return errors;
        }
        return null;
    }
}
