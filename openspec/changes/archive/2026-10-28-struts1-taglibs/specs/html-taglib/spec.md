# Spec Delta

## Purpose

The `struts-html` tag library (URI `http://struts.apache.org/tags-html`): forms and input widgets, error and message output, URL building (img/link/rewrite), and the xhtml mode — with the TLD, names, and HTML output structure of the weblegacy reference.

## ADDED Requirements

### Requirement: TLD, URI, and tag name identity
The library TLD SHALL be identical to the weblegacy reference (`struts-html.tld`, 30 tags): the same URI `http://struts.apache.org/tags-html`, the same tag and attribute names; the JSP declaration — `<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>`.

#### Scenario: Connecting and compiling a JSP
- **WHEN** a JSP declares the library by the URI `http://struts.apache.org/tags-html` and uses `html:form` with widgets
- **THEN** the JSP compiles and the tags are processed without additional declarations

### Requirement: Form with html:form
`html:form` SHALL render a `<form>` element: with `action` — the action URL (per the mapping, with the application context, the `post` method by default, and URL encoding); without `action` — a form without the action attribute; the `focus`, `onsubmit`, and arbitrary dynamic attributes are carried into the output; hidden token fields are rendered together with the form if tokens are in use.

#### Scenario: Form by action
- **WHEN** the JSP contains `<html:form action="/InputSubmit.do">` with `<html:text property="name"/>`
- **THEN** `<form name="..." action="/ctx/InputSubmit.do" method="post">` is output with `<input type="text" name="name" value="...">`, where value comes from the form in scope

#### Scenario: Form without action
- **WHEN** the JSP contains `<html:form>` without the action attribute (as the html-form exercise page)
- **THEN** a `<form>` is output without the action attribute, and the body is processed

### Requirement: Input widgets
The tags `text`, `password`, `hidden`, `textarea`, `checkbox`, `multibox`, `radio`, `select`, `option`, `options`, `optionsCollection`, `submit`, `reset`, `button`, `cancel`, `image`, `file`, `textarea` SHALL render the corresponding elements with the name from `property`, the value from the form in scope (or from `value`), and event/style attributes from the identically named tag attributes; the `html:errors`-related attributes `errorKey` etc. are honored.

#### Scenario: Text field with the form value
- **WHEN** the form in scope contains `name = "Ivan"`, and the JSP contains `<html:text property="name"/>`
- **THEN** `<input type="text" name="name" value="Ivan">` is output

#### Scenario: Select with options
- **WHEN** the JSP contains `<html:select property="color">` with `<html:option value="blue">Blue</html:option>` and the form contains `color = "blue"`
- **THEN** a `select` is output with the `blue` option selected (the `selected` attribute on the corresponding option)

#### Scenario: Checkbox from a property
- **WHEN** the JSP contains `<html:checkbox property="active"/>` and the form property `active = true`
- **THEN** `<input type="checkbox" name="active" checked="checked">` is output (the checked attribute by value)

### Requirement: Error and message output
`html:errors` SHALL render errors from the request/session (`ActionErrors` under the engine key): each error item is wrapped in `errors.prefix`/`errors.suffix`, the list in `errors.header`/`errors.footer` (by default `<UL>`/`<LI>`/`</LI>`/`</UL>`), the text — from the bundle by the message key with arguments; `property` filters by property. `html:messages` SHALL render arbitrary `ActionMessages` with the given header/footer/prefix/suffix and the `id` variable.

#### Scenario: Rendering an error list
- **WHEN** the request contains two errors (the keys `errors.required` with an argument and `errors.invalid`), and the JSP contains `<html:errors/>`
- **THEN** a `<UL>` with two `<LI>` items is output, the text of each — the message from the bundle with the substituted argument

#### Scenario: Errors of a specific property
- **WHEN** the JSP contains `<html:errors property="name"/>`
- **THEN** only the errors of the `name` property are output

### Requirement: URL building: img, link, rewrite
`html:link` SHALL build `<a href>` by `forward` (from global forwards), `action`, or `href` with the application context, adding parameters (`paramId`/`paramName` or nested `html:param`) and URL encoding; `html:rewrite` SHALL output only the URL (without an `<a>` element); `html:img` SHALL render an `<img>` with a context-aware URL and parameters; `html:base` SHALL render a `<base href>` per the current request; `html:html` SHALL render `<html>` with the `lang` attribute from the locale.

#### Scenario: Link by forward
- **WHEN** the global forward `welcome` leads to `/Welcome.do`, and the JSP contains `<html:link forward="welcome">Go</html:link>`
- **THEN** `<a href="/ctx/Welcome.do">Go</a>` is output (with the application context)

#### Scenario: Link with a parameter
- **WHEN** the JSP contains `<html:link href="/list.do" paramId="id" paramName="bean" paramProperty="key">…</html:link>` and the bean contains `key = 7`
- **THEN** `<a href="/ctx/list.do?id=7">…</a>` is output

### Requirement: Xhtml mode
With the xhtml mode enabled (the engine attribute / the `html:xhtml` tag), the library tags SHALL render valid XHTML: self-closing elements (`<input ... />`), value attributes as `selected="selected"`/`checked="checked"`, correct output of `javascript` content; when disabled — classic HTML output.

#### Scenario: Self-closing elements in xhtml
- **WHEN** the xhtml mode is enabled, and the JSP contains `<html:text property="name"/>`
- **THEN** `<input type="text" name="name" value="..."/>` is output (a self-closing element)

### Requirement: Deferred validator tag
The `html:javascript` tag (generation of commons-validator scripts) SHALL be deferred to the validation phase (phase 3): the TLD declares it, but there is no implementation in this phase.

#### Scenario: Using the javascript tag
- **WHEN** a JSP uses `<html:javascript formName="..."/>`
- **THEN** JSP processing ends with an error indicating an unimplemented tag (the validator — phase 3)
