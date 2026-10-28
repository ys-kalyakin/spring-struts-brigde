<%--

Welcome page of the ported blank application. The weblegacy original uses
the struts-bean / struts-html / struts-logic tag libraries, which arrive in
phase 2; the plain-JSP equivalent below renders the same content.

--%>
<%@ page import="org.apache.struts.Globals,
                 org.apache.struts.util.MessageResources,
                 java.util.Locale" %>
<%
    MessageResources resources = (MessageResources)
        application.getAttribute(Globals.MESSAGES_KEY);
    Locale locale = (Locale) session.getAttribute(Globals.LOCALE_KEY);
    String title = (resources != null)
        ? resources.getMessage(locale, "welcome.title")
        : null;
    String heading = (resources != null)
        ? resources.getMessage(locale, "welcome.heading")
        : null;
    String message = (resources != null)
        ? resources.getMessage(locale, "welcome.message")
        : null;
%>
<html>
<head>
<title><%= title %></title>
</head>
<body bgcolor="white">

<% if (resources == null) { %>
  <font color="red">
    ERROR:  Application resources not loaded -- check servlet container
    logs for error messages.
  </font>
<% } %>

<h3><%= heading %></h3>
<p><%= message %></p>

</body>
</html>
