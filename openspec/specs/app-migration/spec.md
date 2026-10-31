# app-migration Specification

## Purpose
A migration path for an existing Struts 1 application onto the engine via the starter: a minimal set of steps for a working migration, proven by the demo application with annotations and the set of test applications; a migration guide, a compatibility matrix and artifact publishing infrastructure.

## Requirements

### Requirement: Migrating applications to the starter
Applications from the project's set of test applications SHALL run through the starter: each application starts its own Spring Boot configuration (the application class), and the engine is configured by the starter's auto-configuration rather than manual imports. All application functionality — pages, forms, validation, tiles, messages, upload — SHALL be preserved without changes to struts-config.xml and JSP. Application integration tests SHALL run on a real embedded Tomcat against the assembled starter.

#### Scenario: Set of applications on the starter
- **WHEN** the assembled starter is attached to each test application and the applications are deployed to embedded Tomcat
- **THEN** each application starts through the starter's auto-configuration and all of its integration scenarios pass

#### Scenario: Functional completeness after migration
- **WHEN** an application is migrated to the starter and its integration tests run
- **THEN** the page, form, validation, tiles and message scenarios produce the same results as before the migration

### Requirement: Demo migration with annotations
The demo application SHALL run as an executable Spring Boot application without web.xml: migration is expressed by replacing the dependency with `spring-struts1-starter`, adding the starter, and `@StrutsAction`/`@StrutsForm` annotations over actions and forms. Annotated actions SHALL be singleton Spring beans with dependency injection, annotated forms — prototype beans; the rest of the configuration — struts-config.xml, JSP, message-resources — SHALL work as before.

#### Scenario: Running the demo as a Boot application
- **WHEN** the demo application is run as a Spring Boot application (embedded container)
- **THEN** pages render, forms are populated and validated, messages are resolved by locale, forwards work

#### Scenario: Annotated beans in the demo
- **WHEN** the demo's action and form are marked with `@StrutsAction`/`@StrutsForm`, and the action declares a dependency via `@Autowired`
- **THEN** the action is a singleton, reused across requests, with the dependency injected; each request receives a separate form instance

### Requirement: Migration guide
The project SHALL contain a migration guide describing all known manual steps: replacing the Struts 1 artifact with engine dependencies, adding the starter, annotating forms and actions, typical adjustments (Jasper for JSP, starter properties, static resources) — with a reference to the demo application as the model.

#### Scenario: Migration per the guide
- **WHEN** a developer follows the guide's steps for their application
- **THEN** no required step is missing from the guide, and every typical manual step is documented

### Requirement: Compatibility matrix
The project SHALL publish a compatibility matrix: supported Spring Boot versions (4.0.x), Java (17+), Spring Framework versions, servlet container (Tomcat 11, jakarta servlet 6.1) and the set of modules with their artifacts.

#### Scenario: Choosing versions via the matrix
- **WHEN** a developer chooses versions for migrating their application
- **THEN** the matrix in the documentation unambiguously defines the supported Boot, Java and container versions

### Requirement: Publishing infrastructure
The repository SHALL contain ready-to-use infrastructure for publishing Maven artifacts: a publishing profile with the target repository configured via properties and documentation on running deploy. The infrastructure SHALL be verified by local publishing to a file repository; the external publishing channel (credentials, repository) SHALL be documented as configurable outside the repository.

#### Scenario: Local artifact publishing
- **WHEN** publishing with the profile to a file repository is run
- **THEN** all modules are published with their POMs and are available for consumption as dependencies

#### Scenario: Consuming published artifacts
- **WHEN** an external application consumes the published engine and starter artifacts
- **THEN** the dependencies resolve and the application builds
