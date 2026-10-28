<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>
<nested:form action="/formSubmit.do">
text=[<nested:text property="name"/>]
items=[<nested:iterate property="items">[<nested:write property="name"/>]</nested:iterate>]
root=[<nested:root name="bean"><nested:write property="name"/></nested:root>]
</nested:form>
