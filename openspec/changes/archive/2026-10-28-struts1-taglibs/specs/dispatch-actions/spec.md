# Spec Delta

## Purpose

The dispatch action family (`org.apache.struts.extras.actions` of the reference): a single action class handles several operations, selecting the method from request data.

## ADDED Requirements

### Requirement: Method selection by parameter
`DispatchAction` SHALL select the processing method by the value of the parameter whose name is given by the mapping's `parameter` attribute: a request with `?method=save` invokes the method `save(...)` with the same signature as `execute`, and its result is processed as a regular action result.

#### Scenario: Dispatch by method
- **WHEN** the mapping declares `parameter="method"`, and the request contains `?method=save`
- **THEN** the action's `save` method is invoked and its result is processed as a forward

#### Scenario: Unknown method
- **WHEN** the request passes a parameter value for which no method exists
- **THEN** processing ends with an error reporting that dispatching is not possible

### Requirement: Method selection by message key
`LookupDispatchAction` SHALL select the method by the submit button: the parameter value (the button text) is translated into a message key via the `getKeyMethodMap()` map and the bundle, and the key into the method name; the button texts are taken from message-resources.

#### Scenario: Dispatch by button
- **WHEN** the map associates the key `button.save` with the method `save`, the bundle contains `button.save=Save`, and the request is submitted with the button parameter `Save`
- **THEN** the `save` method is invoked

### Requirement: Method specified in the mapping
`MappingDispatchAction` SHALL invoke the method whose name is given by the mapping's `parameter` attribute: several mappings to one class lead to different methods.

#### Scenario: Two mappings to one class
- **WHEN** the mappings `/edit` and `/save` point to one class with `parameter="edit"` and `parameter="save"`
- **THEN** a request to `/edit.do` invokes the `edit` method, a request to `/save.do` — the `save` method

### Requirement: Event-based dispatch
`EventDispatchAction`, as well as `ActionDispatcher`/`EventActionDispatcher` (dispatching for actions that do not extend DispatchAction) SHALL select the method by the event name in the request parameters: a parameter whose name is listed in the `parameter` attribute and is present in the request determines the method (with support for prefix schemes and the default value `default`).

#### Scenario: Dispatch by event
- **WHEN** the mapping declares `parameter="save,cancel,default"`, and the request contains the parameter `save`
- **THEN** the `save` method is invoked

#### Scenario: Dispatcher without inheritance
- **WHEN** an action does not extend DispatchAction and uses `ActionDispatcher` with `parameter="method"`
- **THEN** the method is selected by the request parameter just as in DispatchAction

### Requirement: Default method
If the dispatch parameter is absent from the request, a dispatch action SHALL invoke the `unspecified(...)` method, if defined; otherwise — a dispatch error.

#### Scenario: Request without a method parameter
- **WHEN** the request does not contain the `method` parameter, and the action defines `unspecified(...)`
- **THEN** the `unspecified` method is invoked
