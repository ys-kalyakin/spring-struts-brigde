<%@ page import="org.apache.struts.tiles.ComponentContext" %>
<%
    ComponentContext ctx = ComponentContext.getContext(request);
    out.print("CONTROLLED[TITLE:" + ctx.getAttribute("title")
        + ";EXTRA:" + ctx.getAttribute("extra") + "]");
%>
