<%@ page import="org.apache.struts.tiles.ComponentContext" %>
<%
    ComponentContext ctx = ComponentContext.getContext(request);
    out.print("BODY-CONTENT:" + ctx.getAttribute("title"));
%>
