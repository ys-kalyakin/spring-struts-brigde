# Tasks

## 1. Plugin wiring (parent POM + config files)

- [x] 1.1 Pin plugin versions in the parent `pom.xml` properties and `pluginManagement`: `checkstyle-plugin.version` = 3.6.0, Checkstyle runtime override = newest stable `com.puppycrawl.tools:checkstyle` 10.x+ (pinned from Maven Central at implementation), `spotbugs-plugin.version` = 4.10.5.0. Verify: `./mvnw -B -ntp help:evaluate` resolves the properties and the plugins download on first use.
- [x] 1.2 Create `config/checkstyle.xml` with the pragmatic ruleset from design D4 (LineLength 120, 4-space lenient Indentation, import hygiene, naming, coding, blocks/modifiers; no Javadoc/MagicNumber rules). Verify: the file is loaded without a config parse error by a first `./mvnw -B -ntp checkstyle:check` run.
- [x] 1.3 Create `config/spotbugs-exclude.xml` seeded with the generated-code exclusions (`~org\.apache\.struts\.validator\.validwhen\..*`, `~.*\.jmh_generated\..*`), each entry carrying a rationale comment. Verify: file exists and is accepted by a first `./mvnw -B -ntp spotbugs:check` run.
- [x] 1.4 Activate both plugins in the parent `<build><plugins>` bound to `verify` (design D2): checkstyle `check` with `configLocation=${maven.multiModuleProjectDirectory}/config/checkstyle.xml`, `includeTestSourceDirectory=true`, `failOnViolation=true`; spotbugs `check` with `effort=Max`, `threshold=Medium`, `excludeFilterFile=${maven.multiModuleProjectDirectory}/config/spotbugs-exclude.xml`, `failOnError=true`, XML report on. Verify: `./mvnw -B -ntp checkstyle:check spotbugs:check` runs for every module and reports the expected initial violations/findings (proves the wiring reaches all modules).

## 2. Checkstyle fix pass (mechanical, no behavior change)

- [x] 2.1 Fix style violations in `spring-struts1-core` (imports, whitespace, final newline, braces, naming; test sources included). Verify: `./mvnw -B -ntp -pl spring-struts1-core checkstyle:check` passes.
- [x] 2.2 Fix style violations in `spring-struts1-taglib`. Verify: `./mvnw -B -ntp -pl spring-struts1-taglib checkstyle:check` passes.
- [x] 2.3 Fix style violations in `spring-struts1-tiles` and `spring-struts1-starter`. Verify: `./mvnw -B -ntp -pl spring-struts1-tiles,spring-struts1-starter checkstyle:check` passes.
- [x] 2.4 Fix style violations in the `spring-struts1-apps` submodules (blank, examples, cookbook, mailreader-dao, mailreader, demo). Verify: `./mvnw -B -ntp -pl spring-struts1-apps -amd checkstyle:check` passes for all app modules.
- [x] 2.5 Fix style violations in `spring-struts1-benchmarks`. Verify: `./mvnw -B -ntp -pl spring-struts1-benchmarks checkstyle:check` passes.
- [x] 2.6 Tune the ruleset if a rule proves unreasonably noisy: drop the rule and record the decision with a reason comment in `config/checkstyle.xml` (design D4) — never leave a check enabled that the codebase silently fails. Verify: final `./mvnw -B -ntp checkstyle:check` is green and every deviation from the D4 rule list is documented in the file.

## 3. SpotBugs fix pass (behavior-preserving)

- [x] 3.1 Fix SpotBugs findings with behavior-preserving refactors (proper `equals` for string comparison, missing `equals`/`hashCode`, null guards, dead stores, resource handling, serialization fixes) across all modules. Verify: `./mvnw -B -ntp spotbugs:check` passes for every module.
- [x] 3.2 For findings that cannot be fixed without changing the Struts 1 public API or observable behavior, add tightly scoped exclusion entries (pattern + class/package) to `config/spotbugs-exclude.xml`, each with a rationale comment (design D5). Verify: every added `<Match>` carries a rationale and the build passes without code changes to the excluded sites.
- [x] 3.3 Run the full gate after the fix pass. Verify: `./mvnw -B -ntp verify` completes successfully — all unit tests and embedded-Tomcat integration tests green, confirming fixes introduced no behavior change.

## 4. CI and documentation

- [x] 4.1 Add a CI step in `.github/workflows/ci.yml` uploading `**/target/checkstyle-result.xml` and `**/target/spotbugsXml.xml` with `if: always()` and `if-no-files-found: warn`, mirroring the surefire-reports upload. Verify: the workflow YAML parses (`python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/ci.yml'))"` or equivalent) and the step matches the ci-pipeline delta (reports uploaded on failure too).
- [x] 4.2 Document the checks in README (section "Code quality checks"): what runs on `verify`, standalone commands (`./mvnw checkstyle:check spotbugs:check`), and where `config/checkstyle.xml` / `config/spotbugs-exclude.xml` live and how exclusions are governed. Verify: the documented commands run exactly as written from a clean checkout.
- [x] 4.3 Final integration gate simulating CI locally. Verify: `./mvnw -B -ntp verify` exits 0 on a clean checkout with the Maven wrapper alone (local reproducibility scenario), including the new check executions in the reactor output.

## 5. Follow-up: enforce Indentation (review request)

- [x] 5.1 Re-enable the `Indentation` check in `config/checkstyle.xml` with Checkstyle defaults (4-space basic offset, lenient continuation lines) and update the exclusion notes in the file header. Verify: `./mvnw -B -ntp checkstyle:checkstyle` reports only `IndentationCheck` violations (833 across 52 files, tests included).
- [x] 5.2 Fix all Indentation violations by adjusting leading whitespace only (text-block content excluded by a guard); rewrap by hand the two hand-split call sites the check oscillates on (`FactorySet.createFactory`, `MemoryUserDatabase.open`) and the one line the reindent pushed past 120 columns. Verify: `./mvnw -B -ntp checkstyle:check` is green and the diff of files untouched by the earlier fix passes contains leading-whitespace changes only.
- [x] 5.3 Full gate after the follow-up. Verify: `./mvnw -B -ntp clean verify` passes for all 13 modules — unit and embedded-Tomcat integration tests, Checkstyle and SpotBugs all green.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result: `openspec/changes/archive/` contains the change and `openspec/specs/static-analysis/spec.md` exists alongside the updated `ci-pipeline` spec.
