# html-taglib Specification

## Purpose

The `struts-html` tag library (URI `http://struts.apache.org/tags-html`): forms and input widgets, rendering errors and messages, URL building (img/link/rewrite) and xhtml mode — with the TLD, names and HTML output structure of the weblegacy reference.

## Requirements

### Requirement: TLD, URI and tag name identity
The library TLD SHALL be identical to the weblegacy reference (`struts-html.tld`, 30 tags): the same URI `http://struts.apache.org/tags-html`, the same tag and attribute names; the JSP declaration — `<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>`.

#### Scenario: JSP inclusion and compilation
- **WHEN** a JSP declares the library by URI `http://struts.apache.org/tags-html` and uses `html:form` with widgets
- **THEN** the JSP compiles and the tags are processed without additional declarations

### Requirement: Form via html:form
`html:form` SHALL render the `<form>` element: with `action` — the action URL (per the mapping, with the application context, `post` method by default and URL encoding); without `action` — a form without the action attribute; the `focus`, `onsubmit` attributes and arbitrary dynamic attributes are carried over into the output; together with the form, token hidden fields are rendered, if used.

#### Scenario: Form by action
- **WHEN** the JSP contains `<html:form action="/InputSubmit.do">` with `<html:text property="name"/>`
- **THEN** `<form name="..." action="/ctx/InputSubmit.do" method="post">` is rendered with `<input type="text" name="name" value="...">`, where value comes from the form in scope

#### Scenario: Form without action
- **WHEN** the JSP contains `<html:form>` without the action attribute (as the html-form exercise page)
- **THEN** `<form>` is rendered without the action attribute, and the body is processed

### Requirement: Input widgets
Tags `text`, `password`, `hidden`, `textarea`, `checkbox`, `multibox`, `radio`, `select`, `option`, `options`, `optionsCollection`, `submit`, `reset`, `button`, `cancel`, `image`, `file`, `textarea` SHALL render the corresponding elements with the name from `property`, the value from the form in scope (or from `value`), event and style attributes from the same-named tag attributes; the `html:errors`-related attributes `errorKey` etc. are honored.

#### Scenario: Text field with the form value
- **WHEN** the form in scope contains `name = "Ivan"`, and the JSP contains `<html:text property="name"/>`
- **THEN** `<input type="text" name="name" value="Ivan">` is rendered

#### Scenario: Select with options
- **WHEN** the JSP contains `<html:select property="color">` with `<html:option value="blue">Blue</html:option>` and the form contains `color = "blue"`
- **THEN** a `select` is rendered with option `blue` selected (attribute `selected` on the corresponding option)

#### Scenario: Checkbox from a property
- **WHEN** the JSP contains `<html:checkbox property="active"/>` and the form property `active = true`
- **THEN** `<input type="checkbox" name="active" checked="checked">` is rendered (checked attribute according to the value)

### Requirement: Rendering errors and messages
`html:errors` SHALL render errors from the request/session (`ActionErrors` under the engine's key): each error element is wrapped with `errors.prefix`/`errors.suffix`, the list — with `errors.header`/`errors.footer` (by default `<UL>`/`<LI>`/`</LI>`/`</UL>`), the text — from the bundle by the message key with arguments; `property` filters by property. `html:messages` SHALL render arbitrary `ActionMessages` with the given header/footer/prefix/suffix and the `id` variable.

#### Scenario: Rendering an error list
- **WHEN** the request holds two errors (keys `errors.required` with an argument and `errors.invalid`), and the JSP contains `<html:errors/>`
- **THEN** a `<UL>` with two `<LI>` is rendered, the text of each being the bundle message with the substituted argument

#### Scenario: Errors of a specific property
- **WHEN** the JSP contains `<html:errors property="name"/>`
- **THEN** only errors of property `name` are rendered

### Requirement: URL building: img, link, rewrite
`html:link` SHALL build `<a href>` by `forward` (from global forwards), `action` or `href` with the application context, parameter addition (`paramId`/`paramName` or nested `html:param`) and encoding; `html:rewrite` SHALL output only the URL (without the `<a>` element); `html:img` SHALL render `<img>` with a context-aware URL and parameters; `html:base` SHALL render `<base href>` per the current request; `html:html` SHALL render `<html>` with the `lang` attribute from the locale.

#### Scenario: Link by forward
- **WHEN** the global forward `welcome` leads to `/Welcome.do`, and the JSP contains `<html:link forward="welcome">Go</html:link>`
- **THEN** `<a href="/ctx/Welcome.do">Go</a>` is rendered (with the application context)

#### Scenario: Link with a parameter
- **WHEN** the JSP contains `<html:link href="/list.do" paramId="id" paramName="bean" paramProperty="key">…</html:link>` and the bean contains `key = 7`
- **THEN** `<a href="/ctx/list.do?id=7">…</a>` is rendered

### Requirement: Xhtml mode
With xhtml mode enabled (engine attribute / `html:xhtml` tag), the library tags SHALL render valid XHTML: self-closing elements (`<input ... />`), value attributes in the form `selected="selected"`/`checked="checked"`, correct output of `javascript` content; when disabled — classic HTML output.

#### Scenario: Self-closing elements in xhtml
- **WHEN** xhtml mode is enabled, and the JSP contains `<html:text property="name"/>`
- **THEN** `<input type="text" name="name" value="..."/>` is rendered (a self-closing element)

### Requirement: Deferred validator tag
The `html:javascript` tag is implemented in the validation phase: it SHALL generate commons-validator client scripts per the validation.xml rules (behavior defined in the `struts-validator` spec); the tag's TLD declaration remains unchanged.

#### Scenario: Using the javascript tag
- **WHEN** the JSP uses `<html:javascript formName="..."/>`, and validation.xml declares rules for this form
- **THEN** the page contains the generated form validation JavaScript, not a processing error
