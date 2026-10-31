# Design

## Context

Populate is the only section of the request-processing path that depends on the static global `ConvertUtils` registry of commons-beanutils: `RequestUtils.populate()` collects parameters into a `Map<String, Object>` and hands them to `BeanUtils.populate()` (RequestUtils.java:235); `PropertyUtils.getPropertyType` is used for multipart rationalization (RequestUtils.java:263). The remaining uses of beanutils are outside the populate path and do not change:

- **public API**: the `DynaBean` hierarchy (`DynaActionForm`, `DynaValidatorForm`, `LazyValidatorForm`, `BeanValidatorForm`, `DynaActionFormClass`) — the classes must implement the beanutils interfaces;
- **validator**: `FieldChecks` (`PropertyUtils.getProperty`), `BeanValidatorForm` (`ConvertUtils.convert` for the index);
- **configuration**: cloning/copying of configs (`ActionConfig`, `FormBeanConfig`, `ActionServlet`, `ActionConfigMatcher`), initial values (`FormPropertyConfig`).

Populate behavior is pinned: the ITs of all test applications — `TestBean` in `examples` covers primitives, arrays (`int[]`, `String[]`), indexed (`intIndexed[0]`), nested (`nested`), and multibox; dynamic forms in `cookbook`/`mailreader` — `String`, `Boolean`, `String[]`; multipart — `UploadForm`.

## Goals / Non-Goals

**Goals**
- Remove the static `ConvertUtils` registry from the populate path: conversion goes through an own `ConversionService` based on Spring, reproducing the observable beanutils behavior.
- A single conversion service for both assignment paths (JavaBean and DynaBean).

**Non-Goals**
- Removing the commons-beanutils dependency from the project (the public API remains).
- Replacing beanutils in the validator, configuration, and config cloning.
- An extension point for application-specific custom converters (a candidate for a separate change if needed after the migration).
- Any new conversion capabilities (for example, `java.util.Date` gets no converter — as it is today).

## Decisions

### 1. Own `StrutsConversionService` on top of Spring `ConversionService`

A new class `org.apache.struts.util.StrutsConversionService` — a service on `GenericConversionService` with an explicit set of converters reproducing the default commons-beanutils registry (behavior pinned by probe measurements on beanutils 1.11):

| Target type | Valid input | Incorrect/empty input |
|---|---|---|
| `Boolean`/`boolean` | literals `true`,`yes`,`y`,`on`,`1` / `false`,`no`,`n`,`off`,`0`, case-insensitive | `false` (zero value), no error |
| `Byte`,`Short`,`Integer`,`Long`,`Float`,`Double` (+ primitives) | trimmed, decimal parsing (hex `0x…` not supported) | zero value of the type (0, 0.0), no error, including overflow |
| `Character`/`char` | first character of the string | space `' '`, no error |
| `BigDecimal`, `BigInteger` | parsed | populate error |
| `String` | no conversion (from non-string sources — `toString`) | as is |
| `java.sql.Date`/`Time`/`Timestamp` | JDBC formats (`yyyy-MM-dd`, `HH:mm:ss`, `yyyy-MM-dd HH:mm:ss`) | populate error |
| `Class` | class loading by name | populate error |
| `File`, `URL` | path / URL | populate error (`URL`) |
| `Enum`, `java.util.Date`, `Calendar`, collections (`List`) | — NO converter — always a populate error (compatible with the default registry) | |

Arrays/collections: the target array is filled component by component (an incorrect element — the zero value of the component); a scalar target receives the first element of the parameter array — standard Spring `ObjectToArrayConverter`/`ArrayToObjectConverter` with element conversion through the same service. `FormFile` is assigned without conversion when the types match.

**Alternatives**: a plain `DefaultConversionService` — rejected (different Boolean literals without `y`/`n`, errors instead of zero values for numbers, support for hex and enum, which the beanutils registry lacks); keeping beanutils with per-request isolation — rejected (does not remove the static registry and does not integrate with Spring).

**The beanutils semantics are asymmetric for mapped properties — reproduced as is:** a top-level mapped property (`prop(key)`) receives the raw parameter value (a string array) without conversion; the combination of indexed and mapped (`a.b[0](key)`) puts the first element of the parameter as a string into the existing Map element; an undeclared property of a lazy form receives the first element of the parameter as a string. All of this is pinned by fixture tests.

### 2. JavaBean path: `BeanWrapper` + own beanutils expression parser

