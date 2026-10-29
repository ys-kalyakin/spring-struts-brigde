<%@ page import="org.apache.struts.tiles.ComponentContext" %>
<%
    ComponentContext ctx = ComponentContext.getContext(request);
    out.print("LOCALE[TITLE:" + ctx.getAttribute("title") + "]");
%>
