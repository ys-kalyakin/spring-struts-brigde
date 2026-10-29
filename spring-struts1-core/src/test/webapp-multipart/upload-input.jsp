<%@ page import="org.apache.struts.Globals, org.apache.struts.action.ActionMessages" %>
<%
    ActionMessages errors = (ActionMessages) request.getAttribute(Globals.ERROR_KEY);
    out.print("upload-input-errors:" + (errors == null ? "0" : errors.size()));
%>