Assignment of values to JavaBean forms goes through `BeanWrapperImpl` with `StrutsConversionService`. The beanutils name syntax (`prop[0]`, `prop(key)`, `a.b`, combinations) is parsed by an own resolver (mirroring the semantics of beanutils `Resolver`: square brackets `[i]`, parentheses `(key)`, dots) and translated into a `BeanWrapper` path (the mapped key in quoted form `prop['key']`, so that any characters are allowed in the key).

**Alternative**: an own reflective setter — rejected (duplicates `BeanWrapper`).

### 3. DynaBean path: `DynaClass` metadata + the same service

Forms implementing `DynaBean` (`DynaActionForm`, `DynaValidatorForm`, `LazyValidatorForm`, `BeanValidatorForm`) are filled according to `DynaClass` metadata: the type from `DynaProperty` (including the array component) is converted by `StrutsConversionService`, assignment goes through `DynaBean.set(name[, index | key])`. For undeclared properties of lazy forms the value is assigned as is (the auto-add semantics of `LazyDynaBean`); unknown properties of other DynaBeans are skipped without error (like `BeanUtils.populate`).

**Alternative**: keep `BeanUtils.populate` for DynaBeans — rejected: conversion goes through the static `ConvertUtils`, so the risk remains precisely on dynamic forms.

### 4. Preserving the error contract and processing order

- Collecting parameters into a `HashMap` and the order before `BeanUtils.populate` do not change (entry iteration — as now).
- Assignment proceeds up to the first error; then `ServletException("BeanUtils.populate", cause)` — the message is preserved for handler/log compatibility.
- Existing checks before populate do not change: the `org.apache.struts.` prefix, the CVE-2014-0114 pattern, early return when the multipart limit is exceeded.
- `rationalizeMultipleFileProperty` switches from `PropertyUtils.getPropertyType` to `BeanWrapper.getPropertyType` — the populate path is fully free of beanutils.

### 5. Placement and lifecycle

The populate classes (`StrutsConversionService`, the assignment orchestrator) live in `org.apache.struts.util` next to `RequestUtils`; they depend only on `spring-core`. The service is an immutable singleton of the populate class (the converter set is fixed at startup, thread safety is provided by the statelessness of the converters); the public signatures of `RequestUtils.populate` do not change, so the service is not passed through parameters.

### 6. Test strategy: fixture matrix before replacement

First, unit tests on the current behavior are written (golden): the set of cases from the `form-populate` spec — Boolean literals, empty string, number boundaries, `Character`, `BigDecimal`, sql dates/`Calendar`, `Class`/`File`/`URL`, `Enum`, the absence of a converter for `java.util.Date`, indexed/mapped/nested names, scalar↔array, unknown properties, dyna types, lazy auto-add. The tests are green on the current implementation → populate is replaced → the same tests are green without edits. On top — the existing ITs of the applications (`./mvnw verify`) without changes.

## Risks / Trade-offs

- [The beanutils semantics are asymmetric (top-level mapped — raw `String[]`, nested-indexed-mapped — a string of the first element, zero values instead of errors for primitives)] → the entire matrix (including the asymmetries) is pinned by fixture tests BEFORE the replacement; any discrepancy is a red test, not a production surprise.
- [`BeanWrapper` does not understand `prop(key)` and converts Map values where beanutils puts the raw array] → the expression parser + own assignment rules for mapped branches (see decision 2); fixture tests on combinations.
- [`BeanValidatorForm` is a DynaBean facade with its own subtleties] → covered by a fixture test in the core IT; on a discrepancy, the metadata path is fixed, not the API.
- [Applications that registered custom converters in the static `ConvertUtils` will lose their effect in populate] → document in the README (the "Compatibility" section): the engine's populate does not read the static registry; the extension-point question is a separate change if needed.
- [The HashMap entry order makes the first-error point non-obvious] → keep the current parameter collection into a `HashMap` unchanged; the order is identical to today's.

## Migration Plan

1. Write fixture tests for the current populate (green on the beanutils implementation).
2. Introduce `StrutsConversionService` + the new populate, switch `RequestUtils` to them.
3. Fixture tests without edits — green; `./mvnw verify` (all application ITs) — green.
4. Update the README (the "Compatibility" section): populate on Spring `ConversionService`, the static registry is not used.
5. Rollback — revert the commit; there are no external artifacts or data.

## Open Questions

- The growth behavior of `List` on indexed assignment beyond its size is not pinned (no fixture test for this case; it occurs rarely). To be clarified if discovered in the application ITs; it does not affect the approach.
