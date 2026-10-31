# Spec Delta

## Purpose

The `struts-bean` tag library (URI `http://struts.apache.org/tags-bean`): outputting bean values and messages, defining variables, and access to request and environment data — with the TLD, names, and behavior of the weblegacy reference.

## ADDED Requirements

### Requirement: TLD, URI, and tag name identity
The library TLD SHALL be identical to the weblegacy reference (`struts-bean.tld`, 11 tags): the same URI `http://struts.apache.org/tags-bean`, the same tag and attribute names. JSPs SHALL connect the library with the declaration `<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>` without web.xml edits.

#### Scenario: Connecting and compiling a JSP
- **WHEN** a JSP declares the library by the URI `http://struts.apache.org/tags-bean` and uses `bean:write`
- **THEN** the JSP compiles and the tag is processed without additional declarations

### Requirement: Outputting values with bean:write
`bean:write` SHALL output the value of a bean from a scope by `name`/`property` (honoring `scope`); HTML markup in the value SHALL be escaped by default and output as is with `filter="false"`; `format`/`formatKey` apply formatting.

#### Scenario: Bean property value
- **WHEN** the request contains a bean with the property `name = "Ivan"`, and the JSP contains `<bean:write name="bean" property="name"/>`
- **THEN** `Ivan` is output to the page

#### Scenario: Escaping by default
- **WHEN** the property value contains `<b>`, and the JSP contains `<bean:write name="bean" property="html"/>`
- **THEN** the page shows the escaped text `&lt;b&gt;`, while with `filter="false"` — the original markup

### Requirement: Messages with bean:message
`bean:message` SHALL output a message by `key` (or by the property given by `name`/`property`) from the bundle — the default one or the one specified by the `bundle` attribute, with `arg0..arg3` substitution and the locale per engine semantics.

#### Scenario: Message by key
- **WHEN** the bundle contains `welcome.heading=Welcome!`, and the JSP contains `<bean:message key="welcome.heading"/>`
- **THEN** `Welcome!` is output to the page

#### Scenario: Message with arguments
- **WHEN** the JSP contains `<bean:message key="greeting" arg0="Ivan"/>` and the template `greeting=Hello, {0}!`
- **THEN** `Hello, Ivan!` is output to the page

### Requirement: Defining variables with bean:define
`bean:define` SHALL copy a value (by `name`/`property`/`value`) into a JSP variable and a scope attribute: under the given `id`, in the `toScope` scope, with the type of the source property value.

#### Scenario: Defining and outputting a variable
- **WHEN** the JSP contains `<bean:define id="theName" name="bean" property="name"/>` and then `<bean:write name="theName"/>`
- **THEN** the variable is defined and its value is output to the page

### Requirement: Access to request and environment data
The tags `bean:header`, `bean:cookie`, `bean:parameter` SHALL define a variable from a request header, cookie, or request parameter (with a default `value` and `multiple` support); `bean:size` SHALL output the size of a collection/array/map; `bean:include` SHALL include the content of a resource into a variable; `bean:resource` SHALL load the content of a web application resource; `bean:struts` SHALL provide access to configuration elements (form-bean, forward, mapping).

#### Scenario: Request parameter and its size
- **WHEN** the request contains the parameter `color=blue`, and the JSP contains `<bean:parameter id="color" name="color"/>` and `<bean:write name="color"/>`
- **THEN** `blue` is output to the page

#### Scenario: Request header
- **WHEN** the JSP contains `<bean:header id="lang" name="Accept-Language"/>` and `<bean:write name="lang"/>`
- **THEN** the value of the request header is output to the page

#### Scenario: Including a resource
- **WHEN** the JSP contains `<bean:include id="page" href="/some/page.jsp"/>` and `<bean:write name="page" filter="false"/>`
- **THEN** the variable contains the rendered content of the included page
