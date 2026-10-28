<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head><title>spring-integration</title></head>
<body>
el-bean:${springDemo.message}
spring-message:<spring:message code="spring.demo.message"/>
di-form:<bean:write name="demoForm" property="serviceGreeting"/>
errors:<html:errors/>
html-form:<html:form action="/SpringIntegration"><html:text property="label"/></html:form>
</body>
</html>
