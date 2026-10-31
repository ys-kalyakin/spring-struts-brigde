# Proposal

## Why

The roadmap (phase 1) and the archived change `spring-struts1-foundation` designated this change as the starting point of implementation: the project so far contains only documentation, no code. The goal of phase 1 is a minimal Struts 1 engine on Spring MVC capable of running the ported `apps/blank` from the weblegacy/struts1 reference: this is the first verifiable step toward a drop-in replacement of the Struts 1 artifact with `spring-struts1`.

## What Changes

- A multi-module Maven project is created: `spring-struts1-core` (engine and configuration model), `spring-struts1-apps` (ported test applications); in this phase — only `blank`.
- The configuration model and Digester rules from weblegacy `core` (`org.apache.struts.config`) are ported: parsing of `struts-config.xml` — action mappings, forms, forwards, exceptions, message-resources, plugins.
- A request lifecycle engine is built on Spring MVC (`StrutsHandlerMapping` + `StrutsHandlerAdapter` inside `DispatcherServlet`): suffix routing (`*.do`), form populate with commons-beanutils semantics → validation (`validate=true/false`) → `execute` → forward; local forwards take priority over global ones; `redirect=true` — HTTP redirect, otherwise dispatch.
- `MessageResources` is implemented on top of Spring `MessageSource` with Struts locale semantics (session, initialized from the request) and the missing-key format `???<locale>.<key>???`.
- Declarative exception handling is added: `global-exceptions` and `exception` in a mapping — forward + `ActionMessage`; undeclared exceptions are propagated to the container.
- The `@StrutsAction` (singleton) / `@StrutsForm` (prototype) annotations and bean resolution from the Spring context are added; classes not registered in Spring are created directly (classic-mode fallback).
- `ActionServlet` is preserved as a compatibility facade (init-param `config`, working `action.getServlet()` reference); the `PlugIn` lifecycle (init at startup, destroy at shutdown; created from the Spring context if a bean, otherwise direct creation).
- `apps/blank` is ported with minimal documented adaptation: JSPs are rewritten without Struts tag libraries (phase 2), instead of `ValidatorPlugIn` a simple test plugin is wired in (validator — phase 3); the run is an integration test on embedded Tomcat 11.

## Capabilities

### New Capabilities

- `struts-config-parsing`: reading and merging configuration from `struts-config.xml` — action mappings, forms, global/local forwards, exceptions, message-resources, plugins; the init-param `config` contract (list of files) and startup errors on a missing/invalid file.
- `request-processing`: routing requests to actions (suffix mapping) and the full request lifecycle — populate → validate → execute → forward — with forward semantics and 404 for unregistered paths.
- `message-resources`: key-based access to application messages on top of Spring `MessageSource`, Struts locale semantics and the missing-key format.
- `exception-handling`: declarative exception handling via `global-exceptions` and `exception` in a mapping.
- `spring-bean-integration`: the `@StrutsAction`/`@StrutsForm` annotations, singleton/prototype semantics, dependency injection, fallback of direct creation for classes not registered in Spring.
- `action-servlet-facade`: the `ActionServlet` facade (compatibility with legacy code, init-params) and the `PlugIn` lifecycle.

### Modified Capabilities

(none — the project has no specs yet, `openspec list --specs` is empty)

## Impact

- **Structure:** new Maven modules `spring-struts1-core` and `spring-struts1-apps` (parent POM at the root); engine code keeps the `org.apache.struts.*` packages.
- **Dependencies:** Spring Framework 7 / Spring Boot 4.0.x (in case of a JSP blocker on Tomcat 11 — a documented rollback to Boot 3.5.x without changing engine code), commons-digester, commons-beanutils; Java 17+; embedded Tomcat 11 for integration tests.
- **Test applications:** the ported `apps/blank` from `/home/helloween/projects/struts1-reference` (v1.5.0-rc2) — the target application of the phase; the compatibility reference for configurations and scenarios.
- **Out of scope:** tag libraries, Tiles, multipart upload, commons-validator/`DynaActionForm` (phases 2–3), a Spring Boot starter (phase 4), support for multiple modules (`config/<prefix>`) and path-mapped actions (`/action/*`) — the path-mapped decision is recorded in design.md.
- **Refactoring candidate after phase 1:** switching populate from commons-beanutils to Spring `ConversionService`.
