<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
all=[<html:errors/>]
nameOnly=[<html:errors property="name"/>]
msgs=[<html:messages id="m"><bean:write name="m"/>;</html:messages>]
msgPresent=[<logic:messagesPresent>Y</logic:messagesPresent>]
