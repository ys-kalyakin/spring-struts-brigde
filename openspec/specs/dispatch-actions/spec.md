# dispatch-actions Specification

## Purpose

The dispatch-action family (`org.apache.struts.extras.actions` of the reference): one action class handles several operations, selecting a method based on request data.

## Requirements

### Requirement: Method selection by parameter
`DispatchAction` SHALL select the processing method by the value of the parameter whose name is given by the `parameter` attribute of the mapping: a request with `?method=save` invokes the `save(...)` method with the same signature as `execute`, and its result is processed as an ordinary action result.

#### Scenario: Dispatch by method
- **WHEN** the mapping declares `parameter="method"`, and the request contains `?method=save`
- **THEN** the action's `save` method is invoked and its result is processed as a forward

#### Scenario: Unknown method
- **WHEN** the request passes a parameter value for which no method exists
- **THEN** processing fails with an error reporting that dispatch is not possible

### Requirement: Method selection by message key
`LookupDispatchAction` SHALL select the method by the submit button: the parameter value (button text) is translated into a message key via the `getKeyMethodMap()` map and the bundle, and the key into a method name; button texts come from message-resources.

#### Scenario: Dispatch by button
- **WHEN** the map associates key `button.save` with method `save`, the bundle contains `button.save=Save`, and the request is submitted with button parameter `Save`
- **THEN** method `save` is invoked

### Requirement: Method specified in the mapping
`MappingDispatchAction` SHALL invoke the method whose name is given in the `parameter` attribute of the mapping: several mappings onto one class lead to different methods.

#### Scenario: Two mappings onto one class
- **WHEN** mappings `/edit` and `/save` point to one class with `parameter="edit"` and `parameter="save"`
- **THEN** request `/edit.do` invokes method `edit`, request `/save.do` — method `save`

### Requirement: Event dispatching
`EventDispatchAction`, as well as `ActionDispatcher`/`EventActionDispatcher` (dispatching for actions that do not extend DispatchAction) SHALL select the method by the event name in request parameters: a parameter whose name is listed in the `parameter` attribute and is present in the request determines the method (with support for prefix schemes and the default value `default`).

#### Scenario: Dispatch by event
- **WHEN** the mapping declares `parameter="save,cancel,default"`, and the request contains parameter `save`
- **THEN** method `save` is invoked

#### Scenario: Dispatcher without inheritance
- **WHEN** an action does not extend DispatchAction and uses `ActionDispatcher` with `parameter="method"`
- **THEN** the method is selected by the request parameter just as in DispatchAction

### Requirement: Default method
If the dispatch parameter is absent from the request, a dispatch action SHALL invoke the `unspecified(...)` method if it is defined; otherwise — a dispatch error.

#### Scenario: Request without the method parameter
- **WHEN** the request does not contain the `method` parameter, and the action defines `unspecified(...)`
- **THEN** method `unspecified` is invoked
