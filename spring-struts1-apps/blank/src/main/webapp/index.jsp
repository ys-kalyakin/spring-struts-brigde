<%--

Redirect default requests to Welcome global ActionForward.
By using a redirect, the user-agent will change address to match the path of
our Welcome ActionForward. (Ported from weblegacy apps/blank: the original
uses the logic:redirect tag, which arrives with the tag libraries in phase 2.)

--%>
<% response.sendRedirect(request.getContextPath() + "/Welcome.do"); %>
