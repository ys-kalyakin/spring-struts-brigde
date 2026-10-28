<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<html:form action="/formSubmit.do">
text=[<html:text property="name"/>]
select=[<html:select property="color"><html:option value="blue">Blue</html:option><html:option value="red">Red</html:option></html:select>]
checkbox=[<html:checkbox property="active"/>]
hidden=[<html:hidden property="token"/>]
submit=[<html:submit/>]
</html:form>
