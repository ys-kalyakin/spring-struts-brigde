# Proposal

## Why

Currently build quality is confirmed only by a local `./mvnw verify`: the repository has no GitHub Actions checks at all, so regressions in PRs and in `main` are discovered manually, and the built WAR/JAR files are not stored anywhere. The project has already completed phases 1–3 and contains 205 tests, including integration runs on embedded Tomcat — they need a reproducible gate for every PR and every change to the main branch.

## What Changes

- **A new workflow `.github/workflows/ci.yml`** for GitHub Actions:
  - triggers: `pull_request` to `main`, `push` to `main`, and manual `workflow_dispatch`;
  - job `build` on `ubuntu-latest`: checkout, Temurin JDK 17 (the project minimum, `maven.compiler.release=17`), Maven repository and wrapper caching, `./mvnw -B -ntp verify` (compilation, unit and integration tests, WAR/JAR packaging);
  - `concurrency` with cancellation of outdated runs for PRs, `permissions: contents: read`, `timeout-minutes`, and non-interactive Maven output.
- **Publishing results**: surefire reports (`**/target/surefire-reports/**`) are always uploaded as an artifact, even when tests fail; the built WAR/JAR files are uploaded as an artifact of a successful build. This makes failures diagnosable without re-running locally.
- **Caching** of Maven dependencies and the Maven wrapper distribution keyed on `pom.xml`/`maven-wrapper.properties`, so a cold build does not re-download dependencies.
- **A CI status badge** in `README.md` linking to the workflow.
- The `main` branch is used as the primary one (it is also the default branch in `origin`); there is no separate workflow for releases, publishing to Maven Central, or deployment — this is out of scope for the change.

## Capabilities

### New Capabilities

- `ci-pipeline`: GitHub Actions CI behavior — mandatory checks for PRs and `main`, build composition, caching, concurrency, retention of reports and artifacts, manual triggering.

### Modified Capabilities

None: the change does not alter the behavior of the framework, its API, or the test applications.

## Impact

- **Files:** new `.github/workflows/ci.yml`; an edit to `README.md` (badge); nothing else in the code or the build changes.
- **Infrastructure:** a GitHub-hosted `ubuntu-latest` runner, the official actions `actions/checkout`, `actions/setup-java`, `actions/cache`, `actions/upload-artifact`; no secrets required, fork PRs work.
- **Build:** remains `./mvnw verify` with no pom changes; CI uses the same commands as the local check.
- **Out of scope:** releases and publishing artifacts on tags, deployment, CodeQL/SAST, dependency review, a JDK/OS matrix, self-hosted runners, Docker images.
