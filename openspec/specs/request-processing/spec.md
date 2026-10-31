# request-processing Specification

## Purpose

Routes requests to Struts actions by suffix mapping and reproduces the Struts 1 request lifecycle — populate → validate → execute → forward — with forwards and forms semantics.

## Requirements

### Requirement: Suffix-based action routing
A request to an action SHALL be routed by the path without the suffix: a request to `/x.do` is handled by the mapping with `path="/x"`. The suffix SHALL be configurable; when the configured suffix is changed, routing is performed by it. The mapping SHALL be looked up in the module selected by the prefix of the request path; for a request without a module prefix — in the default module.

#### Scenario: Request with the .do suffix
- **WHEN** a request `GET /Welcome.do` arrives and the configuration registers a mapping `path="/Welcome"`
- **THEN** the request is handled by that mapping

#### Scenario: Configured suffix
- **WHEN** the suffix is configured as `.act` and a request `GET /Welcome.act` arrives
- **THEN** the request is handled by the mapping `path="/Welcome"`

#### Scenario: Request to a module
- **WHEN** a request `GET /exercise/bean-write.do` arrives and the module `/exercise` is registered with a mapping `path="/bean-write"`
- **THEN** the request is handled by the `/bean-write` mapping of the `/exercise` module

### Requirement: Unregistered path returns 404
A request to a path with no registered mapping SHALL produce a 404 response.

#### Scenario: Unknown action
- **WHEN** a request `GET /Nope.do` arrives and the mapping `/Nope` is not registered
- **THEN** the client receives a 404 response

### Requirement: Form populate from request parameters
If the mapping declares a form, its properties SHALL be populated from request parameters with commons-beanutils semantics, reproduced by the engine on Spring `ConversionService` (conversion rules are defined in the `form-populate` spec); a mapping without a form SHALL be handled without populate. A multipart request SHALL populate the form from the parsed multipart elements (text elements as parameters, files into properties of the matching types) before the action is invoked; parsing behavior and limit-violation handling SHALL conform to the `multipart-upload` spec.

#### Scenario: Parameters populate form properties
- **WHEN** a POST arrives with parameters whose names match the properties of the form declared in the mapping
- **THEN** by the time the action is invoked, the form properties contain the parameter values

#### Scenario: Mapping without a form
- **WHEN** the mapping does not declare the `name` attribute with a form
- **THEN** populate is not performed and the action is invoked immediately

#### Scenario: Multipart request populates a file property
- **WHEN** a POST `multipart/form-data` with a file field arrives at a mapping with a form containing a `FormFile` property
- **THEN** by the time the action is invoked, the property contains the parsed file, and the form's text fields are populated from the multipart elements

### Requirement: Form placement by scope
The form SHALL be placed in the scope given by the mapping's `scope` attribute (request or session), under the form-bean name from the configuration.

#### Scenario: Form in request
- **WHEN** the mapping declares `scope="request"` and the form is created for the request
- **THEN** the form is available to the subsequent page as a request attribute

#### Scenario: Form in session
- **WHEN** the mapping declares `scope="session"` and the form is created for the request
- **THEN** the form is available as a session attribute and persists between requests within the session

### Requirement: Validation by the validate flag
With `validate="true"`, form validation SHALL run before the action: detected errors lead to a transition to the `input` page without invoking the action, and the errors SHALL be available in the request. With `validate="false"`, validation SHALL NOT run.

#### Scenario: Validation errors go to input
- **WHEN** the mapping declares `validate="true"` with `input="/pages/Input.jsp"` and the form's validate method returned errors
- **THEN** the action is not invoked, the client receives the `input` page, and the errors are available in the request

#### Scenario: Successful validation
- **WHEN** the mapping declares `validate="true"` and the form's validate returned no errors
- **THEN** the action is invoked after validation

#### Scenario: Validation disabled
- **WHEN** the mapping declares `validate="false"`
- **THEN** the form's validate is not called and the action executes

### Requirement: Action execution and forward
The result of the action's execute method SHALL control the transition: the returned forward is rendered to the client; a mapping with the `forward` or `include` attribute SHALL be handled as a direct transition to the specified resource without invoking the action. A forward whose path matches a tiles-definition name SHALL render the tile definition instead of the JSP path (behavior defined in the `struts-tiles` spec); a path that is not a definition SHALL be handled as a regular forward.

#### Scenario: Forward from execute
- **WHEN** the action's execute returns a forward with `path="/pages/Done.jsp"`
- **THEN** the client receives the content of the resource at that path

#### Scenario: Mapping with a forward attribute
- **WHEN** the mapping declares `path="/Welcome"` with `forward="/pages/Welcome.jsp"` and does not declare an action type
- **THEN** the request is handled by a transition to `/pages/Welcome.jsp` without invoking the action method

#### Scenario: Forward to a tiles-definition
- **WHEN** the action's execute returns a forward whose path matches a definition name from the tiles defs
- **THEN** the client receives the page assembled from that definition

### Requirement: Local forwards take priority over global ones
On a name match, the mapping's local forward SHALL be used instead of a global forward with the same name.

#### Scenario: Forward name conflict
- **WHEN** a global and a local forward are declared with the same name, and the action returns a forward by that name
- **THEN** the local forward's path is used

### Requirement: Redirect and dispatch forwards
A forward with `redirect="true"` SHALL be executed as an HTTP redirect (a new client request); a forward with `redirect="false"` (the default) SHALL be executed as a dispatch within the container in the same request.

#### Scenario: Redirect
- **WHEN** a forward declares `redirect="true"` with the path `/Done.do`
- **THEN** the client receives an HTTP redirect to that path

#### Scenario: Dispatch
- **WHEN** a forward declares `redirect="false"` with the path `/pages/Done.jsp`
- **THEN** the client receives the JSP content in the response of the same request

### Requirement: Module selection by request path
The module for handling a request SHALL be selected by path prefix: from the registered modules, the one whose prefix matches the beginning of the path after the context is taken; if there is no match — the default module. The selected module SHALL be available to request processing.

#### Scenario: Module prefix
- **WHEN** the default module and the module `/exercise` are registered, and a request `GET /exercise/html-form.do` arrives
- **THEN** the request is handled in the `/exercise` module: mappings and forms are taken from its configuration

#### Scenario: Path without a module prefix
- **WHEN** the module `/exercise` is registered, and a request `GET /Welcome.do` arrives
- **THEN** the request is handled in the default module

### Requirement: Path-mapped actions
Along with suffix mapping, path-mapped SHALL be supported: with a servlet mapping of the form `/action/*`, a request to `/action/x` is handled by the mapping with `path="/x"` (the `/action` prefix is stripped).

#### Scenario: Request via prefix mapping
- **WHEN** the servlet mapping is configured as `/action/*` and a request `GET /action/Welcome` arrives
- **THEN** the request is handled by the mapping `path="/Welcome"`
