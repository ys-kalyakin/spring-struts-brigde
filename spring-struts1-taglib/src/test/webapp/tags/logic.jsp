<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
iter=[<logic:iterate id="item" name="list"><bean:write name="item"/>;</logic:iterate>]
idx=[<logic:iterate id="item" name="list" indexId="i"><bean:write name="i"/>,</logic:iterate>]
off=[<logic:iterate id="item" name="list" offset="1" length="2"><bean:write name="item"/>,</logic:iterate>]
eqParam=[<logic:equal parameter="page" value="3">Y</logic:equal>]
neProp=[<logic:notEqual name="bean" property="status" value="open">Y</logic:notEqual>]
matchStart=[<logic:match name="bean" property="path" value="/admin" location="start">Y</logic:match>]
empty=[<logic:empty name="bean" property="note">Y</logic:empty>]
presentMsg=[<logic:present name="org.apache.struts.action.MESSAGE" scope="application">Y</logic:present>]
notPresentReq=[<logic:notPresent name="no.such.attr" scope="request">Y</logic:notPresent>]
msgNotPresent=[<logic:messagesNotPresent>Y</logic:messagesNotPresent>]
