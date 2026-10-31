package org.apache.struts.bench;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The action executed by the pipeline benchmark: immediately forwards to a
 * static result path resolved through the mock servlet context.
 */
public class BenchAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response) {
        return new ActionForward("/bench-result.jsp");
    }
}
