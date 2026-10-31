# bean-taglib Specification

## Purpose

The `struts-bean` tag library (URI `http://struts.apache.org/tags-bean`): rendering bean values and messages, defining variables, and access to request and environment data — with the TLD, names and behavior of the weblegacy reference.

## Requirements

### Requirement: TLD, URI and tag name identity
The library TLD SHALL be identical to the weblegacy reference (`struts-bean.tld`, 11 tags): the same URI `http://struts.apache.org/tags-bean`, the same tag and attribute names. JSPs SHALL include the library with the declaration `<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>` without web.xml changes.

#### Scenario: JSP inclusion and compilation
- **WHEN** a JSP declares the library by URI `http://struts.apache.org/tags-bean` and uses `bean:write`
- **THEN** the JSP compiles and the tag is processed without additional declarations

### Requirement: Rendering values with bean:write
`bean:write` SHALL render the value of a bean from a scope by `name`/`property` (taking `scope` into account); HTML markup in the value SHALL be escaped by default and rendered as is with `filter="false"`; `format`/`formatKey` apply formatting.

#### Scenario: Bean property value
- **WHEN** the request holds a bean with property `name = "Ivan"`, and the JSP contains `<bean:write name="bean" property="name"/>`
- **THEN** the page renders `Ivan`

#### Scenario: Escaping by default
- **WHEN** the property value contains `<b>`, and the JSP contains `<bean:write name="bean" property="html"/>`
- **THEN** the page renders the escaped text `&lt;b&gt;`, and with `filter="false"` — the original markup

### Requirement: Messages with bean:message
`bean:message` SHALL render a message by `key` (or by the property specified by `name`/`property`) from the bundle — the default one or the one specified by the `bundle` attribute, with `arg0..arg3` substitution and locale per engine semantics.

#### Scenario: Message by key
- **WHEN** the bundle contains `welcome.heading=Welcome!`, and the JSP contains `<bean:message key="welcome.heading"/>`
- **THEN** the page renders `Welcome!`

#### Scenario: Message with arguments
- **WHEN** the JSP contains `<bean:message key="greeting" arg0="Ivan"/>` and the template `greeting=Hello, {0}!`
- **THEN** the page renders `Hello, Ivan!`

### Requirement: Defining variables with bean:define
`bean:define` SHALL copy a value (by `name`/`property`/`value`) into a JSP variable and a scope attribute: under the given `id`, into the scope `toScope`, with the type of the source property value.

#### Scenario: Defining and rendering a variable
- **WHEN** the JSP contains `<bean:define id="theName" name="bean" property="name"/>` and then `<bean:write name="theName"/>`
- **THEN** the variable is defined and the page renders its value

### Requirement: Access to request and environment data
Tags `bean:header`, `bean:cookie`, `bean:parameter` SHALL define a variable from a request header, cookie or request parameter (with `value` as the default and `multiple` support); `bean:size` SHALL render the size of a collection/array/map; `bean:include` SHALL include the content of a resource into a variable; `bean:resource` SHALL load the content of a web application resource; `bean:struts` SHALL provide access to configuration elements (form-bean, forward, mapping).

#### Scenario: Request parameter and its size
- **WHEN** the request contains parameter `color=blue`, and the JSP contains `<bean:parameter id="color" name="color"/>` and `<bean:write name="color"/>`
- **THEN** the page renders `blue`

#### Scenario: Request header
- **WHEN** the JSP contains `<bean:header id="lang" name="Accept-Language"/>` and `<bean:write name="lang"/>`
- **THEN** the page renders the value of the request header

#### Scenario: Resource inclusion
- **WHEN** the JSP contains `<bean:include id="page" href="/some/page.jsp"/>` and `<bean:write name="page" filter="false"/>`
- **THEN** the variable contains the rendered content of the included page
