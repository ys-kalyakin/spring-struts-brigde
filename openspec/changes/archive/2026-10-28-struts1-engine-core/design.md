# Design

## Context

The repository does not yet contain any code — only documentation (see proposal.md — Why). Decisions fixed by the roadmap: the entry point is the Spring MVC `DispatcherServlet` with `StrutsHandlerMapping` + `StrutsHandlerAdapter`; config parsing — commons-digester with rules from weblegacy `core`; populate — commons-beanutils; `MessageResources` — on top of Spring `MessageSource`; `@StrutsAction` — singleton, `@StrutsForm` — prototype; the stack — Spring Boot 4.0.x / Framework 7, Java 17+, embedded Tomcat 11 (jakarta).

The reference: `/home/helloween/projects/struts1-reference` (weblegacy 1.5.0-rc2). Its `apps/blank` is a war with `web.xml` (servlet `action` → `org.apache.struts.action.ActionServlet`, init-param `config`, mapping `*.do`) and `struts-config.xml` (DTD 1.4): global forward `welcome` → `/Welcome.do`, action `/Welcome` with attribute `forward="/pages/Welcome.jsp"` (no type), `<message-resources parameter="MessageResources"/>` and `ValidatorPlugIn`. The blank JSPs use tag libraries (phase 2), the validator — phase 3.

## Goals / Non-Goals

**Goals:**
- Multi-module Maven: `spring-struts1-core` (engine, `org.apache.struts.*` packages) + `spring-struts1-apps` (aggregator of ported applications, in this phase — `blank`).
- Porting the configuration model (`org.apache.struts.config`) and Digester rules from weblegacy `core`.
- A request lifecycle engine on Spring MVC with the semantics of the weblegacy classic `RequestProcessor`.
- `./mvnw verify` green: integration tests of requests always on embedded Tomcat 11 (not on the mock stack).

**Non-Goals:**
- Tag libraries, Tiles, multipart upload, commons-validator/`DynaActionForm` (phases 2–3), a Spring Boot starter (phase 4).
- Multiple configuration modules (`config/<prefix>`) — only the default module.
- Path-mapped actions (`/action/*`) — suffix mapping only; moved to phase 2.
- Switching populate to Spring `ConversionService` — a refactoring candidate after phase 1.

## Decisions

1. **Entry point — `DispatcherServlet`, `ActionServlet` — a facade bean.** The `blank` port declares a Spring `DispatcherServlet` in web.xml with a mapping to the `*.do` suffix and engine init-params (`config` and others). `ActionServlet` is created as a Spring bean, initialized with the `ServletContext` and the front servlet's init-params, owns the parsed `ModuleConfig` and is available to actions via `action.getServlet()`. Alternatives: a real `ActionServlet` as the front controller (duplicates MVC, breaks integration with Spring — rejected); autoconfiguration of the whole bundle by a starter (phase 4; in phase 1 — explicit declaration in web.xml).
2. **Parsing — commons-digester with rules ported from `org.apache.struts.config` (ConfigRuleSet).** Ported: `BaseConfig`, `ActionConfig`, `FormBeanConfig`, `ForwardConfig`, `ExceptionConfig`, `MessageResourcesConfig`, `ControllerConfig`, `PlugInConfig`, `ModuleConfig` (+ `impl`), the factory and `ConfigRuleSet`; for phase 1 the dependencies on commons-chain and validator are cut out. DTD — 1.4, as in the reference; XML validation — init-param `validating` (on by default). The alternative — a custom XML parser on Spring — is rejected: digester rules already encode the semantics (merging files, set-property, wildcards), a risk of divergence from the reference.
3. **Request pipeline — a port of the classic `RequestProcessor` semantics, without commons-chain.** weblegacy 1.5 executes the pipeline via commons-chain; the observable behavior is the same as the classic `RequestProcessor` (populate → validate → execute → forward, exception handling). Phase 1 implements the pipeline directly in `StrutsHandlerAdapter` (HandlerExecutionChain: mapping → form → action → forward), commons-chain is not pulled in. The alternative — porting the chain configuration as-is — is rejected: an extra dependency with no observable benefit.
4. **Populate — commons-beanutils** (roadmap decision). All access to the static `ConvertUtils` registry is isolated in a single engine utility class (a roadmap risk); conversion — default beanutils converters.
5. **`MessageResources` — the API from weblegacy `util`, backed by a Spring `MessageSource`.** The public API (`MessageResources`, factories) is ported; a message comes from the `MessageSource`, the locale — `Globals.LOCALE_KEY` from the session, otherwise the request locale; a missing key — `???<locale>.<key>???`. At startup the `Globals.MESSAGES_KEY` attribute is placed into the application context (the blank JSP checks it).
6. **Bean resolution.** `@StrutsAction`/`@StrutsForm` — stereotype annotations registered by Spring scanning; the engine resolves an instance by class from the context (singleton for an action, prototype for a form — the semantics are provided by the bean scope), and if there is no bean — direct creation by the class name from the config (fallback).
7. **Porting blank — minimal documented adaptation.** JSPs are rewritten without tag libraries (scriptlets: checking `Globals.MESSAGES_KEY`, output of a `bean:message` equivalent, redirect from `index.jsp` to `/Welcome.do`); instead of `ValidatorPlugIn`, the ported application's configuration contains a minimal test plugin, exercising init/destroy; the rest of the `struts-config.xml` structure is preserved as in the reference.
8. **Test strategy.** Unit tests of the pipeline (populate/validate/exceptions/messages) — on fixture configurations and a dedicated test application in the `spring-struts1-core` tests; all HTTP-request-level tests — on embedded Tomcat 11 (`tomcat-embed` + JSP), not on the mock stack. weblegacy configurations are wired in as fixture tests of parsing.

## Risks / Trade-offs

- [The static `ConvertUtils` registry in beanutils is global] → isolation in a single utility class; if needed — reset/per-application local configuration.
- [JSP compilation on Boot 4.0 / Tomcat 11 may not take off] → a spike in the first task; rollback to Boot 3.5.x is documented and does not change engine code (basic MVC API).
- [Divergence of ported digester rules from weblegacy] → rules are ported file-by-file from the reference; weblegacy configurations — fixture tests.
- [Incomplete `ActionServlet` contract (init-params)] → the used set (`config`, `validating`) is implemented; extended as discovered in test applications (roadmap principle).
- [Direct pipeline implementation instead of chain] → behavior is fixed by tests against the acceptance criteria; on divergence from weblegacy, behavior is corrected per the reference.

## Open Questions

- The exact minor version of Boot 4.0.x — determined by the spike of the first task; rollback to 3.5.x does not affect the specifications and the task breakdown.
