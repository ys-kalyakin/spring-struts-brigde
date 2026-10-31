# Design

## Context

See `proposal.md` — Why. The project build is already reproducible locally: `./mvnw -B -ntp verify` on JDK 17 builds the 10 reactor modules, runs 205 tests (including integration tests on embedded Tomcat with port `0`) in ~31 s on a warm cache, and produces 4 WARs and 4 JARs. The Maven wrapper is pinned to 3.9.9, `maven.compiler.release=17`. The primary branch in `origin` is `main`; the GitHub repository is `ys-kalyakin/spring-struts-brigde` (the spelling in the remote is kept as is).

Constraints: the workflow must work on fork PRs without secrets, must not require self-hosted runners, and must not change the pom or the build command; the `main` branch must not be built twice by different workflows.

## Goals / Non-Goals

**Goals:**

- One reproducible pipeline for PRs, `main`, and manual runs, with the same set of checks.
- Diagnosability: test reports are available even on failure, WAR/JAR files on success.
- Minimal permissions and predictable execution time.

**Non-Goals:**

- Publishing to Maven Central, tag-based releases, deployment, Docker images.
- A JDK/OS matrix, CodeQL/SAST, dependency review, Dependabot.
- Configuring branch protection: enabling the check as required is a repository administrator operation, not a workflow file change (see Migration Plan).

## Decisions

### D1. One `ci.yml` workflow instead of separate PR/main workflows

`on: pull_request` (branch `main`), `on: push` (branch `main`), `on: workflow_dispatch`. A single job `build` named `CI / build` provides a stable check name for branch protection and rules out step divergence between PR and main. The alternative — `pr.yml` + `main.yml` — was rejected: configuration duplication and two different statuses for the same build.

### D2. JDK 17, Temurin, Maven wrapper

`actions/setup-java@v4` with `distribution: temurin`, `java-version: '17'`, `cache: maven`. JDK 17 is the project minimum (`maven.compiler.release=17`) and matches the local check; the 17/21/25 matrix was rejected by the user as excessive for the current stage. The build is only `./mvnw -B -ntp verify`: the wrapper pins Maven 3.9.9, and `verify` covers compile, surefire (unit + IT), and packaging. Test-skipping flags are not used. For environment determinism, `LANG: C.UTF-8` is set (JSPs and Cyrillic resources).

### D3. Two cache levels: Maven repository and wrapper

`setup-java` with `cache: maven` caches `~/.m2/repository` keyed on the hash of `**/pom.xml` (all modules). Additionally, `actions/cache@v4` caches `~/.m2/wrapper` keyed on the hash of `.mvn/wrapper/maven-wrapper.properties`, because `setup-java` does not cover the wrapper distribution. `restore-keys` is not set: a cache miss is a normal cold build, not an error. A fork PR's cache is read-only, so cache poisoning from a fork is impossible.

### D4. Reports — always, artifacts — only on success

Surefire reports (`**/target/surefire-reports/**`) are uploaded by `actions/upload-artifact@v4` with `if: always()` and `if-no-files-found: warn`: when tests fail, the reports are the primary source of diagnosis. WAR/JAR files are uploaded as a separate `build-artifacts` artifact with `if: success()` and `if-no-files-found: error`: a failed build must not look like a delivery. Retention is 7 days — enough for PR triage without bloating storage. Additionally, an `if: always()` step collects a `tests/failures/errors/skipped` summary from `TEST-*.xml` into `$GITHUB_STEP_SUMMARY` (python3 is available on ubuntu-latest), so the outcome is visible without downloading the artifact.

The alternative — a third-party JUnit reporter action — was rejected: an extra dependency and extra permissions; the native summary + artifact covers the need.

### D5. Concurrency and timeouts

`concurrency.group: ci-${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}`, `cancel-in-progress: true`: a new commit in a PR cancels the outdated run, and a new push to `main` cancels the previous one. `timeout-minutes: 30` on the job (a cold build with dependency downloads fits with a large margin; a locally warmed one takes ~31 s). This protects against hung embedded-Tomcat tests that would otherwise keep holding the runner.

### D6. Minimal permissions, official actions only

`permissions: contents: read` at the workflow level, no secrets used, so fork PRs work without `pull_request_target` (and its risks). Only `actions/checkout@v4`, `actions/setup-java@v4`, `actions/cache@v4`, `actions/upload-artifact@v4` are used; versions are major tags, updating them is a separate maintenance task. No `secrets.*`, no `GITHUB_TOKEN` with write permissions, no third-party actions.

### D7. Badge in README

A badge for `ci.yml` linking to `actions/workflows/ci.yml` with `branch=main`; the repository URL is the actual `ys-kalyakin/spring-struts-brigde`. A separate workflow for the badge is not needed.

## Risks / Trade-offs

- [A cold build downloads dependencies and the wrapper] → two caches (D3); a miss does not break the build, only slows it down.
- [Integration tests start an embedded Tomcat] → port `0`, tests are isolated; the 30-minute timeout and `cancel-in-progress` prevent a hung run from blocking the queue.
- [PR artifacts consume storage] → 7-day retention and successful builds only for WAR/JAR; test reports are small.
- [Major tags of actions can be re-pointed] → only official `actions/*` are used; if security requirements tighten, SHA pinning as a separate task.
- [Branch protection cannot be configured from a workflow] → the Migration Plan records a manual administrator step: make `CI / build` required for `main`.
- [The local remote contains the `brigde` typo] → the badge and links use the actual URL; renaming the repository is out of scope.

## Migration Plan

1. Add `.github/workflows/ci.yml` and the badge to README; run YAML validation and `./mvnw -B -ntp verify` locally.
2. Open a test PR: confirm that the workflow starts, reports and artifacts are available, and that a failing test (a temporary commit) yields failed while still uploading the surefire reports; then revert the temporary commit.
3. A repository administrator enables `CI / build` as a required status check for `main` (branch protection/ruleset) — the only step outside the repository.
4. Rollback: delete `.github/workflows/ci.yml` and the badge; code, the build, and artifacts are unaffected.
