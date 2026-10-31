# Design

## Context

Maven multi-module build (parent `spring-struts1-parent`, modules core, taglib, tiles, starter, apps + 6 app submodules, benchmarks), JDK 17, built with the Maven wrapper. The parent POM manages only surefire; no static analysis exists today. CI runs `./mvnw -B -ntp verify` on JDK 17 and uploads surefire reports and build artifacts. Two known generated sources exist: the ANTLR-generated validwhen parser in `org.apache.struts.validator.validwhen` (core) and JMH-generated classes in `org.apache.struts.bench.jmh_generated` (benchmarks). See proposal.md for motivation and specs/static-analysis for the behavior contract.

## Goals / Non-Goals

**Goals:**
- Checkstyle and SpotBugs enforced on `verify` for every module, inheriting from the parent POM with no per-module plugin declarations.
- A pragmatic Checkstyle ruleset matching current style: minimal diff churn, no mass reformat.
- Zero findings at the end of the change: all violations either fixed or covered by a documented exclusion.
- Reports in `target/` per module so CI (and developers) can diagnose failures.

**Non-Goals:**
- Other analyzers (PMD, ErrorProne, NullAway, findsecbugs) — later changes can add them on the same pattern.
- Javadoc/license-header rules, full import reordering, Google-style reformat — excluded deliberately to keep churn minimal.
- Changing runtime behavior of any module; fixes must be behavior-preserving.
- IDE configuration (IntelliJ/Eclipse inspection profiles) — only the Maven build is the source of truth.

## Decisions

### D1: Plugin coordinates and versions
- `org.apache.maven.plugins:maven-checkstyle-plugin:3.6.0` with a runtime dependency override to the newest stable `com.puppycrawl.tools:checkstyle` 10.x+ release (3.6.0 bundles Checkstyle 9.3, which is dated; pinning the exact override version at implementation from Maven Central).
- `com.github.spotbugs:spotbugs-maven-plugin:4.10.5.0` (SpotBugs 4.10.5).
- Versions managed in the parent's `<pluginManagement>` and the plugins activated in the parent's `<build><plugins>`, so all modules inherit; no module POM edits.

*Alternative considered*: adding plugins per-module — rejected, duplication across 6+ POMs.

### D2: Enforcement binding — `verify` phase
`checkstyle:check` and `spotbugs:check` executions bound to `verify` with `failOnViolation`/`failOnError` enabled. `verify` is what CI runs, so enforcement needs no CI-side change; local `test`/`compile` iterations stay fast. Running at `validate` would slow every goal; at `install` it would be skipped by CI.

### D3: Config file locations and resolution
- `config/checkstyle.xml` and `config/spotbugs-exclude.xml` at the repository root, referenced as `${maven.multiModuleProjectDirectory}/config/...` so every submodule resolves them regardless of invocation directory (`.mvn/` exists at the root, so the property is reliable with the wrapper).

*Alternative considered*: `checkstyle-suppressions.xml` combined with a bundled ruleset, or per-module relative paths (`../config/...`) — rejected: relative paths break for the apps aggregator depth and per-module configs fragment the ruleset.

### D4: Pragmatic Checkstyle ruleset (single `config/checkstyle.xml`)
Ruleset mirrors the existing codebase so findings are genuine issues, not style-conversion work:

- **Layout**: `LineLength` 120 (imports ignored), `FileTabCharacter`, `NewlineAtEndOfFile`, default brace placement (`LeftCurly` eol / `RightCurly` alone).
- **Indentation**: `Indentation` with 4-space offsets, lenient (non-strict continuation lines). It was initially dropped when the first pass measured 913 hand-wrapped continuation-line findings; a follow-up pass (task group 5) then fixed all of them (833 remaining after the other style fixes) by adjusting leading whitespace only, and the check is enforced since. If a re-enabled check ever proves unfixable without a mass reformat, drop it again and record that decision in a comment in `checkstyle.xml` — never leave a check enabled that the codebase silently fails.
- **Imports**: `UnusedImports`, `RedundantImport`, `AvoidStarImport`, `IllegalImport` (sun.\*). Full ordering (`ImportOrder`) is NOT enforced — mechanical churn with low defect value for this port.
- **Naming**: standard camelCase/PascalCase checks (`TypeName`, `MethodName`, `MemberName`, `LocalVariableName`, `ParameterName`, `ConstantName`, `PackageName`, type-parameter names), `OneTopLevelClass`.
- **Coding**: `EqualsHashCode`, `StringLiteralEquality`, `EmptyStatement`, `SimplifyBooleanExpression`, `SimplifyBooleanReturn`, `MissingSwitchDefault`, `EmptyCatchBlock` (comments allowed), `HideUtilityClassConstructor`, `ArrayTypeStyle`, `DefaultComesLast`, `FallThrough`.
- **Blocks/modifiers**: `NeedBraces` (single-line statements allowed), `ModifierOrder`, `RedundantModifier`, `WhitespaceAround`, `GenericWhitespace`, `ParenPad`.
- **Explicitly excluded**: Javadoc rules, `MagicNumber`, `FinalParameters`, `AbbreviationAsWordInName` — high churn on a ported Struts 1 API with little defect value.

