<%@ page import="org.apache.struts.action.DynaActionForm" %>
<%
    DynaActionForm dyna = (DynaActionForm) request.getAttribute("dynaForm");
    out.print("dyna:testString=" + dyna.get("testString")
        + ":status=" + dyna.get("status"));
%>
