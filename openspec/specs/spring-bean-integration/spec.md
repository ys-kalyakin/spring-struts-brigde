# spring-bean-integration Specification

## Purpose

Links Struts actions and forms with Spring: annotated classes become Spring beans with dependency injection, while classes not registered in Spring keep working through direct instantiation.

## Requirements

### Requirement: Actions as singleton beans
An action SHALL be a Spring bean in a single instance: the type declared in the configuration is automatically registered in the context as a singleton unless the user has registered that class explicitly. The same instance is reused across requests, and dependencies are injected regardless of the presence of annotations.

#### Scenario: Reuse of an action instance
- **WHEN** two requests are handled by the same action
- **THEN** both requests are served by the same instance

#### Scenario: Dependency injection into an action
- **WHEN** an action declares a dependency on a Spring bean
- **THEN** the dependency is injected and available in the execute method

#### Scenario: Action without annotations becomes a Spring bean
- **WHEN** a mapping declares an action class without annotations and without user registration
- **THEN** the class is automatically registered in the context as a singleton, the instance is created by Spring, and dependencies are injected

### Requirement: Forms as prototype beans
A form SHALL be a Spring bean that creates a new instance for every request: the type declared in the configuration is automatically registered in the context as a prototype unless the user has registered that class explicitly. Form state is not shared between requests, and dependencies are injected into the created instance regardless of the presence of annotations.

#### Scenario: New instance per request
- **WHEN** two requests use the same form
- **THEN** each request gets its own form instance, and the first request's state is not visible to the second

#### Scenario: Dependency injection into a form
- **WHEN** a form declares a dependency on a Spring bean
- **THEN** the dependency is injected into the instance created for the request

#### Scenario: Form without annotations becomes a Spring bean
- **WHEN** a mapping declares a form whose class is not annotated and not registered by the user
- **THEN** the class is automatically registered in the context as a prototype, the instance is created by Spring, dependencies are injected, and populate proceeds as before

### Requirement: Priority of explicit registration
If a form or action class is explicitly registered by the user in Spring (component scan, Java or XML configuration), the engine SHALL use the user's bean definition as is: auto-registration does not replace it, and the scope and name of the user's bean are respected — with one exception for forms: a user-registered form in singleton scope SHALL fail startup (see the no-singleton-form-beans requirement). Actions SHALL keep working as singletons.

#### Scenario: User's action bean takes priority
- **WHEN** an action class is registered by the user as a bean, and the same class is declared in struts-config
- **THEN** requests are served by the user's bean, not by an automatically registered copy

#### Scenario: User's form scope is respected
- **WHEN** the user has registered a form with session scope, and the mapping declares session scope
- **THEN** a single instance of the user's bean is used in the session

#### Scenario: User's singleton form is rejected
- **WHEN** the user has registered a form with singleton scope
- **THEN** startup fails instead of silently serving the same instance to every session

### Requirement: No singleton form beans
A form type explicitly registered by the user with singleton Spring scope SHALL fail application startup with an error naming the form type and the user's bean: a singleton form instance is shared across all sessions and users, which the form lifecycle (fresh state per request or per session) never assumes. Form beans in prototype, request, or session scope SHALL be accepted as before. Automatic engine registration of forms SHALL remain prototype.

#### Scenario: Singleton user form fails startup
- **WHEN** the user registers a form type as a plain Spring bean (singleton scope) and the type is declared as a form-bean in struts-config
- **THEN** application startup fails with an error identifying the form type and the singleton bean registration, instead of running with a cross-session shared instance

#### Scenario: Prototype user form is accepted
- **WHEN** the user registers a form type with prototype scope
- **THEN** startup succeeds and each request receives a fresh instance from that definition

#### Scenario: Session-scoped user form is accepted
- **WHEN** the user registers a form type with session scope and the mapping declares session scope
- **THEN** startup succeeds and a single instance of the user's bean is used in the session, with the serialized form lifecycle protection applied

### Requirement: Spring bean lifecycle
Form and action instances SHALL go through the full Spring bean lifecycle: `@PostConstruct` callbacks are invoked before the first use of the instance, `BeanPostProcessor`s are applied, and `@PreDestroy` callbacks are invoked at context shutdown for singleton instances.

#### Scenario: PostConstruct before the first execute
- **WHEN** an action declares a `@PostConstruct` callback
- **THEN** the callback is invoked before the first execution of the execute method and once per bean lifetime

#### Scenario: PreDestroy at context shutdown
- **WHEN** the application context is closed
- **THEN** the `@PreDestroy` callback is invoked for singleton action instances

### Requirement: Caching of bean resolution
Looking up the definition of an action and form by type SHALL not scan all context bean definitions on every request: the type → bean name match result SHALL be cached. Lifecycle semantics SHALL be preserved: a singleton action instance is reused across requests, a new form instance is created for every request; the priority of user bean registration SHALL be preserved.

#### Scenario: Repeated request served by one action instance
- **WHEN** two requests are handled by an action whose type is resolved from the context
- **THEN** the bean name lookup by type is served from the cache, and both requests are served by the same singleton instance

#### Scenario: Cache does not reuse form instances
- **WHEN** two requests use a form in request scope
- **THEN** each request gets its own form instance; only the type-to-bean-name match is cached

#### Scenario: User registration takes priority with caching
- **WHEN** an action or form class is explicitly registered by the user as a bean
- **THEN** the cached resolution uses the user's bean definition and its scope
