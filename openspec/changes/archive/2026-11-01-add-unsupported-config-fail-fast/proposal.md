# Proposal

## Why

The engine parses every `<controller>` attribute of `struts-config.xml` but implements only some of them: `processorClass`, `catalog`, `command`, and `bufferSize` are parsed into `ControllerConfig` and never used (`getProcessorClass` has zero call sites outside the config model). An application that relies on a custom `RequestProcessor` — the most common Struts 1 customization mechanism (authentication, audit, output post-processing) — starts without any error while the processor's logic is silently skipped. This silent divergence is the most dangerous failure mode of a migration: the application appears to work and misbehaves only where the missing logic mattered.

## What Changes

- Startup validation of each module's `ControllerConfig` against the set of controller properties the engine implements:
  - `processorClass` with a value other than the supported processor classes fails startup with an error naming the property, the offending value, and the module prefix.
  - Supported processor classes: the classic default `org.apache.struts.action.RequestProcessor` and `org.apache.struts.tiles.TilesRequestProcessor` (tiles-forward behavior is implemented natively by the engine).
  - `catalog`/`command` (commons-chain knobs with no meaning on this engine) with non-default values fail startup the same way.
  - `bufferSize` with a non-default value produces a startup warning (a tuning knob with no implemented effect, not a behavior difference).
- A shared "Functional limitations" section in the migration guide: what is not implemented, what the new startup errors mean, and how to port a custom `RequestProcessor` (re-express its logic as Spring MVC interceptors, filters, or engine extension points).

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `struts-config-parsing`: new requirement — startup validation of controller configuration properties the engine does not implement (startup failure on unsupported `processorClass`/`catalog`/`command`, startup warning on non-default `bufferSize`).
- `app-migration`: the migration guide requirement gains functional limitations — the guide must document what is not implemented and the remediation path for a custom `RequestProcessor`.

## Impact

- **Code**: `spring-struts1-core` — a startup validator shared by both init paths (`ActionServlet` facade init and `StrutsEngineRegistrar`, which already share `StrutsConfigParser`); per-module validation over each `ModuleConfig`'s `ControllerConfig`.
- **Behavior**: applications whose `struts-config.xml` sets unsupported `<controller>` values now fail at startup with an actionable message instead of starting and silently misbehaving; configurations at classic defaults are unaffected. `bufferSize` tuning is reported as a warning and does not block startup.
- **Docs**: `docs/MIGRATION.md` — new "Functional limitations" section referenced from the common-adjustments flow.
- **Tests**: unit tests for the validator (supported/unsupported values, module prefixes, warning case); startup-failure test with a configuration carrying a custom `processorClass`.
