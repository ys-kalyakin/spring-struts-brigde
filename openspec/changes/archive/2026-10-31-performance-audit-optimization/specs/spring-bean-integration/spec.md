# Spec Delta

## ADDED Requirements

### Requirement: Caching of bean resolution
The lookup of an action and form definition by type SHALL NOT scan all bean definitions of the context on every request: the type-to-bean-name match result SHALL be cached. Lifecycle semantics SHALL be preserved: the singleton action instance is reused across requests, while a new form instance is created per request; the priority of a user bean registration SHALL be preserved.

#### Scenario: Repeated request served by a single action instance
- **WHEN** two requests are processed by an action whose type is resolved from the context
- **THEN** the bean-name lookup by type is served from the cache, and the same singleton instance serves both requests

#### Scenario: Cache does not reuse form instances
- **WHEN** two requests use a request-scoped form
- **THEN** each request gets its own form instance; only the type-to-bean-name mapping is cached

#### Scenario: User registration takes priority when caching
- **WHEN** an action or form class is explicitly registered by the user as a bean
- **THEN** the cached resolution uses the user's bean definition and its scope
