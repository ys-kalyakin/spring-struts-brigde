# Tasks

## 1. Build skeleton and stack spike

- [x] 1.1 Create a multi-module Maven at the root: parent POM (Java 17+, Spring Framework 7), modules `spring-struts1-core` and `spring-struts1-apps`, Maven wrapper `./mvnw`; verify that `./mvnw -q validate` passes and the module structure is in place
- [x] 1.2 Spike: a test with embedded Tomcat 11 (`tomcat-embed-core` + `tomcat-embed-jasper`) renders JSP on the jakarta stack; record the chosen Spring/Boot versions (in case of a blocker — a documented rollback to Boot 3.5.x in the root README); verify that the JSP rendering test is green

## 2. Configuration model and parsing (`spring-struts1-core`)

- [x] 2.1 Port from weblegacy `core` the configuration model `org.apache.struts.config` (BaseConfig, ActionConfig, FormBeanConfig, FormPropertyConfig, ForwardConfig, ExceptionConfig, MessageResourcesConfig, ControllerConfig, PlugInConfig, ModuleConfig + impl, factory) without dependencies on commons-chain and validator; verify with a unit test of the factory and element registration
- [x] 2.2 Port `ConfigRuleSet` and the Digester parser of `struts-config.xml` (DTD 1.4): form-beans, global-forwards, global-exceptions, action-mappings (local forwards/exceptions, set-property, forward/include attributes), message-resources, plug-in; verify with a fixture test on the `struts-config.xml` of the reference `apps/blank` — all elements registered
- [x] 2.3 Implement the init-param `config` contract (comma-separated list of files, merged into the default module) and init-param `validating`; verify with unit tests: multiple files are merged, a missing and an invalid file produce a startup error with the file name

## 3. MessageResources on top of MessageSource

- [x] 3.1 Port the `org.apache.struts.util.MessageResources` API (with a factory) on top of Spring `MessageSource`: locale — `Globals.LOCALE_KEY` from the session, otherwise the request locale; missing key — `???<locale>.<key>???`; at startup `Globals.MESSAGES_KEY` is placed into the application context; verify with tests: a message by key for the session locale, a named bundle by the `key` attribute, the `???ru_RU.<key>???` format

## 4. Request lifecycle engine on Spring MVC

- [x] 4.1 Implement `StrutsHandlerMapping`: suffix routing (suffix configurable, default `*.do`), 404 for unregistered paths; verify with an integration test on embedded Tomcat: `/Welcome.do` → mapping `/Welcome`, `/Nope.do` → 404, a custom suffix is routed
- [x] 4.2 Implement instance resolution: classes not registered in Spring are created directly by the name from the config (fallback), registered ones are taken from the context; verify with unit tests of both modes
- [x] 4.3 Implement `StrutsHandlerAdapter`: populate with request parameters via commons-beanutils (isolate access to the static `ConvertUtils` in a single utility class), form placement by `scope` (request/session) under the form-bean name, validation by `validate` (errors → `input` without invoking the action, errors available in the request; `validate=false` — no validation), invocation of execute, handling of a mapping with the `forward`/`include` attribute without an action; verify with integration tests on embedded Tomcat against the `request-processing` spec scenarios
- [x] 4.4 Implement forward semantics: local forwards take priority over global ones, `redirect=true` → HTTP redirect, `redirect=false` → dispatch; verify with integration tests (name conflict, 302, JSP content in the same request)
- [x] 4.5 Implement exception handling: `global-exceptions` and the mapping's `exception` (local ones take priority, subtypes are handled), forward to `path` + message by `key` in the request; undeclared exceptions are propagated to the container; verify with integration tests of all three scenarios of the `exception-handling` spec

## 5. Annotations and Spring beans

- [x] 5.1 Add the stereotype annotations `@StrutsAction` (singleton) and `@StrutsForm` (prototype) with registration via Spring scanning and dependency injection; verify with an integration test: one action instance is reused by two requests, a form is created anew for each request (no state carry-over), dependencies are injected into the action and the form

## 6. ActionServlet facade and PlugIn

- [x] 6.1 Implement `ActionServlet` as a facade bean: initialization from the `ServletContext` and the front servlet's init-params, ownership of the `ModuleConfig`, a working `action.getServlet()`; verify with a test: `getInitParameter("config")` returns the value from web.xml, an action receives the servlet
- [x] 6.2 Implement the `PlugIn` lifecycle: init at startup in declaration order, destroy at shutdown, creation from the Spring context for beans and direct creation for the rest; verify with a test using a capturing plugin (init/destroy called, bean dependencies injected)

## 7. Porting apps/blank and the integration run

- [x] 7.1 Create the `spring-struts1-apps` module (aggregator) with the blank module (war): web.xml with `DispatcherServlet` (mapping `*.do`, init-params `config` and others), the ported `struts-config.xml` of the reference (a test plugin instead of `ValidatorPlugIn`), JSPs without tag libraries (scriptlets), `MessageResources.properties`; verify that the war builds with `./mvnw -pl spring-struts1-apps/blank -am package`
- [x] 7.2 Write an integration test of the blank run on embedded Tomcat 11: startup without errors, `index.jsp` redirects to `/Welcome.do`, `/Welcome.do` renders `Welcome.jsp`, messages by keys from `MessageResources.properties`, the `Globals.MESSAGES_KEY` attribute in the context; verify that the test is green
- [x] 7.3 End-to-end verification of phase 1: `./mvnw verify` green as a whole; go through the phase 1 acceptance criteria in `ROADMAP.md` and make sure each is covered by a test or a documented decision; check the final run report

## Workflow follow-up

- Sync the specs and archive the change (`/opsx:sync`, `/opsx:archive`) after review.
- Check the archived result (`openspec list`).
