# Proposal: struts1-starter-migration

## Why

Phases 1–3 delivered the engine, tag libraries, and tiles/validation, but every application still wires them up manually: a web.xml with the `DispatcherServlet` and classic init-params, an `AnnotationConfigWebApplicationContext`, and an explicit `@Import(StrutsEngineConfiguration, ...)`. The project vision is a drop-in replacement: migrating an existing Struts 1 application should come down to swapping the dependency, adding the starter, and annotating forms/actions. Phase 4 closes that path: a Spring Boot starter with auto-configuration, a migration guide, a compatibility matrix, and publishing infrastructure.

## What Changes

- New module `spring-struts1-starter`: Spring Boot 4.0.x auto-configuration — registers all engine beans (the `ActionServlet` facade, the initializer, the resolver, `StrutsHandlerMapping`/`StrutsHandlerAdapter`, the struts-config types registrar) without manual `@Import`; registers the `DispatcherServlet` with a configurable mapping (default `*.do`).
- The classic init-params contract (`config`, `config/<prefix>`, `validating`) moves into `spring.struts1.*` properties — config parsing semantics are unchanged.
- Conditional integration: tag services are wired in automatically when `spring-struts1-taglib` is on the classpath.
- The `apps/*` applications switch to bootstrap via the starter (application class `SpringBootServletInitializer`, web.xml reduced to the minimum); integration tests remain on embedded Tomcat 11 (a real container, not a mock).
- A new demo application (module in `spring-struts1-apps`): an executable Spring Boot jar without web.xml, with an action and form annotated `@StrutsAction`/`@StrutsForm` — the migration reference from the guide.
- A migration guide (`docs/MIGRATION.md`) with typical adjustments (jasper for JSP, properties, static resources) and a compatibility matrix (README).
- Maven artifact publishing infrastructure: a `publication` profile with `distributionManagement` configured via properties, plus documentation on running deploy. The external channel (Maven Central) is not activated in this change: it requires credentials not available in the repository (the open roadmap question is resolved outside the code).

## Capabilities

### New Capabilities

- `spring-boot-starter`: automatic configuration of the Struts 1 engine via the starter — engine bean registration, front-servlet registration and mapping, the classic init-param contract through properties, conditional tag-services integration, disabling the starter.
- `app-migration`: the application migration path to the starter — what suffices for a working migration (dependency, application class, properties), an annotated demo migration, a migration guide, a compatibility matrix, publishing infrastructure.

### Modified Capabilities

- `action-servlet-facade`: the `ActionServlet` facade is now configured in two equivalent ways — web.xml init-params (classic WAR mode) and the starter properties `spring.struts1.*` (Boot mode); parameter semantics and access via `action.getServlet()` are unchanged.

## Impact

- New module `spring-struts1-starter` depending on Spring Boot 4.0.x autoconfigure; the parent pom gains the Boot version and the publication profile.
- The `apps/*` applications (blank, examples, cookbook, mailreader): the starter dependency and application class are added, web.xml is adjusted; integration tests (`TomcatTestServer`) remain as is.
- A new demo application module in `spring-struts1-apps` (packaging jar, boot-maven-plugin).
- The engine (`spring-struts1-core`) remains unchanged behaviorally; the manual `@Import` mode is preserved for classic WAR bootstrap.
- Documentation: new `docs/MIGRATION.md`, README update (compatibility matrix, publishing).
