# Design

## Context

The engine runs the classic Struts 1 pipeline (`StrutsHandlerAdapter.handle`) per request. A read-only pass over `spring-struts1-core` confirmed the hot spots the proposal lists (see proposal.md — Why): every `handle()` call resolves the action through `StrutsInstanceResolver`, which calls `Class.forName`/`loadClass` plus `applicationContext.getBeanNamesForType(clazz)` — an O(bean-definitions) type scan — and every forward calls `getBeansOfType(DefinitionForwardResolver.class)` in `resolveDefinitionForward`. Shared caches (`MessageResources.formats`, `DispatchAction.methods`, `ActionDispatcher.methods`, `LookupDispatchAction.localeMap`) sit behind `synchronized` blocks and serialize threads even after warm-up. `FormPopulator` re-parses every parameter name with `StrutsExpressionParser` and re-scans `getMethods()` for indexed writes per element. `ModuleConfigImpl.findActionConfig` and `ResponseUtils.filter` are already map-backed / lazily-buffered — no action needed there.

Constraints: Java 17, Maven multi-module, Spring 7 (Boot 4 BOM); the `ci-pipeline` spec keeps CI as build+tests; library consumers must not get new dependencies.

## Goals / Non-Goals

**Goals:**
- A reproducible JMH suite covering the per-request hot paths, runnable by one documented command, excluded from build/CI execution.
- A measured baseline recorded before any code change, and after-numbers for comparison.
- Lock-free reads on shared per-request caches; one-shot resolution of static Spring lookups — all behavior-preserving.

**Non-Goals:**
- End-to-end container benchmarks (Tomcat/JMeter load tests); taglib rendering optimization; changes to populate/conversion semantics; CI integration of benchmarks; benchmark result gating (regression thresholds).

## Decisions

**D1. JMH in a dedicated module `spring-struts1-benchmarks`, executed via `exec-maven-plugin`.**
The module joins the root reactor (so it compiles with a normal build, at negligible cost) but contains no tests; JMH runs only through the explicit command `mvn -pl spring-struts1-benchmarks exec:java` (main class `org.openjdk.jmh.Main`), with JSON results exported for the docs. Alternatives: a hand-rolled timing harness (no statistical rigor, JIT pitfalls), JUnit-based timing (noisy, entangled with the test run), a Maven profile that excludes the module (unnecessary — compiling is not executing).

**D2. Benchmarks drive the real classes through spring-test mocks, not a container.**
`MockHttpServletRequest`/`MockHttpServletResponse`/`MockServletContext` over a real `StrutsHandlerAdapter` + programmatic `ModuleConfig` measure engine code in isolation; `forward` lands on a `MockRequestDispatcher`. Suites: full pipeline (form populate + execute + forward), `RequestUtils.populate`, `MessageResources.getMessage(locale, key, args)`, `DispatchAction` dispatch, `StrutsInstanceResolver.resolveAction/resolveForm` over a minimal `AnnotationConfigApplicationContext`. Parameters (property count, message args) fixed to representative defaults and documented. Absolute numbers differ from container round-trips — the docs will say this; the suite's purpose is before/after deltas of engine code.

**D3. Baseline first, code changes after.**
Tasks enforce the order: build module → run baseline on unmodified code → record → apply optimizations → full test suite → re-run → record after-numbers. Without this order the comparison is not honest.

**D4. `StrutsInstanceResolver`: cache type → bean-name resolution per resolver instance.**
Two `ConcurrentHashMap`s: loaded `Class` by type name, and the `beanNames` lookup result by type name. Only successful lookups are cached; failure paths (multiple beans → `IllegalStateException`) re-evaluate on each call, preserving today's error behavior. Instance semantics untouched — the cache holds lookups, never instances, so prototype forms still get a fresh `createBean`/`getBean` per request and singleton actions keep one instance. Alternatives: caching at `ActionConfig`/`FormBeanConfig` level (rejected — config classes are Spring-agnostic; coupling belongs in the resolver); per-mapping resolved-instance memoization (rejected — would break prototype semantics). Limitation accepted and documented: bean definitions registered after the first resolution are not seen (the engine registers its beans during startup; dynamic late registration was never a supported flow).

**D5. `StrutsHandlerAdapter.resolveDefinitionForward`: resolve the resolver list once, lazily.**
A `volatile List<DefinitionForwardResolver>` filled on first use from `getBeansOfType` (empty list allowed). Lazy rather than `@PostConstruct`-eager because the application context reference arrives via `ActionServlet` at runtime and ordering is not worth coupling to. Same limitation as D4: resolvers added to the context after the first forward are not picked up.

**D6. `MessageResources`: `ConcurrentHashMap` for the formats cache.**
`computeIfAbsent` on a `ConcurrentHashMap` replaces the `synchronized (formats)` block; `format.format(args)` stays outside any lock — which is exactly today's semantics (the current code already formats outside the synchronized block, so concurrent `format()` on a shared `MessageFormat` is a pre-existing property of the ported design, unchanged here). Alternatives: synchronizing the format call itself (re-introduces thread serialization on hot keys), `ThreadLocal<MessageFormat>` per key (memory grows with threads × keys).

**D7. Dispatch-action caches: `ConcurrentHashMap`, lock-free reads.**
`DispatchAction.methods` and `ActionDispatcher.methods` use a `get`/`putIfAbsent` pattern (a `computeIfAbsent` lambda would have to smuggle the checked `NoSuchMethodException` across its boundary and change the thrown type); `LookupDispatchAction.localeMap` uses `computeIfAbsent` (the initializer captures the request, as today). Rationale: these are per-action-instance maps hit on every dispatch request; lock-free reads remove cross-thread serialization, and the write path (warm-up) is rare. `TokenProcessor` stays synchronized — its operations are infrequent and per-session, not hot.

**D8. `FormPopulator`: two small caches.**
Parsed parameter-name segments cached by raw name (`StrutsExpressionParser` is stateless; empty results cached too), and indexed-write `Method` resolution cached per (class, method name) with an `Optional.empty()` marker for absent methods. Both caches are static `ConcurrentHashMap`s capped at 1024 entries each: parameter names arrive with requests, so the cap stops floods of unique attacker-chosen names from growing the maps unboundedly (legitimate forms fit far below it). If the benchmark shows the segments cache alone carries the win, the method cache can be dropped — each cache is an independent, revertible unit.

## Risks / Trade-offs

- [Dev-machine noise makes absolute numbers unstable] → Report before/after deltas from the same environment; fixed JMH params (warmup/iterations/forks) documented in `docs/performance.md`; treat numbers as relative, not absolute.
- [Caches freeze late bean registrations (D4/D5)] → Document the startup-only registration contract; it matches how the engine registers beans today. Revert path is a single-file change per cache.
- [`MessageFormat` concurrent-format race is visible in the new code too] → Pre-existing semantics, explicitly documented in `docs/performance.md`; fixing it (per-call instances or locking) is a separate change.
- [Benchmark module adds reactor compile time and JMH deps to the build tree] → Benchmark-scoped only; no artifact consumers see them; compile cost is one small module.
- [Optimizations regress semantics] → The full existing test suite (including `dispatch-actions`, `spring-bean-integration`, `message-resources`, `form-populate` behaviors) must stay green before after-numbers are recorded.

## Migration Plan

No deployment impact: internal code changes plus an additive module. Rollback is per-commit revert. Library release notes may mention the improved per-request overhead.

## Open Questions

None — the remaining unknowns (exact JMH parameter values, whether the indexed-write method cache earns its keep) are resolved by running the benchmarks during apply and do not change specs or scope.
