<%@ page import="org.apache.struts.Globals" %>
<%
    Throwable exception = (Throwable) request.getAttribute(Globals.EXCEPTION_KEY);
    out.print("local-error:" + exception.getMessage());
%>
