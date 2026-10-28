package org.apache.struts.spring.it;

import java.io.IOException;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

/**
 * Reports messages for the session locale and the missing-key format; can
 * also set the session locale first (param <code>setLocale</code>).
 */
public class MsgAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        String setLocale = request.getParameter("setLocale");
        if (setLocale != null) {
            String[] parts = setLocale.split("_");
            Locale locale = (parts.length > 1)
                ? new Locale(parts[0], parts[1])
                : new Locale(parts[0]);
            request.getSession().setAttribute(Globals.LOCALE_KEY, locale);
        }
        Locale locale = (Locale) request.getSession()
            .getAttribute(Globals.LOCALE_KEY);
        MessageResources resources = (MessageResources) getServlet()
            .getServletContext().getAttribute(Globals.MESSAGES_KEY);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print("msg:locale=" + locale
            + ":" + resources.getMessage(locale, "it.hello")
            + ":" + resources.getMessage(locale, "it.missing"));
        return null;
    }
}
