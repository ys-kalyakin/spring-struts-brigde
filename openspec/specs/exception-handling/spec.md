# exception-handling Specification

## Purpose

Reproduces Struts 1 declarative exception handling: handlers from `global-exceptions` and from the mapping translate an exception into a forward with a message; undeclared exceptions propagate to the container.

## Requirements

### Requirement: Handling declared exceptions
An exception whose type is declared in `global-exceptions` or in the mapping's `exception` SHALL be handled by the default handler: control passes to the declaration's `path`, and the message (by `key` from the bundle) SHALL be available in the request like Struts errors.

#### Scenario: Exception declared globally
- **WHEN** an action's execute throws an exception whose type is declared in `global-exceptions` with `path="/pages/Error.jsp"` and `key="errors.detail"`
- **THEN** the client receives the page `/pages/Error.jsp`, and the message by key `errors.detail` is available in the request

#### Scenario: Subtype of a declared exception
- **WHEN** execute throws an exception that is a subclass of a type declared in the configuration
- **THEN** it is handled by the same declaration

### Requirement: Local handler takes precedence over global
On a type match, a local `exception` declaration in the mapping SHALL be used instead of the global one.

#### Scenario: Declaration conflict
- **WHEN** an exception type is declared both globally and in the mapping, and the exception is thrown by this action
- **THEN** the path and key of the local declaration apply

### Requirement: Undeclared exceptions propagate to the container
An exception whose type is declared neither globally nor in the mapping SHALL be propagated to the servlet container without interception by the engine.

#### Scenario: Exception without a declaration
- **WHEN** execute throws an exception not declared in the configuration
- **THEN** the container receives this exception and handles it as an application error
