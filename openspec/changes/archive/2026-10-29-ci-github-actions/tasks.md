# Tasks

## 1. Workflow skeleton and triggers

- [x] 1.1 Create `.github/workflows/ci.yml`: name `CI`, triggers `pull_request` to `main`, `push` to `main`, `workflow_dispatch`; `permissions: contents: read`, `concurrency` with a group keyed on PR/ref and `cancel-in-progress: true`, `timeout-minutes: 30`, job `build` on `ubuntu-latest` with checkout. Verify: the YAML parses (`python3 -c "import yaml; yaml.safe_load(open('.github/workflows/ci.yml'))"` or `ruby -e 'require "yaml"; YAML.load_file(...)'`), all three triggers and the limits are present in the file, and `git diff --check` is clean.
- [x] 1.2 Add the JDK and build steps: `actions/setup-java@v4` (`temurin`, `17`, `cache: maven`), a cache of `~/.m2/wrapper` keyed on the hash of `.mvn/wrapper/maven-wrapper.properties`, `LANG: C.UTF-8`, and `./mvnw -B -ntp verify` without test-skipping flags. Verify: a local `./mvnw -B -ntp verify` on JDK 17 ends with `BUILD SUCCESS`; the workflow contains no `-DskipTests`/`-Dmaven.test.skip`, and the Java version and cache keys match design D2–D3.

## 2. Diagnostics and artifacts

- [x] 2.1 Add a surefire report upload `actions/upload-artifact@v4` with `if: always()`, `if-no-files-found: warn`, 7-day retention, and an `if: always()` summary step collecting `tests/failures/errors/skipped` from `TEST-*.xml` into `$GITHUB_STEP_SUMMARY`. Verify: a local `verify` run produces the reports; the summary script prints `205` tests for them and does not fail on an empty reports directory.
- [x] 2.2 Add a `build-artifacts` upload with `if: success()`, `if-no-files-found: error`: the JARs `spring-struts1-core`, `spring-struts1-taglib`, `spring-struts1-tiles`, `spring-struts1-apps/mailreader-dao` and the WARs `blank`, `examples`, `cookbook`, `mailreader`. Verify: after a local `verify` all eight paths exist (`find … -name '*.war' -o -name '*.jar'`), and the workflow paths contain no stray `*-sources.jar`.

## 3. README and status

- [x] 3.1 Add a `ci.yml` badge to `README.md` linking to `https://github.com/ys-kalyakin/spring-struts-brigde/actions/workflows/ci.yml` and `branch=main`. Verify: the markdown link and badge URL point to an existing workflow and the `main` branch, and the badge renders in the README preview.

## 4. CI integration check

- [ ] 4.1 Verify the success path on GitHub: push a branch with the change and open a PR to `main`; confirm that the workflow starts on `pull_request`, the `CI / build` job appears in the checks, and after success the `surefire-reports` and `build-artifacts` (4 WAR + 4 JAR) are available. Verify: the check status is green, the artifacts download, and the summary shows the test totals.
- [x] 4.2 Verify the failure path: add a deliberately failing test on a temporary branch, push it, and confirm that the check becomes failed, `surefire-reports` is still uploaded, and `build-artifacts` is not published; then revert the test commit. Verify: a failed run with the reports in artifacts and no `build-artifacts`.
- [ ] 4.3 Verify manual triggering and cancellation: run `workflow_dispatch` for a branch, then push a second commit to an active PR and confirm that the previous run is cancelled. Verify: the manual run completes successfully, and the PR retains only the run of the latest commit.
- [x] 4.4 Final check: `openspec validate ci-github-actions --strict` passes, `./mvnw -B -ntp verify` is green locally, and `git status` shows no temporary build files other than the ignored `target/`. Verify: both commands exit with code 0, and the working tree contains only `.github/workflows/ci.yml`, `README.md`, and OpenSpec artifacts.

## 5. Fix identified by the first CI run

- [x] 5.1 Make `TilesIntegrationTest.localizedDefinitionsFileIsSelectedByLocale` independent of the JVM locale: request the base variant with an explicit `Accept-Language: ru`, because on a GitHub runner the JVM defaults to `en` and selected `tiles-defs_en`. Verify: `LANG=C.UTF-8 LC_ALL=C.UTF-8 ./mvnw -B -ntp verify` ends with `BUILD SUCCESS`, and the full run on GitHub is green.

## Workflow follow-up

- A repository administrator enables `CI / build` as a required status check for `main` (branch protection/ruleset) — outside the repository files.
- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
