<%@ page import="org.apache.struts.tiles.ComponentContext" %>
<%
    ComponentContext ctx = ComponentContext.getContext(request);
    out.print("TILESACTION[TITLE:" + ctx.getAttribute("title") + "]");
%>
