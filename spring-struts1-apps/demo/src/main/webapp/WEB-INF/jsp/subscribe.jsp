<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<html>
<body>
<h1><bean:message key="subscribe.title"/></h1>
<html:errors/>
<html:form action="subscribeSubmit">
  <bean:message key="subscribe.name"/>: <html:text property="name"/><br/>
  <bean:message key="subscribe.email"/>: <html:text property="email"/><br/>
  <html:submit><bean:message key="subscribe.submit"/></html:submit>
</html:form>
</body>
</html>
