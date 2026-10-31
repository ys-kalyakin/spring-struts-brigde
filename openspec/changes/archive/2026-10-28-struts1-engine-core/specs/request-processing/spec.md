# Spec Delta

## Purpose

Routes requests to Struts actions by suffix mapping and reproduces the Struts 1 request lifecycle — populate → validate → execute → forward — with forward and form semantics.

## ADDED Requirements

### Requirement: Suffix routing of actions
A request to an action SHALL be routed by the path without the suffix: a request to `/x.do` is handled by the mapping with `path="/x"`. The suffix SHALL be configurable; when the configured suffix is changed, routing is performed by it.

#### Scenario: Request with the .do suffix
- **WHEN** a `GET /Welcome.do` request arrives and the configuration has a mapping with `path="/Welcome"` registered
- **THEN** the request is handled by that mapping

#### Scenario: Configured suffix
- **WHEN** the suffix is configured as `.act` and a `GET /Welcome.act` request arrives
- **THEN** the request is handled by the mapping with `path="/Welcome"`

### Requirement: Unregistered path returns 404
A request to a path for which no mapping is registered SHALL produce a 404 response.

#### Scenario: Unknown action
- **WHEN** a `GET /Nope.do` request arrives and the `/Nope` mapping is not registered
- **THEN** the client receives a 404 response

### Requirement: Form populate from request parameters
If the mapping declares a form, its properties SHALL be populated with request parameters with commons-beanutils semantics before the action is invoked; a mapping without a form SHALL be handled without populate.

#### Scenario: Parameters populate form properties
- **WHEN** a POST arrives with parameter names matching the properties of the form declared in the mapping
- **THEN** by the time the action is invoked, the form properties contain the parameter values

#### Scenario: Mapping without a form
- **WHEN** the mapping does not declare a `name` attribute with a form
- **THEN** populate is not performed and the action is invoked immediately

### Requirement: Form placement by scope
The form SHALL be placed in the scope given by the mapping's `scope` attribute (request or session) under the form-bean name from the configuration.

#### Scenario: Form in request
- **WHEN** the mapping declares `scope="request"` and a form is created for the request
- **THEN** the form is available to the subsequent page as a request attribute

#### Scenario: Form in session
- **WHEN** the mapping declares `scope="session"` and a form is created for the request
- **THEN** the form is available as a session attribute and persists between requests within the session

### Requirement: Validation by the validate flag
With `validate="true"`, form validation SHALL run before the action: detected errors lead to a transition to the `input` page without invoking the action, and the errors SHALL be available in the request. With `validate="false"`, validation SHALL not run.

#### Scenario: Validation errors lead to input
- **WHEN** the mapping declares `validate="true"` with `input="/pages/Input.jsp"` and the form's validate method returned errors
- **THEN** the action is not invoked, the client receives the `input` page, and the errors are available in the request

#### Scenario: Successful validation
- **WHEN** the mapping declares `validate="true"` and the form's validate returned without errors
- **THEN** the action is invoked after validation

#### Scenario: Validation disabled
- **WHEN** the mapping declares `validate="false"`
- **THEN** the form's validate is not called and the action executes

### Requirement: Action execution and forward
The result of the action's execute method SHALL control the transition: the returned forward is rendered to the client; a mapping with the `forward` or `include` attribute SHALL be handled as a direct transition to the specified resource without invoking an action.

#### Scenario: Forward from execute
- **WHEN** the action's execute returns a forward with `path="/pages/Done.jsp"`
- **THEN** the client receives the content of the resource at that path

#### Scenario: Mapping with the forward attribute
- **WHEN** the mapping declares `path="/Welcome"` with `forward="/pages/Welcome.jsp"` and does not declare an action type
- **THEN** the request is handled by a transition to `/pages/Welcome.jsp` without invoking the action method

### Requirement: Local forwards take priority over global ones
On a name match, a mapping's local forward SHALL be used instead of the global forward with the same name.

#### Scenario: Forward name conflict
- **WHEN** a global and a local forward are declared with the same name, and the action returns a forward by that name
- **THEN** the path of the local forward is used

### Requirement: Redirect and dispatch forwards
A forward with `redirect="true"` SHALL be executed as an HTTP redirect (a new client request); a forward with `redirect="false"` (the default) SHALL be executed as a dispatch within the container in the same request.

#### Scenario: Redirect
- **WHEN** a forward declares `redirect="true"` with the path `/Done.do`
- **THEN** the client receives an HTTP redirect to that path

#### Scenario: Dispatch
- **WHEN** a forward declares `redirect="false"` with the path `/pages/Done.jsp`
- **THEN** the client receives the JSP content in the response of the same request
