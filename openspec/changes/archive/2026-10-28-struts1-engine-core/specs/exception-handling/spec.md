# Spec Delta

## Purpose

Reproduces Struts 1 declarative exception handling: handlers from `global-exceptions` and from a mapping turn an exception into a forward with a message; undeclared exceptions go to the container.

## ADDED Requirements

### Requirement: Handling declared exceptions
An exception whose type is declared in `global-exceptions` or in the mapping's `exception` SHALL be handled by the default handler: control passes to the declaration's `path`, and the message (by the `key` from the bundle) SHALL be available in the request as Struts errors.

#### Scenario: Exception declared globally
- **WHEN** an action's execute throws an exception whose type is declared in `global-exceptions` with `path="/pages/Error.jsp"` and `key="errors.detail"`
- **THEN** the client receives the `/pages/Error.jsp` page, and the message for the key `errors.detail` is available in the request

#### Scenario: Subtype of a declared exception
- **WHEN** execute throws an exception that is a subtype of a type declared in the configuration
- **THEN** it is handled by the same declaration

### Requirement: Local handler takes priority over the global one
On a type match, the local `exception` declaration in the mapping SHALL be used instead of the global one.

#### Scenario: Declaration conflict
- **WHEN** an exception type is declared both globally and in the mapping, and the exception is thrown by that action
- **THEN** the path and key of the local declaration apply

### Requirement: Undeclared exceptions are propagated to the container
An exception whose type is declared neither globally nor in the mapping SHALL be propagated to the servlet container without interception by the engine.

#### Scenario: Exception without a declaration
- **WHEN** execute throws an exception that is not declared in the configuration
- **THEN** the container receives this exception and handles it as an application error
