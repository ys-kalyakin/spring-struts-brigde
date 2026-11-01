# Design

## Context

The engine parses `<controller>` fully into `ControllerConfig` but implements only part of it. Current usage audit (call-site grep across all modules):

| Property | Honored? | Where |
|---|---|---|
| `contentType`, `locale`, `inputForward`, `forwardPattern`, `nocache`, `pagePattern` | yes | engine, taglib |
| `maxSize`, `maxFileSize`, `maxStringLen`, `memFileSize`, `fileCountMax`, `tempDir` | yes | `CommonsMultipartRequestHandler` |
| `multipartClass` | yes | `RequestUtils.getMultipartHandler` (controller + action level) |
| `processorClass` | **no** | zero call sites outside the config model |
| `catalog`, `command` (commons-chain) | **no** | zero call sites |
| `bufferSize` | **no** | zero call sites; multipart handlers read `memFileSize`, not `bufferSize` |

Both init paths parse independently and share `StrutsConfigParser`: `ActionServlet.initModuleConfig` (classic facade path) and `StrutsEngineRegistrar.parseModule` (Spring registration path). Tiles-forward behavior that classically required `TilesRequestProcessor` is implemented natively (`DefinitionForwardResolver`), so that value is a no-op in configuration, not a missing feature.

See proposal.md (Why) for motivation; see the delta specs for the normative behavior.

## Goals / Non-Goals

**Goals:**
- Fail startup with an actionable message when a module's controller configuration would be silently ignored (`processorClass`, `catalog`, `command`).
- Warn (not fail) on `bufferSize`, whose effect is tuning, not behavior.
- One shared validator used by both init paths, so the facade and the starter cannot diverge.
- A migration-guide section that makes every unimplemented controller property discoverable with its treatment and remediation.

**Non-Goals:**
- Implementing `processorClass` support or a RequestProcessor SPI/hooks (a possible follow-up change, not this one).
- Supporting commons-chain (`catalog`/`command`) semantics.
- An escape-hatch property to downgrade errors to warnings: it would reintroduce the silent divergence this change exists to prevent. If real applications are blocked, a follow-up can revisit with concrete evidence.
- Validating action-level `multipartClass` — it is honored and out of the problem scope.
- Completing `ModuleConfigVerifier` (separate concern).

## Decisions

### D1. Dedicated validator invoked from both init paths
A new `ControllerConfigValidator` in `org.apache.struts.config`, called right after each module's parse in `ActionServlet.initModuleConfig` and `StrutsEngineRegistrar.parseModule`.
- *Why two call sites instead of validating inside `StrutsConfigParser.parse`*: the parser stays a pure parser; the DTD already accepts attributes the engine may not implement, and a parse-time check would couple parsing semantics to engine capabilities. Explicit calls keep the audit visible at the two known entry points.
- *Alternative — validating only in the starter*: rejected; the classic `ActionServlet` facade path must behave identically.

### D2. Runtime exception from the validator, wrapped by the call sites
The validator throws a dedicated runtime exception (message carries property name, offending value, module prefix, and the supported values). `ActionServlet` wraps it into `ServletException` alongside its existing parse-error handling; `StrutsEngineRegistrar` wraps it the way it already wraps parse failures. Startup fails in both paths.
- *Why a runtime exception*: the validator itself must not depend on `jakarta.servlet`, so it is testable standalone and the servlet dependency stays at the facade.

### D3. String-based allowlist, no class loading
Supported `processorClass` values are compared as trimmed strings against the classic default `org.apache.struts.action.RequestProcessor` and `org.apache.struts.tiles.TilesRequestProcessor`. Empty/absent values normalize to the default.
- *Why not load the class and check assignability*: tiles-forward behavior is native to the engine regardless of the configured name; loading application classes during validation would couple startup validation to classpath details and permit subclasses whose extra logic would still be skipped — the exact silent divergence being prevented. Exact names only: a subclass of `TilesRequestProcessor` carries custom logic and must fail with a clear message.

### D4. Per-module validation with prefix in every message
Validation runs for each parsed `ModuleConfig`; messages identify the module as the default module (empty prefix) or by `/<prefix>`, so an application with many modules sees exactly which file needs attention.

### D5. `bufferSize` warning via SLF4J
Non-default `bufferSize` logs a warning naming property, value, and module prefix and continues. It is a performance knob with no observable behavior difference; failing on it would block applications for no functional reason.

### D6. Documentation in the migration guide
`docs/MIGRATION.md` gains a "Functional limitations" section after "Common adjustments": a table of the unimplemented properties with their treatment (startup error / startup warning), and the remediation path for a custom `RequestProcessor` — re-expressing its `process*` overrides as Spring MVC interceptors, servlet filters, or engine extension points.

## Risks / Trade-offs

- [A real application subclasses `TilesRequestProcessor` or the default processor] → startup fails with an explicit message; the migration guide documents the porting path. This is the intended fail-fast outcome, not a regression.
- [An application in this repository's IT suite already uses an unsupported value] → the validator's unit/IT work runs the full IT suite first; if any app sets such a value, its configuration is migrated within this change before enabling validation.
- [Validation adds a startup step to every module] → four string comparisons per module; negligible.

## Open Questions

None — the error-vs-warning split is fixed per property by the delta specs, and the allowlist is closed (two exact names).
