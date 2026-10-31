# Proposal

## Why

The bridge recreates the whole Struts 1 request pipeline on Spring MVC, so per-request overhead is a product property users will notice under load. There is no benchmark suite and no measured baseline, and a first read-only pass over the hot paths already surfaced concrete per-request costs: `StrutsInstanceResolver` re-scans bean definitions (`getBeanNamesForType`) on every action/form resolution, `StrutsHandlerAdapter.resolveDefinitionForward` calls `getBeansOfType` on every forward, and shared caches in `MessageResources` and the dispatch actions sit behind global `synchronized` blocks that serialize threads. Optimizing without a measured baseline is guesswork, and the optimizations themselves need a way to prove they do not regress.

## What Changes

- Add a JMH benchmark module `spring-struts1-benchmarks` covering the per-request hot paths of `spring-struts1-core`: the handler adapter pipeline (populate → validate → execute → forward), form population, message formatting, dispatch-action method resolution, and action/form instance resolution. The module is not part of the default build/CI run; benchmarks are executed by an explicit documented command.
- Record a baseline of the current code with the benchmark suite and document it in `docs/performance.md` (environment, numbers, how to reproduce).
- Apply conservative, behavior-preserving optimizations at the confirmed hot spots:
  - `StrutsInstanceResolver`: cache class loading and type → bean-name resolution instead of re-scanning bean definitions per request; instance semantics (singleton action, fresh form per request) unchanged.
  - `StrutsHandlerAdapter.resolveDefinitionForward`: resolve registered `DefinitionForwardResolver` beans once at startup instead of `getBeansOfType` per forward.
  - `MessageResources`: replace the global `synchronized (formats)` map with a `ConcurrentHashMap` so message formatting does not serialize threads.
  - Dispatch actions (`DispatchAction`, `ActionDispatcher`, `LookupDispatchAction`): replace `synchronized` reflection caches with `ConcurrentHashMap`.
  - `FormPopulator`: cache parsed parameter-name segments and indexed-write method lookups (per class) instead of re-parsing/re-scanning per parameter.
- Verify the full existing test suite stays green, re-run the benchmarks, and record after-numbers next to the baseline in `docs/performance.md`.

Out of scope: rewriting the populate/conversion layer, tag library rendering optimization (benchmarks will measure it only as context), running benchmarks in CI, public API changes, and any semantic change to request processing.

Assumptions: JMH is the harness (standard for JVM microbenchmarks; added benchmark-scoped only, no new dependencies for library users); message-format `MessageFormat` instances stay cached per locale+key (the format call outside the map lock is the pre-existing semantics of the current code).

## Capabilities

### New Capabilities

- `performance-benchmarks`: the library's benchmark suite — a JMH harness covering the request-pipeline hot paths, how it is run, how it stays out of the default build and CI, and where baseline/after results are recorded.

### Modified Capabilities

- `spring-bean-integration`: adds a requirement that action/form resolution must not re-scan bean definitions per request — lookups are cached while preserving singleton-action / prototype-form lifecycle semantics and user-registration priority.
- `docs-site`: the page set grows from six to seven pages — a performance page documenting the benchmark run command and the recorded measurement results.

## Impact

- **New files**: `spring-struts1-benchmarks/` Maven module (pom, JMH benchmark sources, `README`-level run instructions), `docs/performance.md` (baseline + after results, reproduction instructions), site nav entry for the new doc page.
- **Modified files**: root `pom.xml` (module registration), `spring-struts1-core` sources `StrutsInstanceResolver.java`, `StrutsHandlerAdapter.java`, `MessageResources.java`, `DispatchAction.java`, `ActionDispatcher.java`, `LookupDispatchAction.java`, `FormPopulator.java`; `docs/mkdocs.yml` nav.
- **Dependencies**: JMH (`jmh-core`, `jmh-generator-annprocess`) and `exec-maven-plugin` added to the new module only — no dependency changes for library consumers.
- **No changes** to public APIs, request-processing semantics, or the `ci.yml` pipeline.
