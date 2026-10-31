# logic-taglib Specification

## Purpose

The `struts-logic` tag library (URI `http://struts.apache.org/tags-logic`): iteration over collections, comparisons, matching, presence and emptiness checks, redirect/forward transitions — with the TLD, names and behavior of the weblegacy reference.

## Requirements

### Requirement: TLD, URI and tag name identity
The library TLD SHALL be identical to the weblegacy reference (`struts-logic.tld`, 17 tags): the same URI `http://struts.apache.org/tags-logic`, the same tag and attribute names; the JSP declaration — `<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>`.

#### Scenario: JSP inclusion and compilation
- **WHEN** a JSP declares the library by URI `http://struts.apache.org/tags-logic` and uses `logic:iterate`
- **THEN** the JSP compiles and the tag is processed without additional declarations

### Requirement: Iteration via logic:iterate
`logic:iterate` SHALL repeat the body for each element of a collection, array, map or `Enumeration` (by `name`/`property` or a nested set), defining the `id` variable with the current element (and `indexId` with the index); the `offset` and `length` attributes restrict the range.

#### Scenario: Iteration over a collection
- **WHEN** the scope holds a list of three strings, and the JSP contains `<logic:iterate id="item" name="list"><bean:write name="item"/>;</logic:iterate>`
- **THEN** the body is rendered three times with the element values separated by semicolons

#### Scenario: Iteration index
- **WHEN** the JSP contains `<logic:iterate id="item" name="list" indexId="i">…</logic:iterate>`
- **THEN** variable `i` holds 0, 1, 2 across the iterations

### Requirement: Value comparisons
Tags `equal`, `notEqual`, `greaterEqual`, `lessEqual`, `greaterThan`, `lessThan` SHALL process the body when the value (from `name`/`property`, `parameter`, `cookie` or `header`) compares with `value` in the specified way; non-numeric values are compared as strings.

#### Scenario: Request parameter equality
- **WHEN** the request contains parameter `page=3`, and the JSP contains `<logic:equal parameter="page" value="3">shown</logic:equal>`
- **THEN** the body is rendered

#### Scenario: Property inequality
- **WHEN** the bean property `status = "closed"`, and the JSP contains `<logic:notEqual name="bean" property="status" value="open">shown</logic:notEqual>`
- **THEN** the body is rendered

### Requirement: String matching
Tags `match` and `notMatch` SHALL process the body when the value matches (or does not match) `value`; the `location` attribute restricts the check to the beginning (`start`) or end (`end`) of the string.

#### Scenario: Match at the start of a string
- **WHEN** the bean property `path = "/admin/users"`, and the JSP contains `<logic:match name="bean" property="path" value="/admin" location="start">shown</logic:match>`
- **THEN** the body is rendered

### Requirement: Presence and emptiness checks
Tags `present`/`notPresent` SHALL process the body depending on the presence of a bean (by `name` in a scope or `property`), a request parameter (`parameter`), a header (`header`), a cookie (`cookie`) or a configuration attribute; tags `empty`/`notEmpty` — by emptiness of the value (null, empty string, empty collection); `messagesPresent`/`messagesNotPresent` — by the presence of messages/errors in the request (with the `property` filter).

#### Scenario: Presence of an application attribute
- **WHEN** the JSP contains `<logic:notPresent name="org.apache.struts.action.MESSAGE" scope="application">ERROR</logic:notPresent>` (as in the reference's Welcome.jsp)
- **THEN** with message-resources loaded the body is not rendered, and without them — it is rendered

#### Scenario: Empty value
- **WHEN** the bean property `note = ""`, and the JSP contains `<logic:empty name="bean" property="note">shown</logic:empty>`
- **THEN** the body is rendered

### Requirement: redirect and forward transitions
`logic:redirect` SHALL perform an HTTP redirect to a resource by `forward`, `action` or `href` (with parameters and encoding); `logic:forward` SHALL perform the controller forward by the name of a global (or local) forward.

#### Scenario: Redirect by global forward
- **WHEN** the global forward `welcome` leads to `/Welcome.do`, and the JSP (index.jsp) contains `<logic:redirect forward="welcome"/>`
- **THEN** the client receives an HTTP redirect to `/ctx/Welcome.do`

#### Scenario: Forward by name
- **WHEN** the JSP contains `<logic:forward name="welcome"/>`
- **THEN** processing is passed to the resource of the global forward `welcome`
