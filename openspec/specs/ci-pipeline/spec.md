# ci-pipeline Specification

## Purpose

Provides reproducible automatic verification of every pull request and the main branch: a full project build, all tests, and saving results for diagnostics.

## Requirements

### Requirement: Pull request check
CI SHALL run for every pull request targeting `main` on open, reopen and head-commit update. The run result SHALL be displayed as a PR check and SHALL be unsuccessful if any build stage fails.

#### Scenario: New pull request
- **WHEN** a pull request to `main` is created
- **THEN** a CI run starts and its status appears in the pull request checks list

#### Scenario: Pull request update
- **WHEN** a new commit is added to a pull request
- **THEN** CI runs again for the new head commit

#### Scenario: Build failure in a pull request
- **WHEN** compilation, a unit test or an integration test in the PR fails
- **THEN** the PR check gets status failed and the run is not considered successful

### Requirement: Main branch check and manual run
CI SHALL run on push to `main` and SHALL support manual runs from GitHub Actions. The run for `main` SHALL execute the same full set of checks as the pull request run.

#### Scenario: Push to main
- **WHEN** a commit lands in `main`
- **THEN** a full CI run starts for that commit

#### Scenario: Manual run
- **WHEN** a user runs the workflow manually via `workflow_dispatch`
- **THEN** a full CI run executes for the selected branch or commit

### Requirement: Full build and tests on JDK 17
CI SHALL build the project with the Maven wrapper on JDK 17 and SHALL run `verify` for all modules, including compilation, unit tests, integration tests on embedded Tomcat and WAR/JAR packaging. Tests SHALL run rather than be skipped with the `-DskipTests`/`-Dmaven.test.skip` flags.

#### Scenario: Successful full build
- **WHEN** CI builds a commit where all modules compile and all tests pass
- **THEN** the run completes successfully and the WAR and JAR of all modules are built

#### Scenario: Integration test failure
- **WHEN** an integration test on embedded Tomcat fails
- **THEN** the run stops with a non-zero exit code and does not publish artifacts as successful

### Requirement: Dependency caching
CI SHALL cache the Maven repository and the Maven wrapper distribution between runs. The cache key SHALL take into account the contents of `pom.xml` and `.mvn/wrapper/maven-wrapper.properties`, so that a change of dependencies or the Maven version does not lead to a stale cache being used. A cache miss SHALL lead to a normal build, not an error.

#### Scenario: Repeated run with the same dependency set
- **WHEN** CI runs again with no changes in `pom.xml` and the wrapper properties
- **THEN** Maven dependencies are restored from cache and the build passes

#### Scenario: Dependency change
- **WHEN** a dependency version in `pom.xml` or the Maven version in the wrapper properties changes
- **THEN** a new cache key is used and the build runs with up-to-date dependencies

### Requirement: Saving test reports
CI SHALL save surefire reports as a run artifact regardless of the build result, including the case of test failures. The saved reports SHALL contain the results of each test class and SHALL be available for download after the run completes.

#### Scenario: Successful run
- **WHEN** all tests pass
- **THEN** surefire reports are available as a run artifact

#### Scenario: Test failure
- **WHEN** one or more tests fail
- **THEN** surefire reports are still uploaded and available for diagnostics

### Requirement: Saving static analysis reports
CI SHALL save the Checkstyle and SpotBugs result reports as a run artifact regardless of the build result, including the case where one of the checks fails the build. The saved reports SHALL cover every analyzed module and SHALL be available for download after the run completes, so a static-analysis failure can be diagnosed from the run.

#### Scenario: Successful run with reports
- **WHEN** all checks pass and the build succeeds
- **THEN** the Checkstyle and SpotBugs reports of all modules are available as a run artifact

#### Scenario: Static analysis failure
- **WHEN** a style violation or bug-pattern finding fails the build in CI
- **THEN** the reports are still uploaded and available for diagnostics

### Requirement: Publishing build artifacts
CI SHALL save the built WAR and JAR of the modules as an artifact of a successful run. The artifact SHALL include all executable reactor results: library JARs, WARs of test applications and the dao module JAR.

#### Scenario: Successful main build
- **WHEN** a full run for `main` completes successfully
- **THEN** the WAR/JAR of all modules are available as a run artifact

#### Scenario: Unsuccessful build
- **WHEN** the run fails
- **THEN** the artifact with WAR/JAR is not published as a successful build result

### Requirement: Concurrency and run safety
CI SHALL cancel outdated runs for the same pull request or branch when a new commit arrives. The workflow SHALL run with minimal permissions (`contents: read`), without secrets and external services, and SHALL have a run time limit so that a hung run does not block the queue forever.

#### Scenario: New commit in an active PR
- **WHEN** a pull request receives a new commit while the previous run is still in progress
- **THEN** the previous run is cancelled and only the new commit is checked

#### Scenario: Hung run
- **WHEN** a build exceeds the configured time limit
- **THEN** the run fails by timeout and does not remain in the queue forever

### Requirement: CI status display
The project README SHALL contain a CI workflow status badge linking to the GitHub Actions runs page.

#### Scenario: Viewing the README
- **WHEN** a user opens the README on GitHub
- **THEN** they see the current CI status and can navigate to the list of runs
