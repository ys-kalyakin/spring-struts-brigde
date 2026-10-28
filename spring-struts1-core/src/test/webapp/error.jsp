<%@ page import="org.apache.struts.Globals, org.apache.struts.action.ActionMessages" %>
<%
    Throwable exception = (Throwable) request.getAttribute(Globals.EXCEPTION_KEY);
    ActionMessages errors = (ActionMessages) request.getAttribute(Globals.ERROR_KEY);
    out.print("global-error:" + exception.getClass().getSimpleName() + ":"
        + exception.getMessage() + ":errors="
        + (errors == null ? "0" : errors.size()));
%>
