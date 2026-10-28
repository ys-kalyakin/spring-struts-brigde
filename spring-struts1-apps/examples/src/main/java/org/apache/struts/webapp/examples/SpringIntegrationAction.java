package org.apache.struts.webapp.examples;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/**
 * Demo action of the spring-integration showcase: an unannotated action
 * declared in struts-config — the engine registers it as a Spring singleton
 * with dependency injection. It reports one error and forwards to the JSP
 * that mixes Struts tags, Spring tags and EL access to Spring beans.
 */
public class SpringIntegrationAction extends Action {

    @Autowired
    private transient DemoMessageService demoMessageService;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response) {
        ActionErrors errors = new ActionErrors();
        errors.add(ActionErrors.GLOBAL_MESSAGE,
            new ActionMessage("spring.demo.error"));
        saveErrors(request, errors);
        request.setAttribute("serviceMessage",
            demoMessageService.getMessage());
        return mapping.findForward("page");
    }
}
