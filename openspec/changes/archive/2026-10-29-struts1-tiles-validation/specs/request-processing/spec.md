# Spec Delta: request-processing

## MODIFIED Requirements

### Requirement: Form populate from request parameters
If a mapping declares a form, its properties SHALL be populated from the request parameters with commons-beanutils semantics before the action is invoked; a mapping without a form SHALL be processed without populate. A multipart request SHALL populate the form from the parsed multipart elements (text elements — as parameters, file ones — into properties of the corresponding types) before the action is invoked; the parsing behavior and limit-excess handling SHALL follow the `multipart-upload` spec.

#### Scenario: Parameters populate the form properties
- **WHEN** a POST arrives with parameters whose names match the properties of the form declared in the mapping
- **THEN** by the time the action is invoked, the form properties contain the parameter values

#### Scenario: A mapping without a form
- **WHEN** the mapping does not declare a `name` attribute with a form
- **THEN** no populate runs and the action is invoked immediately

#### Scenario: A multipart request populates the file property
- **WHEN** a `multipart/form-data` POST with a file field arrives at a mapping with a form containing a `FormFile` property
- **THEN** by the time the action is invoked, the property contains the parsed file, and the form's text fields are populated from the multipart elements

### Requirement: Action execution and forward
The result of the action's execute method SHALL control the transition: the returned forward is rendered to the client; a mapping with the `forward` or `include` attribute SHALL be processed as a direct transition to the specified resource without invoking an action. A forward whose path matches a tiles-definition name SHALL render the tile definition instead of the JSP path (the behavior is defined in the `struts-tiles` spec); a path that is not a definition SHALL be processed as a regular forward.

#### Scenario: A forward from execute
- **WHEN** the action's execute returns a forward with `path="/pages/Done.jsp"`
- **THEN** the client receives the content of the resource at that path

#### Scenario: A mapping with the forward attribute
- **WHEN** a mapping declares `path="/Welcome"` with `forward="/pages/Welcome.jsp"` and does not declare an action type
- **THEN** the request is processed by transitioning to `/pages/Welcome.jsp` without invoking the action method

#### Scenario: A forward to a tiles-definition
- **WHEN** the action's execute returns a forward with a path matching a definition name from tiles-defs
- **THEN** the client receives the page assembled from that definition
