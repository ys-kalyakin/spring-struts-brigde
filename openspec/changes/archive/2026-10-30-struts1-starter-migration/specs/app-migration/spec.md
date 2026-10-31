# Spec Delta: app-migration

## Purpose

The migration path of an existing Struts 1 application to the engine via the starter: the minimal set of steps for a working migration, verified by the annotated demo application and the set of test applications; a migration guide, a compatibility matrix, and artifact publishing infrastructure.

## ADDED Requirements

### Requirement: Application migration to the starter
The applications from the project's set of test applications SHALL run via the starter: each application starts its own Spring Boot configuration (an application class), and the engine is configured by the starter's auto-configuration rather than a manual import. All application functionality — pages, forms, validation, tiles, messages, upload — SHALL be preserved without changes to struts-config.xml and JSPs. Application integration tests SHALL run on a real embedded Tomcat against the built starter.

#### Scenario: Application set on the starter
- **WHEN** the built starter is added to each test application and the applications are deployed to embedded Tomcat
- **THEN** each application starts via the starter's auto-configuration and all of its integration scenarios pass

#### Scenario: Functional completeness after migration
- **WHEN** an application has been migrated to the starter and its integration tests are run
- **THEN** the page, form, validation, tiles, and message scenarios produce the same results as before the migration

### Requirement: Annotated demo migration
The demo application SHALL work as an executable Spring Boot application without web.xml: the migration is expressed by swapping the dependency to `spring-struts1-starter`, adding the starter, and annotating actions and forms with `@StrutsAction`/`@StrutsForm`. Annotated actions SHALL be singleton Spring beans with dependency injection, annotated forms prototype beans; the rest of the configuration — struts-config.xml, JSPs, message-resources — SHALL work as before.

#### Scenario: Running the demo as a Boot application
- **WHEN** the demo application is run as a Spring Boot application (embedded container)
- **THEN** pages render, forms are filled and validated, messages are resolved by locale, and forwards work

#### Scenario: Annotated beans in the demo
- **WHEN** the demo's action and form are annotated `@StrutsAction`/`@StrutsForm` and the action declares a dependency via `@Autowired`
- **THEN** the action is a singleton reused across requests with the dependency injected; each request receives a separate form instance

### Requirement: Migration guide
The project SHALL contain a migration guide describing all known manual steps: replacing the Struts 1 artifact with the engine dependencies, adding the starter, annotating forms and actions, typical adjustments (Jasper for JSP, starter properties, static resources) — with a reference to the demo application as the reference implementation.

#### Scenario: Migration following the guide
- **WHEN** a developer follows the guide's steps for their application
- **THEN** no required step is missing from the guide, and every typical manual step is documented

### Requirement: Compatibility matrix
The project SHALL publish a compatibility matrix: supported Spring Boot versions (4.0.x), Java (17+), Spring Framework versions, servlet container (Tomcat 11, jakarta servlet 6.1), and the set of modules with their artifacts.

#### Scenario: Choosing versions from the matrix
- **WHEN** a developer chooses versions for migrating their application
- **THEN** the matrix in the documentation unambiguously specifies the supported Boot, Java, and container versions

### Requirement: Publishing infrastructure
The repository SHALL contain ready-to-use publishing infrastructure for Maven artifacts: a publication profile with the target repository configured via properties and documentation on running deploy. The infrastructure SHALL be verified by local publication to a file repository; the external publishing channel (credentials, repository) SHALL be documented as configurable outside the repository.

#### Scenario: Local publication of artifacts
- **WHEN** publication to a file repository is run with the profile
- **THEN** all modules are published with their POMs and are available for consumption as dependencies

#### Scenario: Consuming published artifacts
- **WHEN** an external application consumes the published engine and starter artifacts
- **THEN** the dependencies resolve and the application builds
