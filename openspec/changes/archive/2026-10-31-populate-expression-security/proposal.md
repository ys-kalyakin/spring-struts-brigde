# Proposal

## Why

A security audit of the populate path (manual review + deep Mimosa static scan + an empirical probe against Spring 7.0.10) established that it is **not possible** to craft a parameter-name expression that executes arbitrary code: the name syntax is structural (no method calls or expression evaluation), class-loader traversal vectors (`class.classLoader.*`, `class.module.classLoader.*` — CVE-2014-0114 / Spring4Shell-style) are blocked by two layers, and class instantiation by name happens only from configuration. However, the audit found violations of the `form-populate` spec itself and gaps in the defense-in-depth that must be fixed: some "safe" expressions crash populate with a servlet error (HTTP 500) instead of being silently skipped, the CVE filter is not covered by tests and could silently regress, and the multipart path reads properties before the filter is applied.

## What Changes

- **Fail-silent navigation**: an unknown or unreadable intermediate property during nested-path navigation (`a.b.c`) SHALL be skipped without error; currently `NotReadablePropertyException` is not caught and the entire populate fails with a servlet error — a direct violation of the spec requirement that "a name that does not correspond to any property SHALL be skipped without error".
- **Second layer of defense against class access in populate**: segments named `class` (any case) — bare, indexed (`class[0]`), and mapped (`class(key)`) — SHALL be skipped at the `FormPopulator` level rather than relying solely on the `RequestUtils` regex filter (CVE-2014-0114). Currently a bare `class=x` reaches the `String`→`Class` conversion and fails with a populate error.
- **Order of checks in the multipart path**: the CVE name filter SHALL be applied before calling `rationalizeMultipleFileProperty` (currently it reads property types through `BeanWrapper` using the raw name before the filter — a `class.x` parameter in a multipart request produces an unhandled exception before the populate try block).
- **Regression tests for the class filter**: an expression matrix (from the verification probe) for the regex layer and for the populate layer, including `class`, `class.classLoader`, `class.module.classLoader.DefaultAssertionStatus`, `x.class.y`, `foo[class]`, `foo('class')`, `map(class)`, lazy forms, and DynaBean.
- **Spec refinement for `form-populate`**: exact semantics for ignoring class access and for silently skipping unknown/unreadable intermediate properties, including the multipart path.
- Out of scope (deliberately fixed): client-side `eval` in the ported JS commons-validator (client-side legacy validation, no server-side effect); reflective class loading from configuration (`applicationInstance`/factories — by design, not reachable from HTTP input); `String`→`Class` conversion for `Class`-typed form properties (locked in by the spec as beanutils compatibility; for forms with such properties — a documented risk, a rare case).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `form-populate`: the requirements "Populate Value Sources" (complete ignoring of class access, ordering of the filter and multipart processing) and "Property Name Syntax" (silent skipping of unknown/unreadable intermediate properties, including nested paths) are refined.

## Impact

- **Code**: `spring-struts1-core` — `FormPopulator` (catching `InvalidPropertyException` in navigation/descriptors, skipping `class` segments), `RequestUtils` (ordering the CVE filter before `rationalize`, fail-silent in `rationalize`). The public API and populate conversion semantics do not change; only behavior on "garbage"/dangerous names changes (error → silent skip), which removes the divergence from the spec.
- **Tests**: new regression tests in `spring-struts1-core/src/test/java/org/apache/struts/util/`; existing populate fixture tests do not lock in the currently failing behavior (verified) and must not change.
- **Dependencies**: unchanged (Spring 7.0.10, commons-beanutils 1.11.0 — current, both contain fixes for CVE-2010-1622 / CVE-2022-22965 / CVE-2014-0114).
- **Compatibility**: for well-behaved forms behavior does not change; for anomalous names it changes from 500 to silent skip (which is exactly what the spec requires and what is compatible with the beanutils reference, where unknown names were skipped).
