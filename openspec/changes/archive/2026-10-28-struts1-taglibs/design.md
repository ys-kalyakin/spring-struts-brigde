# Design

## Context

Phase 1 delivered the engine (`DispatcherServlet` + `StrutsHandlerMapping`/`StrutsHandlerAdapter`, the `ActionServlet` facade, `MessageResources` on top of `MessageSource`, config parsing) and a ported `blank` with JSPs without tag libraries. The phase reference: the weblegacy `taglib` module — 4 libraries (bean 11, html 30, logic 17, nested 40 tags) + `TagUtils`, TLDs in `META-INF/tld`. The target application `apps/examples` uses **multi-module configuration** (`config/exercise`, `config/dispatch`; upload/validator — phase 3), `DynaActionForm` in the dispatch config, and dispatch actions from the `extras` module (`org.apache.struts.extras.actions`). See proposal.md — Why.

## Goals / Non-Goals

**Goals:**
- A `spring-struts1-taglib` module with 98 tags, TLDs "as is" from the reference, same URIs.
- Ported utilities `TagUtils` (in the taglib module, as in the reference) and `ResponseUtils`/`ModuleUtils` (in core).
- Multi-module configuration and module selection by request; path-mapped actions.
- A minimal `DynaActionForm`; the dispatch action family (extras subset).
- A run of `apps/examples` (exercise + dispatch) and `blank` on the original JSPs — on embedded Tomcat 11.

**Non-Goals:**
- struts-el, commons-validator/`ValidatorPlugIn`/`DynaValidatorForm`/`html:javascript` (phase 3), multipart handling (rendering `html:file` — yes, accepting uploads — no), Tiles, `DownloadAction` and the rest of the non-ported extras, a separate extras artifact.

## Decisions

1. **Tag porting — file-by-file from the reference**, preserving the `org.apache.struts.taglib.{bean,html,logic,nested}` packages and `TagUtils`; adaptation is minimal (jakarta is already in the reference). TLDs are copied byte-for-byte — the container finds them by `<uri>` inside the jar, JSP declarations work without edits. The alternative — reinventing the tags on top of Spring — is rejected: output and attribute compatibility would be lost.
2. **A single source of truth for URLs and messages — `TagUtils`.** It resolves the module from the request (the `Globals.MODULE_KEY` attribute that the adapter already sets), builds URLs with the context, and renders messages. `ModuleUtils` (`selectModule`/`getModuleConfig`) is ported into core; module selection happens in `StrutsHandlerMapping` before dispatching.
3. **Multi-modularity.** `ActionServlet.initModuleConfig` parses all init-params with the `config/` prefix (the suffix after `config/` is the module prefix starting with a slash) into a map of prefixes. `StrutsHandlerMapping` gains module selection by request path (the prefix after the context; otherwise the default module), and the selected `ModuleConfig` is placed into a request attribute; the adapter and `TagUtils` read the module from the request. Module-relative forwards work within a module; cross-module ones go through the forward's `module` attribute (the prefix is prepended to the path).
4. **Path-mapped actions.** The mapping property `servletMapping` accepts the classic formats (`*.do` or `/action/*`): suffix-based — as now; prefix-based — strips the prefix (an optional suffix is trimmed as well). The default value is `*.do`.
5. **`DynaActionForm` — a minimal port** (`org.apache.struts.action.DynaActionForm` on top of the commons-beanutils `DynaBean`, which is already available): creation from `<form-property>` with `initial` values, populate via the existing `RequestUtils.populate`, dynamic access by name/index. `FormBeanConfig.createActionForm` gains a branch for dynamic forms. Validator variants (`DynaValidatorForm`) — phase 3.
6. **Dispatch actions — an extras subset in core.** The classes are ported preserving the `org.apache.struts.extras.actions` package: `DispatchAction`, `LookupDispatchAction`, `MappingDispatchAction`, `EventDispatchAction`, `ActionDispatcher`, `EventActionDispatcher`, `ForwardAction`, `IncludeAction`. Putting them in core (rather than a separate artifact) is temporary: with the phase 4 migration the starter assembles the artifacts anyway; a separate module can be extracted if needed.
7. **`html:javascript` is not ported in this phase** (it depends on commons-validator): the TLD stays unchanged, the tag implementation will arrive with the validator in phase 3 — a JSP using this tag will produce a processing error. The other 29 html tags are in scope.
8. **Porting examples — only the working sections.** Modules `config` (root), `config/exercise`, `config/dispatch`; the upload/validator sections are excluded from web.xml and the webapp until phase 3. The application sources (`org.apache.struts.webapp.examples.*`, `...dispatch.*`, `...validator.LocaleAction`) and JSPs come from the reference without edits; `CustomFormBean`/`CustomActionMapping` are already supported (`set-property` via `BaseConfig`).
9. **blank — on the original JSPs.** The JSPs from the reference (with `html:html`, `bean:message`, `logic:notPresent`) are copied as is, replacing the phase 1 adaptation: a basic regression of the bean/html/logic tags on a real application.
10. **Test strategy.** ITs on embedded Tomcat 11: each exercise page → 200 + key HTML structure markers (attributes, nesting — per the output compatibility criterion); the interactive dispatch scenarios (methods via buttons/events) are exercised with POST requests; `blank` on the original JSPs; dedicated pages for nested and xhtml (examples does not exercise them). Unit tests — for `TagUtils` (filter, URLs), module selection, path-mapped, `DynaActionForm` (initial/populate), and the dispatch actions.

## Risks / Trade-offs

- [HTML output divergence from weblegacy] → tags are ported file-by-file, output is pinned by tests on attributes/nesting; the reference exercise pages serve as fixture tests.
- [JSP 4.0 (Tomcat 11) vs the reference's JSP 3.1] → the `jakarta.servlet.jsp.tagext` API is stable between 3.x and 4.0; the risk is closed by an IT run of all pages on Tomcat 11.
- [Nested tags depend on the `BodyTagSupport` stack] → `NestedPropertyHelper`/`NestedReferenceHelper` are ported as in the reference; nesting tests are isolated in dedicated pages.
- [Multi-module state in the session (forms of different modules)] → phase 1 stores forms by form-bean name; the reference semantics (a shared session namespace) are preserved as is, divergences are recorded in tasks.
- [`TagUtils` pulls in adjacent core bits (ModuleUtils, ResponseUtils)] → they are ported as one group before the tags so that the tags compile without edits.
- [A TLD declares a tag without an implementation (`html:javascript`)] → the container parses the TLD without instantiating classes; only a JSP using that tag fails — the behavior is pinned by the spec.

## Open Questions

- The integration point for struts-el (a separate change after phase 2 or within phase 3) — does not affect the scope and specs of this phase.
