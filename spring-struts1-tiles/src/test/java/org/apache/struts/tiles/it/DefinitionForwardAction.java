package org.apache.struts.tiles.it;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Fixture action: forwards to the named (definition) forward of its mapping,
 * driving the definition-forward hook of the engine.
 */
public class DefinitionForwardAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response) {
        return mapping.findForward("page");
    }
}
