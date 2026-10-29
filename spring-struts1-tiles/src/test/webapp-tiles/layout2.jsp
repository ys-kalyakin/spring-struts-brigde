<%@ page import="org.apache.struts.tiles.ComponentContext" %>
<%
    ComponentContext ctx = ComponentContext.getContext(request);
    out.print("LAYOUT2[TITLE:" + ctx.getAttribute("title") + "]");
%>
