# Design

## Context

The engine (StrutsHandlerMapping/StrutsHandlerAdapter in the DispatcherServlet), the bean/html/logic/nested tag libraries on the Spring base, the plugin mechanism (PlugIn + `<plug-in>` + `<set-property>`), DynaActionForm with `<form-property>` parsing, and the MessageResources→MessageSource bridge are already implemented. The configurations of the examples upload/validator modules have been migrated and are waiting for their classes (`ValidatorPlugIn`, `UploadForm`, `RegistrationForm`); the `struts-html` TLD declares `html:javascript` without an implementation. `ControllerConfig` already parses `bufferSize/maxFileSize/maxStringLen/memFileSize/multipartClass/tempDir`, but lacks `maxSize`/`fileCountMax`, and there is no multipart handling in the request loop at all.

The reference (weblegacy 1.5.x, jakarta) provides the material: Tiles — a separate `struts-tiles` module (the `org.apache.struts.tiles.*` packages, 11 tags, the `I18nFactorySet` default factory, a digester parser, DTDs 1.1/1.3/1.4) plus a separate optional `struts-tiles2` bridge on org.apache.tiles 3.0.8; upload — `org.apache.struts.upload` on top of commons-fileupload2-jakarta (2.0.0-M1) with graceful limit-exceeded semantics (request attributes instead of exceptions) and multi-file `getFileElements() → FormFile[]`; validator — `org.apache.struts.validator` on top of commons-validator 1.10.1, validwhen on ANTLR4-runtime 4.13.2, `ValidatorActionForm`/`DynaValidatorActionForm` moved out to extras; mailreader depends on the `struts-mailreader-dao` dao module and the extras plugins (`ModuleConfigVerifier`, `DigestingPlugIn`).

## Goals / Non-Goals

**Goals:**

- The classic `org.apache.struts.upload`/`org.apache.struts.validator`/`org.apache.struts.tiles.*` contract with the reference behavior; the target applications compile and run without changes to their own code.
- Spring-nativeness in the spirit of phase 3: the extension points are Spring components (tile logic services, the forward-resolution hook, adaptation of the Spring multipart parser), the Tiles/validator plugins go through the existing plugin mechanism; the core does not depend on the tiles module.
- All three target applications (the upload/validator sections of examples, cookbook, mailreader) start and pass their main scenarios as ITs.

**Non-Goals:**

- The tiles2 bridge (`org.apache.tiles`), struts-el, faces/scripting apps.
- `BeanValidatorForm`/`LazyValidatorForm` (not needed by the target apps; cheap to add on request).
- The Boot starter, artifact publishing (phase 4).
- Replacing commons-beanutils with the Spring ConversionService (a deferred refactoring, not in this phase).

## Decisions

### D1. Tiles — a port of the 1.x module, not tiles2 (closes the roadmap's open question)

We port the weblegacy `struts-tiles` module (the `org.apache.struts.tiles`/`tiles.taglib`/`tiles.xmlDefinition` packages) into a new `spring-struts1-tiles` module. The tiles2 alternative (a bridge to Apache Tiles 3.0.8) is rejected: compatibility principle #3 requires the same URI/TLD/names/behavior for `struts-tiles`, classic applications use 1.x semantics (`tiles:insert` with `put`), and tiles2 pulls in an external stack (tiles-api/core/request) with a different model — it is a separate optional product both in weblegacy and for us.

### D2. Tiles the Spring-native way: a forward hook instead of replacing the processor

The classic integration wires tiles in by replacing the RequestProcessor (`TilesRequestProcessor`) or with a chain command (`TilesPreProcessor`). We have a single adapter, so we introduce an extensible point in the core: before processing a forward, `StrutsHandlerAdapter` polls Spring beans of type `DefinitionForwardResolver` (a new interface in `org.apache.struts.spring`); `TilesPlugin` registers the implementation as a singleton in the `WebApplicationContext` at init. The core depends only on the interface — the tiles module stays optional. The definitions factory is stored in the servlet context under the classic keys (`org.apache.struts.tiles.DEFINITIONS_FACTORY[+prefix]` — the classic `TilesUtil`/legacy code works) and is also available as a Spring component; XML parsing uses the reference's digester rules with local DTDs; the default factory is `I18nFactorySet`. The tags are on the `SpringTagSupport` base; the logic (definition resolution, tile context) is Spring services (overridable); the reference's `struts-tiles.tld` goes into the module's `META-INF/tld/`.

### D3. Upload: the classic contract over commons-fileupload2-jakarta; the Spring multipart parser — adaptation

We implement `CommonsMultipartRequestHandler` on commons-fileupload2-jakarta (base version as in weblegacy 2.0.0-M1; during implementation we take the newest stable 2.x if the API is compatible) — this gives the exact semantics of the reference: graceful limit excess (the request attributes `ATTRIBUTE_MAX_LENGTH_EXCEEDED` and the byte/string/count-specific ones instead of an exception), temp files with a threshold, multi-file. Integration: `StrutsHandlerAdapter.processMultipart` wraps a `multipart/form-data` POST into a `MultipartRequestWrapper`; `processPopulate` sets `Globals.MULTIPART_KEY` (the class from `mapping.multipartClass` or `ControllerConfig`), `RequestUtils.populate` obtains the elements via the handler (`getAllParametersForMultipartRequest` + `rationalizeMultipleFileProperty`), `rollback`/`finish` run in the adapter's error/success branches.

