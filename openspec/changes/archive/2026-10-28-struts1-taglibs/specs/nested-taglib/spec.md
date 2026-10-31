# Spec Delta

## Purpose

The `struts-nested` tag library (URI `http://struts.apache.org/tags-nested`): nested wrappers over bean/html/logic that maintain a relative property path across nesting levels — with the TLD, names, and behavior of the weblegacy reference.

## ADDED Requirements

### Requirement: TLD, URI, and tag name identity
The library TLD SHALL be identical to the weblegacy reference (`struts-nested.tld`, 40 tags): the same URI `http://struts.apache.org/tags-nested`, the same tag and attribute names; the JSP declaration — `<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>`.

#### Scenario: Connecting and compiling a JSP
- **WHEN** a JSP declares the library by the URI `http://struts.apache.org/tags-nested` and uses `nested:form` with nested tags
- **THEN** the JSP compiles and the tags are processed without additional declarations

### Requirement: Nested relative paths
Every nested tag with a `property` attribute SHALL build the full property name as the concatenation of the paths of all open nested levels (`nested:nest`, `nested:form`, `nested:iterate`) and its own `property`: widgets are rendered with the full property name, and values are taken from the corresponding object of the chain.

#### Scenario: Property inside an iteration
- **WHEN** a bean in scope contains a list of elements with the property `name`, and the JSP contains `nested:iterate property="items"` with `nested:write property="name"` inside
- **THEN** each value is output from the `name` property of the current list element

#### Scenario: Widget with a full path
- **WHEN** the JSP contains `nested:form action="/Edit.do"` with `nested:text property="person.name"`
- **THEN** `<input type="text" name="person.name" value="...">` is output with the value from the form's `person.name` property

### Requirement: Rendering equivalence to the base tags
The nested counterparts of tags (`nested:write`, `nested:define`, `nested:text`, `nested:select`, `nested:iterate`, `nested:present`, and the rest) SHALL render and behave the same as the identically named bean/html/logic tags, differing only in relative path resolution; `nested:root` and `nested:reference` SHALL allow referring to the root bean and the current nesting level.

#### Scenario: Referring to the root bean
- **WHEN** inside nesting the JSP contains `nested:write property="rootProperty"` on a `nested:root` with `name="bean"`
- **THEN** the value is taken from the root bean's property, not from the nesting chain
