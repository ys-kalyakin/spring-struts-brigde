<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<html>
<body>
<h1><bean:message key="welcome.heading"/></h1>
<p>form-name=<bean:write name="testForm" property="name"/></p>
</body>
</html>
