# Tasks

## 1. Fixture tests for the current populate (golden)

- [x] 1.1 Write unit tests for scalar conversion against the current implementation (`RequestUtils.populate`): Boolean literals (`true/yes/y/on/1`, `false/no/n/off/0`, case-insensitive, invalid literal — error), numbers of all primitive types and wrappers, empty string into a number — error, `Character` (first character), `BigDecimal`/`BigInteger`, `Enum`, `String` without conversion, `java.util.Date` — error (no converter). Verify: `mvnw test -pl spring-struts1-core` green on the current beanutils implementation
- [x] 1.2 Write tests for property names: simple, indexed `prop[0]`, mapped `prop(key)`, nested `a.b`, combination `a.b[0](key)`, unknown name — skipped, `org.apache.struts.*` — skipped, the `class` pattern (CVE-2014-0114) — ignored. Verify: the same tests green on the current implementation
- [x] 1.3 Write tests for arrays and collections: `String[]` → array/list component by component, `int[]` from multibox parameters, a single parameter into a scalar (first element), `FormFile` from multipart (modeled after `RequestUtilsMultipartTest`). Verify: tests green on the current implementation
- [x] 1.4 Write tests for the DynaBean path: `DynaActionForm` with `String`/`Boolean`/`Integer`/`String[]` types, a mapped property of a dyna form, `LazyValidatorForm` — an undeclared property is added as is, a declared one is converted. Verify: tests green on the current implementation

## 2. Conversion service and expression parser

- [x] 2.1 Implement `StrutsConversionService` (`org.apache.struts.util`): a `DefaultConversionService` configuration with converters of beanutils semantics per the matrix from design.md (Boolean literals, numbers with an error on an empty string, `Character`, `BigDecimal`/`BigInteger`, `java.sql.Date/Time/Timestamp` and `Calendar` with beanutils patterns, `Class`/`File`/`URL`, `Enum`; no `java.util.Date`). Verify: unit tests of the service across the entire conversion matrix green
- [x] 2.2 Implement the beanutils expression parser (simple/`[i]`/`(key)`/dots/combinations) with translation into the `BeanWrapper` path (mapped key in quoted form). Verify: unit tests of the parser on all name forms from fixture test 1.2 green

## 3. New populate

- [x] 3.1 Implement the JavaBean assignment path via `BeanWrapper` with `StrutsConversionService`: component-by-component arrays/collections, scalar-first-element, skipping unknown properties, stopping at the first error. Verify: fixture tests 1.1–1.3 green already on the new path (via the public `RequestUtils.populate`)
- [x] 3.2 Implement the DynaBean path on `DynaClass` metadata with the same service: conversion per `DynaProperty`, assignment via `set(name[, index | key])`, auto-add for lazy forms, skipping undeclared ones for regular DynaBeans. Verify: fixture tests 1.4 green on the new path
- [x] 3.3 Switch `RequestUtils` to the new populate: remove `BeanUtils.populate` and `PropertyUtils` from the populate path (`rationalizeMultipleFileProperty` — via `BeanWrapper`), keep `ServletException("BeanUtils.populate", e)`, parameter collection into a `HashMap`, the `org.apache.struts.*`/CVE checks, and the multipart-limit early return. Verify: `grep beanutils spring-struts1-core/src/main/java/org/apache/struts/util/RequestUtils.java` is empty; fixture tests 1.1–1.4 green without edits

## 4. Integration verification

- [x] 4.1 Run the full set of application integration tests without changes: `./mvnw verify` — all ITs (`blank`, `examples`, `cookbook`, `mailreader`, `demo`, starter) green. Verify: the build is green, surefire reports have no failures
- [x] 4.2 Confirm that the populate path does not depend on the static registry: a test registering a custom converter in `ConvertUtils` does not affect populate. Verify: such a test is green (populate ignored the registry)

## 5. Documentation

- [x] 5.1 Update the README (the "Compatibility" section): populate is performed by the engine's conversion service on Spring `ConversionService`; the static `ConvertUtils` registry does not participate in populate (custom converters in it do not affect populate). Verify: the README section describes the new behavior
- [x] 5.2 Mark in ROADMAP.md the item "move to Spring ConversionService" as implemented (the `populate-conversion-service` change). Verify: the roadmap line references the completed change