`includeTestSourceDirectory=true`: test code follows the same style rules. The precise rule set is finalized during implementation; any rule dropped from the list above is recorded with a reason in `checkstyle.xml` comments.

### D5: SpotBugs configuration and exclusion policy
- `effort=Max`, `threshold=Medium`, `failOnError=true`, XML report on.
- `config/spotbugs-exclude.xml` seeded with generated-code exclusions: `~org\.apache\.struts\.validator\.validwhen\..*` (ANTLR) and `~.*\.jmh_generated\..*` (JMH).
- **Exclusion policy (spec: Documented exclusions only)**: prefer fixing over excluding; exclude only when a fix would change the Struts 1 public API or observable behavior. Expected intentional patterns in this port: `EI_EXPOSE_REP`/`EI_EXPOSE_REP2` (config objects such as `ModuleConfig`/`ForwardConfig` are deliberately mutable and shared), public-field access driven by digester/BeanUtils, and similar. Each `<Match>` entry carries a comment with the rationale; exclusions are scoped as tightly as possible (pattern + class/package), never a blanket "disable everything".

### D6: Finding-fix strategy
- Checkstyle findings: purely mechanical (imports, whitespace, final newline, braces, naming) — no behavior change, bulk-applied.
- SpotBugs findings: behavior-preserving refactors first (proper `equals` instead of `==`, null guards, `EqualsHashCode`, resource handling, removing dead stores). When a clean fix is impossible without changing the public API, the finding is excluded per D5 instead.
- Gate: `./mvnw verify` fully green (all tests + integration tests on embedded Tomcat) after the fix pass; every fix keeps the existing test suite passing.

### D7: CI integration
No new CI job: the checks run inside the existing `./mvnw -B -ntp verify` step and fail it. One added step uploads `**/target/checkstyle-result.xml` and `**/target/spotbugsXml.xml` with `if: always()` (mirrors the surefire-reports upload; satisfies the ci-pipeline delta).

### D8: Docs
README gains a short "Code quality checks" section: what runs on `verify`, how to run the checks alone (`./mvnw checkstyle:check spotbugs:check`), and where ruleset/exclusions live.

## Risks / Trade-offs

- [Checkstyle rules prove noisier than expected on ~500 ported files] → fix pass starts with the highest-churn rules first; a rule that cannot be satisfied without unreasonable churn is dropped with a documented reason (D4) rather than left half-enforced.
- [SpotBugs `EI_EXPOSE_REP` flood on the Struts config API] → tightly scoped exclusions with rationale (D5); no blanket pattern disables.
- [A "fix" changes runtime behavior] → fixes are behavior-preserving refactors; full `verify` with the embedded-Tomcat integration tests is the gate; behavioral questions default to exclusion-with-rationale instead of code change.
- [Build time grows on `verify`] → analysis runs once per module at verify; CI timeout (30 min) has headroom; local `test` iterations unaffected.
- [Checkstyle 10.x+ override incompatible with plugin 3.6.0 config features] → pin the exact override version at implementation; the ruleset uses only long-stable checks.

## Migration Plan

Single change, no deployment step: plugins, config files, fixes, and the CI upload land together, and CI enforces from the first run after merge. Rollback is reverting the change (no data or state involved).

## Open Questions

None — the exact Checkstyle override version is pinned at implementation time from Maven Central (deferrable, does not affect specs or task breakdown).
