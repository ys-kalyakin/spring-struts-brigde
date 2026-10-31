# Proposal

## Why

Phase 1 (`struts1-engine-core`) delivered a working engine and ran the ported `apps/blank`, but its JSPs had to be rewritten without tag libraries — migrating real applications without them is impossible: the Struts tag libraries are the second pillar of compatibility alongside configuration (see phase 1 proposal.md and the roadmap compatibility principles). Phase 2 implements the `struts-bean`, `struts-html`, `struts-logic`, and `struts-nested` tag libraries with the same URIs, TLDs, and behavior as weblegacy, and runs the target application `apps/examples` (the exercise-taglib and dispatch scenarios).

## What Changes

- A new module `spring-struts1-taglib` is created: 98 tags of the four libraries, ported from weblegacy `taglib` while preserving the `org.apache.struts.taglib.*` packages; the TLDs are copied unchanged (the same `<uri>`, tag names, and attributes) — JSPs connect the libraries with the same `taglib` declarations.
- `spring-struts1-core` receives the utilities the tags depend on: `TagUtils`, `ResponseUtils`, `ModuleUtils` (classic APIs).
- The engine gains support for multiple configuration modules: init-params `config/<prefix>` (e.g. `config/exercise`), module selection by request path (default module — as before), module-relative forwards. The phase-1 specs `struts-config-parsing` and `request-processing` are modified.
- Routing is extended with path-mapped actions (`/action/*` alongside `*.do`) — a decision deferred from phase 1.
- A minimal `DynaActionForm` is ported (creation from `<form-property>`, initial values, populate, dynamic DynaBean access): needed by the target application's dispatch scenarios; DynaValidatorForm remains in phase 3.
- Dispatch actions are ported from weblegacy `extras` (`org.apache.struts.extras.actions`): `DispatchAction`, `LookupDispatchAction`, `MappingDispatchAction`, `EventDispatchAction`, `ActionDispatcher`, `EventActionDispatcher`, `ForwardAction`, `IncludeAction`.
- `apps/examples` is ported with the exercise and dispatch modules (JSPs and application sources — unchanged); the upload and validator sections are excluded until phase 3.
- The ported `blank` switches to the original reference JSPs (with tag libraries) instead of the phase 1 adaptation — a regression check of the basic tags.

## Capabilities

### New Capabilities

- `bean-taglib`: the `struts-bean` library (URI `http://struts.apache.org/tags-bean`): write, message, define, size, header, cookie, parameter, include, resource, struts — outputting values, messages, defining variables, access to request data.
- `html-taglib`: the `struts-html` library: form and widgets, errors/messages, img/link/rewrite, base/html, xhtml mode; the `javascript` tag is deferred until the validation phase (depends on commons-validator).
- `logic-taglib`: the `struts-logic` library: iterate, comparisons, match, present/empty, messagesPresent, redirect/forward.
- `nested-taglib`: the `struts-nested` library — nested wrappers over bean/html/logic with relative property paths.
- `dynamic-form-beans`: `DynaActionForm` — dynamic forms from `<form-property>` (validator variants not yet included).
- `dispatch-actions`: the dispatch action family (DispatchAction, LookupDispatchAction, MappingDispatchAction, EventDispatchAction, ActionDispatcher) — selecting the processing method from the request.

### Modified Capabilities

- `struts-config-parsing`: init-params `config/<prefix>` define configuration modules; `config` remains the default module (list of files merged — as before).
- `request-processing`: routing selects the module by request path prefix; path-mapped action mapping (`/action/*`) is added.

## Impact

- **Structure:** a new Maven module `spring-struts1-taglib` (depends on core); `spring-struts1-apps/examples` — a new test application (war).
- **Dependencies:** jakarta.servlet.jsp-api (provided) for compiling the tags; commons-validator is not included (phase 3).
- **Reference:** `/home/helloween/projects/struts1-reference` — the taglib module (v1.5.0-rc2), TLDs in `META-INF/tld`, `apps/examples`; tag HTML output is verified against weblegacy.
- **Out of scope:** struts-el (EL variants of the tags — a separate decision after this phase), commons-validator/`ValidatorPlugIn`/`DynaValidatorForm`, multipart upload (html:file is rendered, but upload handling is phase 3), Tiles, full xhtml coverage beyond the classic mode, the remaining extras utilities (DownloadAction etc.).
- **Compatibility:** tag names, TLDs, URIs, and the structure of the generated HTML — per the weblegacy reference; phase 2 acceptance criteria from ROADMAP.md.
