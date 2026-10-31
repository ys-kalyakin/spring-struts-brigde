# Design

## Context

The populate path (form submission) accepts beanutils parameter-name expressions (`a.b[i](key)`), parses them with its own structural parser `StrutsExpressionParser`, and fills the form via `FormPopulator` on top of the Spring `BeanWrapper` and `StrutsConversionService`. The audit question "can an expression that executes arbitrary code be crafted" was verified empirically (probe against Spring 7.0.10): **no**.

Experimental matrix (probe; layer 1 — the `RequestUtils.CLASS_ACCESS_PATTERN` regex, layer 2 — `FormPopulator`/`BeanWrapper`):

| Expression | Layer 1 | Layer 2 (Spring 7.0.10) |
|---|---|---|
| `class.classLoader`, `class.protectionDomain`, `CLASS.classLoader` | BLOCKED | — |
| `class.module.classLoader.DefaultAssertionStatus` | BLOCKED | on a Class object, reading `module`/`classLoader` is disallowed (`NotReadablePropertyException`) |
| `foo.class.bar` | BLOCKED | — |
| `class` (bare), `x.class` (tail), `servlet.class` | allowed | the `class` descriptor is readable; the `String`→`Class` conversion produces a populate error (500) |
| `class[0]` | BLOCKED (the `\[` alternative) | when populate is called directly — `InvalidPropertyException` → 500 |
| `classname`, `map(class)`, `foo('class')`, `foo[class]` | allowed | safe: the key/index does not participate in navigation (`map(class.classLoader)` lands as a key in the Map) |
| `nonExistent.deep.path` | allowed | `NotReadablePropertyException` is not caught → 500 (violation of the spec's "skipped without error") |
| lazy form, `class.classLoader` | allowed | DynaBean navigation looks at declared properties → skip (safe) |

Class instantiation by name (`RequestUtils.applicationInstance/applicationClass`, the `ConfigRuleSet`, Tiles, and `StrutsInstanceResolver` factories) is reachable only from configuration (struts-config, Spring properties) — class names do not come from HTTP input (all call sites checked). `ValidWhen` is an ANTLR visitor over expressions from validation configuration; only values come from the request. The Mimosa scan (61 findings) confirms: "HIGH" — client-side `eval` in the ported JS commons-validator (no server-side effect), "MEDIUM" — reflection from configuration strings.

## Goals / Non-Goals

**Goals**
- Restore spec compliance: populate silently skips unknown/unreadable names and any class access, without 500 errors.
- A second (independent) layer of defense against class traversal at the `FormPopulator` level.
- Eliminate the window in the multipart path where a field name is introspected before the CVE filter.
- Lock everything in with regression tests (the CVE filter is currently covered by no test at all).

**Non-Goals**
- Narrowing the nested-property navigation surface (the `nested.intProperty` scenario is locked in by the spec and the beanutils reference; the "allowlist of navigation roots" option — a candidate for a separate change).
- `String`→`Class` conversion for `Class`-typed properties (locked in by the spec as compatibility; the risk is only realizable for forms declaring a property of type `Class` — we document it, we do not remove it).
- Client-side JS commons-validator and configuration reflection (see proposal — out of scope).

## Decisions

### D1. Fail-silent navigation via the parent exception
In `FormPopulator.navigate()` and `descriptorOf()`, catch `InvalidPropertyException` (the common ancestor of `NotReadablePropertyException`, `NotWritablePropertyException`, `NullValueInNestedPathException`) rather than only the two specific subclasses. The alternative — pre-checking property existence — is rejected: it duplicates Spring's introspection and adds a desynchronization window. Catching the ancestor is resilient to changes in the Spring exception hierarchy.

### D2. Second layer: skipping `class` segments in `FormPopulator.populateProperty`
After parsing the name, the parameter is skipped entirely (silently) if any segment's name is `class` in any case. This closes the cases the regex layer lets through deliberately or by oversight: bare `class`, tail `x.class`, `servlet.class`, `class[0]`, `class(key)`. Alternatives: widen the `RequestUtils` regex (does not close the direct call to `FormPopulator.populate` and widens the regex with false-positive risks), or check only in the `String`→`Class` converter (too late — navigation has already happened). The two-layer approach is chosen deliberately: the layers are independent, and a regression in either one does not open the vector. Side effect: a lazy form no longer adds a `class` map entry — this aligns behavior with the beanutils reference (unknown property → skip).

### D3. Filter before introspection in the multipart path
In `RequestUtils.populate`, the CVE filter and the `org.apache.struts.*` skip are applied to the name before calling `rationalizeMultipleFileProperty` (currently the order is reversed), and `rationalizeMultipleFileProperty` itself becomes fail-silent on `InvalidPropertyException` (returns the value unchanged). A parameter name from the request must not reach `BeanWrapper` before filtering.

### D4. Regression matrix from the probe
- Regex-layer test: the 13 expressions of the matrix (BLOCKED/allowed).
- Populate-layer tests: class cases (bare/tail/`servlet.class`/`class[0]`/`class(key)`/`class.module.classLoader.DefaultAssertionStatus`) — silent skip, remaining parameters are populated; a static form, a DynaBean, and a lazy form.
- Fail-silent navigation tests: unknown intermediate (`nonExistent.deep.path`), unreadable intermediate (`nested.missing.child`), null intermediate (already covered by fixture tests).
- Multipart test: a `class.x` field is ignored before type resolution, remaining fields are populated.
Locations: `spring-struts1-core/src/test/java/org/apache/struts/util/` (next to the populate fixture tests). Existing fixture tests do not lock in the currently failing behavior (verified) — no changes needed.

## Risks / Trade-offs

- [Observable behavior change: anomalous names go from 500 to silent skip] → this is precisely the goal (spec and beanutils-reference compliance); fixture tests do not lock in the 500; the D4 matrix fixes the new behavior.
- [A legitimate form with a property named `class`] → such access matches the CVE-2014-0114 signature and is already partially cut off by the regex layer; the guard extends this to all positions. Documented in the `FormPopulator` javadoc; do not use `class` as a property name in forms.
- [The regex layer lets through `foo[class]`, `foo('class')`, `map(class)`] → structurally safe (index/key positions do not participate in navigation), locked in by tests as expected behavior.
- [A Spring upgrade changes the `BeanWrapper` exception hierarchy] → catching the parent `InvalidPropertyException` (D1) + behavioral matrix tests not tied to exception types.

## Migration Plan

A change without migration: rebuild and run the full module test suite. Rollback — revert the commit. Configuration and dependencies are unchanged.

## Open Questions

(none — the RCE reachability question is closed experimentally; the remaining options are recorded as Non-Goals or risks)
