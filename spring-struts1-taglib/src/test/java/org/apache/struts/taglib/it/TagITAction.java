package org.apache.struts.taglib.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Prepares test data in the request scope and forwards to the JSP page
 * named by the mapping parameter.
 */
public class TagITAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        String page = mapping.getParameter();

        if (page.endsWith("bean.jsp") || page.endsWith("logic.jsp")
            || page.endsWith("link.jsp") || page.endsWith("nested.jsp")) {
            request.setAttribute("bean", makeBean());
            List<String> list = new ArrayList<>(List.of("A", "B", "C"));
            request.setAttribute("list", list);
        }
        if (page.endsWith("html-errors.jsp")) {
            ActionErrors errors = new ActionErrors();
            errors.add("name", new ActionMessage("errors.required", "Name"));
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage("errors.invalid", "Value"));
            saveErrors(request, errors);
        }
        if (page.endsWith("nested.jsp") && (form instanceof ITForm itForm)) {
            ITForm child = new ITForm();
            child.setName("child-A");
            itForm.getItems().add(child);
        }
        return new ActionForward(page);
    }

    private ITForm makeBean() {
        ITForm bean = new ITForm();
        bean.setName("Ivan");
        bean.setHtml("<b>bold</b>");
        bean.setStatus("closed");
        bean.setPath("/admin/users");
        bean.setNote("");
        bean.setKey(7);
        return bean;
    }
}
