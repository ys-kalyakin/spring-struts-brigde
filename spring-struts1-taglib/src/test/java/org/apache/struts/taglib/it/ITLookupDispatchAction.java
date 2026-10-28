package org.apache.struts.taglib.it;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Lookup dispatch fixture: keys button.save / button.cancel from the module
 * message resources select the method.
 */
public class ITLookupDispatchAction
    extends org.apache.struts.extras.actions.LookupDispatchAction {

    @Override
    protected Map<String, String> getKeyMethodMap() {
        return Map.of("button.save", "save", "button.cancel", "cancel");
    }

    private ActionForward respond(HttpServletResponse response, String method)
        throws java.io.IOException {
        response.getWriter().print("method:" + method);
        return null;
    }

    public ActionForward save(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException {
        return respond(response, "save");
    }

    public ActionForward cancel(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException {
        return respond(response, "cancel");
    }
}
