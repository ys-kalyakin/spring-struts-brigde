# Proposal

## Why

The project has no automated style or static bug checks: formatting and coding-convention drift is only caught by eye during review, and latent bug patterns (null handling, mutable shared state, resource misuse, suspicious calls) reach `main` unnoticed. Adding Checkstyle and SpotBugs to the Maven build makes both classes of problems machine-checked, and fixing the current findings establishes a clean baseline that every later commit has to preserve.

## What Changes

- Add `maven-checkstyle-plugin` to the parent POM, bound to the `verify` phase for every module, with a pragmatic custom `checkstyle.xml` matching the current project style (4-space indent, ~120-column lines, import order and hygiene, naming, whitespace, block layout). The build fails on violations.
- Add `spotbugs-maven-plugin` to the parent POM, bound to the `verify` phase for every module, at max effort / medium threshold. The build fails on findings.
- Exclude generated code from both checks (ANTLR-generated validwhen parser in core, JMH-generated classes in benchmarks).
- Introduce a documented `spotbugs-exclude.xml` holding deliberate, justified exclusions for patterns that are intentional in a Struts 1 API port (e.g., deliberately mutable configuration objects); every exclusion carries a rationale.
- Fix all style violations and SpotBugs findings reported on the first run so `./mvnw verify` passes with zero violations and zero findings.
- CI saves the Checkstyle and SpotBugs reports as run artifacts so a failed check can be diagnosed from the run (delta to `ci-pipeline`).

## Capabilities

### New Capabilities

- `static-analysis`: The Maven build enforces code style (Checkstyle) and bug patterns (SpotBugs) uniformly across all modules; covers the ruleset location, generated-code exclusions, the documented suppression policy, and the zero-findings gate on `verify`.

### Modified Capabilities

- `ci-pipeline`: CI additionally saves Checkstyle and SpotBugs result reports as run artifacts (regardless of build result), mirroring the existing surefire-report saving, so static-analysis failures are diagnosable from the run.

## Impact

- **Build config**: parent `pom.xml` (plugin versions managed, plugins bound to `verify`); no module POM changes expected beyond possibly none — plugins inherit from the parent.
- **New files**: `config/checkstyle.xml` (ruleset), `config/spotbugs-exclude.xml` (documented exclusions).
- **Sources**: mechanical fixes across all six modules (~500 Java files in scope) — imports, whitespace, naming, and targeted refactors for SpotBugs findings; generated code excluded.
- **CI**: `.github/workflows/ci.yml` gains an upload step for the analysis reports.
- **Behavior**: no runtime behavior change intended; SpotBugs-driven fixes must not alter observable behavior. Build time increases slightly (analysis on `verify`).
- **Docs**: README gains a short "Code quality checks" note (how to run locally, where exclusions live).