The alternative "everything through the Spring `StandardServletMultipartResolver`" is rejected as the main path: the limits live in the container (`MultipartConfig`), and an excess yields a container exception/`MultipartException` instead of an attribute — the reference behavior (form validation sees the attribute and writes a message) is not reproduced. But we keep the compatibility path: if the user declared their own `multipartResolver` and the request is already a `MultipartHttpServletRequest`, the adapter builds the same element maps from its parts (a `SpringMultipartRequestHandler` adapter) without re-parsing.

### D4. Validator: a port of the package into the core, validwhen on ANTLR4-runtime

`org.apache.struts.validator` (ValidatorPlugIn, Resources, ValidatorForm, DynaValidatorForm, FieldChecks, validwhen) — into `spring-struts1-core`; `ValidatorActionForm`/`DynaValidatorActionForm` — into `org.apache.struts.extras.validator` (the weblegacy layout); the resources `validator-rules.xml`, `LocalStrings.properties`, and the JS utilities — from the reference into the core classpath. Dependencies: commons-validator 1.10.1 + antlr4-runtime 4.13.2. We vendor the validwhen parser with generated sources (as in weblegacy) instead of an ANTLR plugin in the build — a deterministic build without generator version drift; on divergence, generation via the plugin (fallback). The validation key is `mapping.getAttribute()` (core forms); for extras forms — `mapping.getPath()`. `DynaValidatorForm` extends `DynaActionForm` — the existing skip-Dyna in the registrar/resolver covers it without changes to spring-bean-integration. `ValidatorPlugIn` goes through the existing plugin mechanism (the context keys `VALIDATOR_KEY+prefix`, `STOP_ON_ERROR_KEY+prefix` — as in the reference). `html:javascript` (`JavascriptValidatorTag`) — in the taglib module on the Spring base; it reads the JS resources from the core.

### D5. We vendor mailreader-dao as our own module

We copy the dao module (`org.apache.struts.apps.mailreader.dao.*` — UserDatabase/User/Subscription, the memory impl) from the reference into the repository as a `mailreader-dao` module under `spring-struts1-apps`. Alternatives: a dependency on the published `io.github.weblegacy:struts-mailreader-dao` — rejected (an external dependency on a reference SNAPSHOT/release in our build; the dao is application domain code, not framework code) and building from a local clone of the reference — rejected (the build stops being self-contained). The extras plugins for mailreader (`ModuleConfigVerifier`, `DigestingPlugIn`) are ported into the core (`org.apache.struts.extras.plugins`, the package is checked against the reference during implementation) — this is part of the struts-extras public API.

### D6. Applications: examples enables the modules, cookbook and mailreader are new wars

- `examples`: web.xml gains the init-params `config/upload` and `config/validator` (and the neighboring validator config variants); the classes and JSPs of the upload/validator sections are ported from the reference (they are jakarta-compatible; no changes needed except the config variants that reference classes from the ported packages).
- `cookbook`: a new war module — porting the reference application (DynaValidatorForm validation, the custom `twofields` validator, localization, tokens); ITs over the main scenarios.
- `mailreader`: a new war module + a dependency on the vendored dao; porting the application (DynaValidatorForm with `extends` form-bean, MemoryDatabasePlugIn — in the application sources, wildcard mappings); ITs over the main scenarios (logon → registration → subscription).
- `blank`: the `TestPlugin` stub is replaced with a real `ValidatorPlugIn` (as in the reference blank: validation of the welcome form); it remains the phase 1 regression.

## Risks / Trade-offs

- [commons-fileupload2 is a young line (2.0.0-M1 in weblegacy)] → during implementation take the newest stable 2.x and run the upload ITs (including limit excess and multi-file); the API drift is localized in a single class, `CommonsMultipartRequestHandler`.
- [The ANTLR4-vendored validwhen sources may diverge from the runtime version] → we pin antlr4-runtime 4.13.2 as in the reference; on mismatch — switch to generation with antlr4-maven-plugin (a solution local to one package).
- [Porting ~100 highly coupled taglib/tiles/taglib-taglib classes] → we move the packages from the reference with minimal changes (jakarta is already in the reference) and only fix the integration points (tag bases, services); ITs on the reference applications catch output regressions.
- [Divergence of validator behavior details (arguments, bundles, page) from the reference] → we wire in the configurations and validation.xml of the target applications as fixture tests; `stopOnFirstError` and message order are verified on the examples/validator ITs.
- [The static TilesUtil stores factories in the servlet context] → the keys and the module-aware scheme are exactly as in the reference; the Spring components are an additional, not a replacing, access path.
- [Vendoring dao/validwhen grows the repository] → it provides self-containment and build reproducibility; the modules are marked with an origin comment in the module pom/README.

## Migration Plan

The change is additive: the new tiles module and the new core packages do not touch the existing specs (except the two modifications in the delta). Implementation order: upload → validator (the core, the javascript tag) → the tiles module → enabling examples → cookbook → mailreader → full regression. Rollback — revert the phase's changes; the public API of previous phases does not change.

## Open Questions

- The exact commons-fileupload2 version (2.0.0-M1 as in the reference, or the newest stable 2.x) — decided by the first upload task by running the ITs; it does not affect the spec.
- The extras plugins package in weblegacy (`org.apache.struts.extras.plugins` or `org.apache.struts.plugins`) — fixed according to the actual reference in the first mailreader-dependencies task; the class names and behavior do not change.
