# Performance

The library ships a JMH benchmark suite that measures the per-request hot
paths of the request-processing engine. It exists so that optimizations rely
on numbers instead of guesses, and so that a change can prove it did not
regress.

## What is measured

| Benchmark | Measures |
|---|---|
| `RequestPipelineBenchmark.handleFullPipeline` | the full per-request cycle: locale/session processing, form creation through the Spring context, populate, execute, forward |
| `FormPopulateBenchmark.populateScalar` | assignment of twelve scalar parameters to a form |
| `FormPopulateBenchmark.populateIndexed` | the indexed expression path (`items[0]`, `tags[1]`) |
| `MessageResourcesBenchmark.lookupPlain` | message lookup without arguments |
| `MessageResourcesBenchmark.formatTwoArgs` | parametric replacement through the `MessageFormat` cache |
| `DispatchBenchmark.dispatchByParameter` | the classic dispatch-action cycle: method name from the request, reflection cache lookup, invocation |
| `InstanceResolveBenchmark.resolveAction` | action resolution through the Spring context |
| `InstanceResolveBenchmark.resolveForm` | request-scoped form resolution (a fresh instance per call) |

Benchmarks drive the real engine classes over spring-test mocks
(`MockHttpServletRequest`/`MockHttpServletResponse`/`MockServletContext`),
not a servlet container: absolute numbers are engine-code costs and differ
from container round-trips. Their purpose is comparing engine code before
and after a change.

## How to run

```bash
mvn -pl spring-struts1-benchmarks -am -Pjmh package
```

The `-Pjmh` profile exists only in the benchmark module: the normal build
(`mvn package`, `mvn test`, CI) never executes benchmarks, and no published
artifact carries a JMH dependency.

Options:

- `-Djmh.include=<regex>` — run a subset (e.g.
  `-Djmh.include=MessageResources`);
- `-Djmh.forks`, `-Djmh.warmupIterations`, `-Djmh.measurementIterations` —
  run size;
- `-Djmh.results=<file>` — result file (JSON, written into
  `spring-struts1-benchmarks/`), default `jmh-result.json`.

## Fixed run parameters

All numbers below were produced with the suite defaults: 2 forks, 3 warmup
iterations of 1 s, 5 measurement iterations of 1 s, average time per
operation (`avgt`), fail-on-error enabled.

## Environment

- CPU: AMD Ryzen 7 5700X3D 8-Core (16 threads)
- JVM: OpenJDK 25.0.4.1 (Red Hat), code compiled with `--release 17`
- OS: Fedora Linux 44 (kernel 7.2.11-200.fc44.x86_64)
- Date: 2026-10-31

Treat the numbers as relative before/after deltas measured on one machine,
not as absolute guarantees.

## Results: baseline vs optimized

The baseline was captured on the unmodified code, the optimized run after
the hot-path changes (cached bean-name resolution, cached definition-forward
resolvers, lock-free message and dispatch caches, cached populate
parsing). Raw results: `spring-struts1-benchmarks/baseline.json` and
`spring-struts1-benchmarks/after.json`.

| Benchmark | Baseline | Optimized | Δ |
|---|---|---|---|
| `RequestPipelineBenchmark.handleFullPipeline` | 5.27 µs | 4.46 µs | **−15.4%** |
| `FormPopulateBenchmark.populateScalar` | 4.85 µs | 4.44 µs | −8.5% |
| `FormPopulateBenchmark.populateIndexed` | 11.51 µs | 10.42 µs | −9.5% |
| `MessageResourcesBenchmark.lookupPlain` | 36.27 ns | 34.43 ns | −5.1% |
| `MessageResourcesBenchmark.formatTwoArgs` | 634.52 ns | 607.14 ns | −4.3% |
| `DispatchBenchmark.dispatchByParameter` | 315.75 ns | 284.59 ns | −9.9% |
| `InstanceResolveBenchmark.resolveAction` | 183.38 ns | 28.46 ns | **−84.5%** |
| `InstanceResolveBenchmark.resolveForm` | 984.62 ns | 725.92 ns | −26.3% |

The largest wins are where whole-context scans disappeared per request:
action/form resolution no longer walks every bean definition
(`resolveAction` −84%), and every request pays no definition-forward
resolver lookup. `resolveForm` keeps a Spring prototype `createBean` per
request by design (a fresh form instance per request is a compatibility
requirement), which bounds its gain.

## Caching contract

Two lookups are resolved once and cached: type → bean names in the instance
resolver, and the definition-forward resolver list in the request adapter.
Beans or resolvers registered in the Spring context after the first request
are not picked up — the engine registers its own beans during startup, and
explicit user registration happens before request processing. The populate
caches (parsed parameter names, indexed write methods) are capped at 1024
entries each so that floods of unique attacker-chosen parameter names cannot
grow them unboundedly.

## Note on concurrent message formatting

The `MessageFormat` cache is a concurrent map, and `format()` runs outside
any lock. Concurrent `format()` calls on the same cached `MessageFormat`
instance were already possible in the ported classic code (the format call
sat outside the synchronized block there as well) and remain the semantics;
changing that would be a separate compatibility-affecting change.
