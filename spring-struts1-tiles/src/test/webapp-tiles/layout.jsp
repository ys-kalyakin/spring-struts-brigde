<%@ page import="org.apache.struts.tiles.ComponentContext" %>
<%
    ComponentContext ctx = ComponentContext.getContext(request);
    out.print("LAYOUT[TITLE:" + ctx.getAttribute("title") + "]");
    Object body = ctx.getAttribute("body");
    if (body instanceof String) {
%>
<jsp:include page="<%= (String) body %>"/>
<%
    }
%>
