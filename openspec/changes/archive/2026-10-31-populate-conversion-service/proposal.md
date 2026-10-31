# Proposal

## Why

Form populate is the only place in the engine where request processing depends on the static global `ConvertUtils` registry of commons-beanutils: a known risk from the roadmap (registration collisions between applications, hidden mutability, no way to configure via Spring). The roadmap twice postponed moving populate to Spring `ConversionService` (phase 1, phase 3); all phases are now complete — a window has opened for a regression-free refactoring: behavior is already pinned by integration tests of all test applications (`blank`, `examples`, `cookbook`, `mailreader`, `demo`).

## What Changes

- New engine-level populate on top of Spring `ConversionService`: an own `ConversionService` with a set of converters reproducing the semantics of the default commons-beanutils registry (primitives and wrappers, `BigDecimal`/`BigInteger`, Boolean literals, `String`, arrays, `FormFile`).
- Support for parameter names in beanutils syntax: simple, indexed (`prop[0]`), mapped (`prop(key)`), nested (`a.b.c`), and their combinations; unknown properties are skipped without errors (like `BeanUtils.populate`).
- Two value-assignment paths: JavaBean forms — via `BeanWrapper`; `DynaBean` forms (`DynaActionForm`, `DynaValidatorForm`, `LazyValidatorForm`, `BeanValidatorForm`) — via `DynaClass` metadata with conversion by the same `ConversionService`.
- `RequestUtils` stops calling `BeanUtils.populate`/`PropertyUtils` in the populate path; the public API `RequestUtils.populate` is preserved unchanged.
- A conversion error still results in a `ServletException` (the `BeanUtils.populate` message is preserved for log/handler compatibility).
- commons-beanutils remains a dependency of the engine: the public API (the `DynaBean` hierarchy of dynamic forms, `FieldChecks`, configuration cloning) is untouched; only the populate path changes.

## Capabilities

### New Capabilities

- `form-populate`: form-filling semantics — value sources (request parameters and multipart elements), property name syntax, value conversion rules (the set of supported types and literals, empty-string behavior, scalar/array), handling of DynaBean forms and conversion errors.

### Modified Capabilities

- `request-processing`: the populate requirement — commons-beanutils semantics are reproduced by the engine on Spring `ConversionService` (tied to the `form-populate` spec); the observable lifecycle behavior does not change.
- `dynamic-form-beans`: the dynamic-form populate requirement — the same restatement; dynamic forms are filled via `DynaClass` metadata with conversion by the same `ConversionService`.

## Impact

- **Code**: `spring-struts1-core` — `RequestUtils` (populate path), a new populate class + `ConversionService` (package `org.apache.struts.util`), tests. Not affected: `StrutsHandlerAdapter` (the call to `RequestUtils.populate` remains), tag libraries, starter, validator.
- **API**: the public API does not change (`RequestUtils.populate` with the same signatures; the `DynaBean` hierarchy stays on beanutils).
- **Dependencies**: `commons-beanutils` remains (public API); the engine's populate path no longer uses it. `spring-core` (ConversionService) is already available transitively via `spring-webmvc`.
- **Compatibility**: the observable populate behavior must remain identical — the existing ITs of all test applications pin this without changes, plus new fixture tests for converter semantics.
