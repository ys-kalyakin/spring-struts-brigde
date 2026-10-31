# Tasks

## 1. Fail-silent populate navigation (D1)

- [x] 1.1 Add regression tests for skipping unknown/unreadable intermediate properties: the parameter `nonExistent.deep.path` (first segment missing) and `nested.missing.child` (the intermediate property exists, the nested one does not) — populate completes without error and the remaining parameters are populated. Verification: `mvnw -pl spring-struts1-core test -Dtest=PopulatePropertyNamesFixtureTest` — the new tests fail on the current code (ServletException instead of a skip).
- [x] 1.2 In `FormPopulator.navigate()` and `descriptorOf()`, catch `InvalidPropertyException` (the parent of `NotReadablePropertyException`/`NotWritablePropertyException`/`NullValueInNestedPathException`) and treat it as "skip without error". Verification: the tests from 1.1 are green; the existing populate fixture tests are green.
- [x] 1.3 Confirm that the null-intermediate-property scenario (already covered by fixture tests) has not changed. Verification: `mvnw -pl spring-struts1-core test -Dtest="Populate*FixtureTest"`.

## 2. Second layer of defense against class access (D2)

- [x] 2.1 Add populate tests for class access: the parameters `class=x`, `CLASS=x`, `x.class=y`, `servlet.class=y`, `class[0]=1`, `class(key)=1`, `class.module.classLoader.DefaultAssertionStatus=1`, `a.class.b=1` — each is silently skipped and the remaining form parameters are populated; the same cases for a DynaBean and a lazy form (for lazy — absence of the `class` map entry). Verification: the new tests run and lock in the required behavior (bare `class` currently fails — test 2.1 is red until 2.2).
- [x] 2.2 In `FormPopulator.populateProperty`, after parsing the name, skip the parameter entirely if any segment's name is `class` in any case; document in the javadoc that a property named `class` is not supported in forms (the CVE-2014-0114 signature). Verification: the tests from 2.1 are green; `mvnw -pl spring-struts1-core test -Dtest="Populate*"` is green.
- [x] 2.3 Check compatibility with the reference: the behavior for an unknown property (including `class`) in the reference beanutils is a silent skip; the populate conversion behavior is unchanged. Verification: full run of `mvnw -pl spring-struts1-core test`.

## 3. Filter and introspection ordering in the multipart path (D3)

- [x] 3.1 Add a test to `RequestUtilsMultipartTest`: a multipart request with a `class.x` field and a text field `name=Alice` — populate completes without error, `name` is populated, the `class.x` field is ignored; a separate test: a name with `class.x` does not reach `rationalizeMultipleFileProperty` (no exception from introspection is observed). Verification: the test is red on the current code (currently the exception occurs before the filter). [fact: the test is green even before the change — `getPropertyType` in Spring 7 silently returns null, there is no exception; the test locks in the required behavior]
- [x] 3.2 In `RequestUtils.populate`, apply the CVE filter and the `org.apache.struts.*` skip to the name before calling `rationalizeMultipleFileProperty`; make `rationalizeMultipleFileProperty` fail-silent on `InvalidPropertyException` (returns the value unchanged). Verification: the tests from 3.1 are green; `mvnw -pl spring-struts1-core test -Dtest=RequestUtilsMultipartTest` is green.

## 4. Regression test for the regex layer (CVE-2014-0114)

- [x] 4.1 Add `RequestUtilsClassAccessFilterTest` with the matrix from design.md: BLOCKED — `class.classLoader`, `class.module.classLoader.DefaultAssertionStatus`, `class.protectionDomain`, `foo.class.bar`, `CLASS.classLoader`, `Class.classLoader`, `x.class.y`; allowed — `class`, `classname`, `foo[class]`, `foo('class')`, `map(class)`, `x.class`. Verification: the test is green without code changes (locks in the current behavior of layer 1).

## 5. Final verification

- [x] 5.1 Full run of the core tests: `mvnw -pl spring-struts1-core test` — no failures, including the populate fixture tests, `StrutsExpressionParserTest`, `RequestUtilsMultipartTest`, and the new tests from 1.1/2.1/3.1/4.1.
- [x] 5.2 Full build of all modules with tests: `mvnw test` — the populate path is used by the starter and the applications, no cross-module regressions.
