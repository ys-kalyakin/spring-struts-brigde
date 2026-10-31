# Spec Delta

## Purpose

Links Struts actions and forms with Spring: annotated classes become Spring beans with dependency injection, while classes not registered in Spring keep working through direct creation.

## ADDED Requirements

### Requirement: Actions as singleton beans
An action class marked `@StrutsAction` and registered in Spring SHALL exist as a single instance: the same instance is reused between requests.

#### Scenario: Action instance reuse
- **WHEN** two requests are handled by one action marked `@StrutsAction`
- **THEN** both requests are served by the same instance

#### Scenario: Dependency injection into an action
- **WHEN** an action with `@StrutsAction` declares a Spring bean dependency
- **THEN** the dependency is injected and available in the execute method

### Requirement: Forms as prototype beans
A form class marked `@StrutsForm` and registered in Spring SHALL produce a new instance for each request: form state is not shared between requests.

#### Scenario: New instance per request
- **WHEN** two requests use a form marked `@StrutsForm`
- **THEN** each request gets its own form instance, and the state of the first request is not visible to the second

#### Scenario: Dependency injection into a form
- **WHEN** a form with `@StrutsForm` declares a Spring bean dependency
- **THEN** the dependency is injected into the instance created for the request

### Requirement: Direct-creation fallback
A class not registered in Spring SHALL be created directly by the name from the configuration: an application without annotations works without Spring registration.

#### Scenario: Action without annotations
- **WHEN** the mapping declares an action type whose class is not marked with annotations and is not registered in Spring
- **THEN** the instance is created directly and the request is handled

#### Scenario: Form without annotations
- **WHEN** the mapping declares a form name whose class is not registered in Spring
- **THEN** the form instance is created directly and populated with request parameters
