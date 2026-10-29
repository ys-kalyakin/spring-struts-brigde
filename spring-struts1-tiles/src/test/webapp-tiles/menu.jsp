<%@ page import="org.apache.struts.tiles.ComponentContext" %>
<%
    ComponentContext ctx = ComponentContext.getContext(request);
    out.print("MENU[ITEMS:" + ctx.getAttribute("items")
        + ";EXTRA:" + ctx.getAttribute("extra") + "]");
%>
