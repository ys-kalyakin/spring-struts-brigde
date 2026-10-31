# Spec Delta: spring-bean-integration

## MODIFIED Requirements

### Requirement: Actions as singleton beans
An action SHALL be a Spring bean in a single instance: a type declared in the configuration is automatically registered in the context as a singleton unless the user has explicitly registered that class. The same instance is reused across requests, and dependencies are injected regardless of annotations.

#### Scenario: Action instance reuse
- **WHEN** two requests are handled by one action
- **THEN** both requests are served by the same instance

#### Scenario: Dependency injection into an action
- **WHEN** the action declares a dependency on a Spring bean
- **THEN** the dependency is injected and available in the execute method

#### Scenario: An unannotated action becomes a Spring bean
- **WHEN** a mapping declares an action class without annotations and without user registration
- **THEN** the class is automatically registered in the context as a singleton, the instance is created by Spring, and dependencies are injected

### Requirement: Forms as prototype beans
A form SHALL be a Spring bean that creates a new instance per request: a type declared in the configuration is automatically registered in the context as a prototype unless the user has explicitly registered that class. Form state is not shared between requests, and dependencies are injected into the created instance regardless of annotations.

#### Scenario: A new instance per request
- **WHEN** two requests use one form
- **THEN** each request gets its own form instance, and the first request's state is not visible to the second

#### Scenario: Dependency injection into a form
- **WHEN** the form declares a dependency on a Spring bean
- **THEN** the dependency is injected into the instance created for the request

#### Scenario: An unannotated form becomes a Spring bean
- **WHEN** a mapping declares a form whose class is not annotated and not registered by the user
- **THEN** the class is automatically registered in the context as a prototype, the instance is created by Spring, dependencies are injected, and populate proceeds as before

## ADDED Requirements

### Requirement: Explicit registration takes priority
If a form or action class is explicitly registered by the user in Spring (component scan, Java or XML configuration), the engine SHALL use the user's bean definition as is: auto-registration does not replace it, and the user bean's scope and name are respected.

#### Scenario: The user's action bean takes precedence
- **WHEN** an action class is registered by the user as a bean, and the same class is declared in struts-config
- **THEN** requests are served by the user's bean, not by an automatically registered copy

#### Scenario: The user's form scope is respected
- **WHEN** the user registered a form with session scope, and the mapping declares scope session
- **THEN** a single instance of the user's bean is used in the session

### Requirement: Spring bean lifecycle
Form and action instances SHALL go through the full Spring bean lifecycle: `@PostConstruct` callbacks are invoked before the instance is first used, `BeanPostProcessor`s are applied, and `@PreDestroy` callbacks are invoked at context shutdown for singleton instances.

#### Scenario: PostConstruct before the first execute
- **WHEN** the action declares a `@PostConstruct` callback
- **THEN** the callback is invoked before the first execution of the execute method, and once per bean lifetime

#### Scenario: PreDestroy at context shutdown
- **WHEN** the application context is closed
- **THEN** the `@PreDestroy` callback is invoked for singleton action instances

## REMOVED Requirements

### Requirement: Direct-creation fallback
**Reason**: Creating instances directly, bypassing Spring, contradicts the goal of integration — forms and actions must be real Spring beans with dependency injection and a lifecycle.
**Migration**: Application behavior is preserved: a class without annotations is automatically registered in Spring and created by the bean factory (the classic Struts semantics — a singleton action, a prototype form — are reproduced by bean scopes). Explicit user registration takes priority.
