<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<html>
<body>
<h1><bean:write name="greeting"/></h1>
<p>action-instance=<bean:write name="actionInstanceId"/></p>
<p>form-instance=<bean:write name="formInstanceId"/></p>
</body>
</html>
