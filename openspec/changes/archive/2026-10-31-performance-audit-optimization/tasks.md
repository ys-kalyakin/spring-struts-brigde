# Tasks

## 1. Benchmark module

- [x] 1.1 Create the `spring-struts1-benchmarks` Maven module (pom with `jmh-core`, `jmh-generator-annprocess`, `exec-maven-plugin` bound to a `jmh` execution running `org.openjdk.jmh.Main`, dependencies on `spring-struts1-core` and spring-test mocks) and register it in the root `pom.xml` modules; verify `mvn -q -pl spring-struts1-benchmarks -am package` compiles the module and `mvn -q package` still builds all modules without running any benchmark.
- [x] 1.2 Implement the benchmark sources per design D2: `RequestPipelineBenchmark` (full `StrutsHandlerAdapter.handle` with form populate, execute, forward via `MockServletContext`/`MockRequestDispatcher`), `FormPopulateBenchmark` (`RequestUtils.populate` on a form with a fixed representative parameter set), `MessageResourcesBenchmark` (`getMessage(locale, key, args)`), `DispatchBenchmark` (`DispatchAction` method dispatch), `InstanceResolveBenchmark` (`resolveAction`/`resolveForm` over a minimal `AnnotationConfigApplicationContext`); verify `mvn -pl spring-struts1-benchmarks exec:java -Djmh.regex=...` runs each benchmark class in isolation and prints scores.
- [x] 1.3 Run the full suite on the unmodified code with fixed JMH parameters (documented warmup/iterations/forks) and save raw results to `spring-struts1-benchmarks/baseline.json`; verify the file exists and contains one result per benchmark.

## 2. Hot-path optimizations

- [x] 2.1 Cache type → bean-name resolution in `StrutsInstanceResolver` per design D4 (two `ConcurrentHashMap`s for class and beanNames lookup, successful results only); verify with the existing `spring-bean-integration`/`spring-struts1-core` tests: `mvn -pl spring-struts1-core test` green, including scenarios for singleton action reuse, fresh form per request, and user-registration priority.
- [x] 2.2 Cache the `DefinitionForwardResolver` list in `StrutsHandlerAdapter.resolveDefinitionForward` per design D5 (lazy `volatile` list, empty list allowed); verify `mvn -pl spring-struts1-core test` green, including the tiles/definition-forward tests.
- [x] 2.3 Replace the `synchronized (formats)` block in `MessageResources` with a `ConcurrentHashMap` per design D6, keeping `format.format(args)` outside any lock; verify `mvn -pl spring-struts1-core test` green, including `message-resources` behavior tests.
- [x] 2.4 Convert `DispatchAction.methods`, `ActionDispatcher.methods`, and `LookupDispatchAction.localeMap` to `ConcurrentHashMap.computeIfAbsent` per design D7; verify `mvn -pl spring-struts1-core test` green, including `dispatch-actions` behavior tests.
- [x] 2.5 Add the `FormPopulator` caches per design D8 (parsed segments by raw parameter name, indexed-write `Method` per class+property with null-marker); verify `mvn -pl spring-struts1-core test` green, including `form-populate` and `populate-expression-security` behavior tests.

## 3. Verification and results

- [x] 3.1 Run the full test suite across all modules (`mvn test`) and verify it is green with no skipped-for-failure modules.
- [x] 3.2 Re-run the benchmark suite on the optimized code with the same JMH parameters and save results to `spring-struts1-benchmarks/after.json`; verify every benchmark is present and no score regressed beyond noise (re-run individual benchmarks to confirm any suspect outlier).
- [x] 3.3 Write `docs/performance.md`: how to run the suite (the exact command), fixed JMH parameters, measurement environment (CPU, JVM, OS), and a before/after table per benchmark from `baseline.json`/`after.json`; add the page to `mkdocs.yml` nav; verify `mkdocs build` succeeds and the documented run command works as written.
- [x] 3.4 Verify spec-level isolation and reproducibility requirements: ordinary `mvn package`/`mvn test` triggers no benchmark execution and published artifacts carry no JMH dependencies (inspect `mvn -pl spring-struts1-starter dependency:tree` or equivalent); note the pre-existing concurrent-`MessageFormat` semantics in `docs/performance.md` as design D6 documents.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
