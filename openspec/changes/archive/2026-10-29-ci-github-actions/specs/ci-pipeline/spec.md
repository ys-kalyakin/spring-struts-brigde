# Spec Delta

## Purpose

Provides a reproducible automated check of every pull request and the main branch: a full project build, all tests, and retention of results for diagnostics.

## ADDED Requirements

### Requirement: Pull request check
CI SHALL run for every pull request targeting `main`, on open, reopen, and head-commit update. The run result SHALL be displayed as a PR check and SHALL be unsuccessful if any build stage fails.

#### Scenario: New pull request
- **WHEN** a pull request to `main` is created
- **THEN** a CI run starts, and its status appears in the pull request's list of checks

#### Scenario: Pull request update
- **WHEN** a new commit is added to a pull request
- **THEN** CI runs again for the new head commit

#### Scenario: Build failure in a pull request
- **WHEN** compilation, a unit test, or an integration test in the PR fails
- **THEN** the PR check gets the failed status, and the run is not considered successful

### Requirement: Main branch check and manual trigger
CI SHALL run on push to `main` and SHALL support manual triggering from GitHub Actions. A run for `main` SHALL execute the same full set of checks as a pull request run.

#### Scenario: Push to main
- **WHEN** a commit lands in `main`
- **THEN** a full CI run starts for that commit

#### Scenario: Manual trigger
- **WHEN** a user triggers the workflow manually via `workflow_dispatch`
- **THEN** a full CI run executes for the selected branch or commit

### Requirement: Full build and tests on JDK 17
CI SHALL build the project with the Maven wrapper on JDK 17 and SHALL run `verify` for all modules, including compilation, unit tests, integration tests on embedded Tomcat, and WAR/JAR packaging. Tests SHALL be executed, not skipped via the `-DskipTests`/`-Dmaven.test.skip` flags.

#### Scenario: Successful full build
- **WHEN** CI builds a commit in which all modules compile and all tests pass
- **THEN** the run completes successfully, and the WAR and JAR of every module are built

#### Scenario: Integration test failure
- **WHEN** an integration test on embedded Tomcat fails
- **THEN** the run stops with a non-zero exit code and does not publish artifacts as successful

### Requirement: Dependency caching
CI SHALL cache the Maven repository and the Maven wrapper distribution between runs. The cache key SHALL take into account the contents of `pom.xml` and `.mvn/wrapper/maven-wrapper.properties`, so that a change in dependencies or the Maven version does not lead to a stale cache being used. A cache miss SHALL result in a normal build, not an error.

#### Scenario: Repeated run with the same set of dependencies
- **WHEN** CI runs again with no changes to `pom.xml` and the wrapper properties
- **THEN** the Maven dependencies are restored from cache, and the build proceeds

#### Scenario: Dependency change
- **WHEN** a dependency version in `pom.xml` or the Maven version in the wrapper properties changes
- **THEN** a new cache key is used, and the build runs with up-to-date dependencies

### Requirement: Test report retention
CI SHALL retain the surefire reports as a run artifact regardless of the build outcome, including the case of failing tests. The retained reports SHALL contain the results of every test class and SHALL be available for download after the run completes.

#### Scenario: Successful run
- **WHEN** all tests pass
- **THEN** the surefire reports are available as a run artifact

#### Scenario: Test failures
- **WHEN** one or more tests fail
- **THEN** the surefire reports are still uploaded and available for diagnostics

### Requirement: Build artifact publication
CI SHALL retain the built WAR and JAR modules as an artifact of a successful run. The artifact SHALL include all executable outputs of the reactor: the library JARs, the test application WARs, and the dao module JAR.

#### Scenario: Successful main build
- **WHEN** a full run for `main` completes successfully
- **THEN** the WAR/JAR of every module is available as a run artifact

#### Scenario: Failed build
- **WHEN** the run fails
- **THEN** the artifact with the WAR/JAR is not published as a successful build result

### Requirement: Run concurrency and safety
CI SHALL cancel outdated runs for the same pull request or branch when a new commit arrives. The workflow SHALL operate with minimal permissions (`contents: read`), without secrets or external services, and SHALL have an execution time limit so that a hung run does not block the queue.

#### Scenario: New commit in an active PR
- **WHEN** a new commit arrives in a pull request while the previous run is still executing
- **THEN** the previous run is cancelled, and only the new commit is checked

#### Scenario: Hung run
- **WHEN** the build exceeds the configured time limit
- **THEN** the run fails by timeout and does not remain in the queue forever

### Requirement: CI status display
The project README SHALL contain a CI workflow status badge linking to the GitHub Actions runs page.

#### Scenario: Viewing the README
- **WHEN** a user opens the README on GitHub
- **THEN** they see the current CI status and can navigate to the list of runs
