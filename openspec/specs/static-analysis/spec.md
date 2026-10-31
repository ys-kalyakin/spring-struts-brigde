# static-analysis Specification

## Purpose

Enforces a uniform code style and guards against statically detectable bug patterns: the Maven build checks every module with Checkstyle (style) and SpotBugs (bug patterns) on `verify`, fails on any violation or finding, and keeps all exceptions in a documented exclusion file.

## Requirements

### Requirement: Style enforcement on build
The build SHALL run the code style check (Checkstyle) for every module during `verify` and SHALL fail the build when the code violates the project ruleset. The ruleset SHALL be defined in a single configuration file inside the repository, shared by all modules. The ruleset SHALL match the existing project style (4-space indentation, 120-character line limit, import hygiene, naming, whitespace, block layout) so that enforcement requires only genuine fixes, not a mass reformat.

#### Scenario: Style violation fails the build
- **WHEN** a module contains code that violates a rule of the project style ruleset
- **THEN** `./mvnw verify` fails for that module and the violation (rule, file, line) is reported

#### Scenario: Conforming code passes
- **WHEN** all modules conform to the project style ruleset
- **THEN** `./mvnw verify` completes successfully with no style errors reported

### Requirement: Bug-pattern enforcement on build
The build SHALL run static bug-pattern analysis (SpotBugs) for every module during `verify` at maximum effort and medium confidence threshold, and SHALL fail the build when any finding is reported at that threshold.

#### Scenario: Bug-pattern finding fails the build
- **WHEN** a module's compiled classes trigger a SpotBugs pattern at or above the configured confidence threshold
- **THEN** `./mvnw verify` fails for that module and the finding (pattern, class, line) is reported

#### Scenario: Clean classes pass
- **WHEN** no module produces SpotBugs findings at the configured threshold
- **THEN** `./mvnw verify` completes successfully

### Requirement: Generated code excluded
The style and bug-pattern checks SHALL NOT report violations for generated sources: the ANTLR-generated validwhen parser in spring-struts1-core and JMH-generated classes in spring-struts1-benchmarks.

#### Scenario: Generated parser is not flagged
- **WHEN** the checks run on spring-struts1-core
- **THEN** no violation or finding is reported for the generated validwhen parser classes

#### Scenario: Generated benchmark code is not flagged
- **WHEN** the checks run on spring-struts1-benchmarks
- **THEN** no violation or finding is reported for JMH-generated classes

### Requirement: Documented exclusions only
Every deliberate exception to the checks SHALL be declared in a shared exclusion configuration inside the repository, and every exclusion SHALL carry a written rationale referencing why the pattern is intentional. Ad-hoc suppression that is not reflected in the exclusion configuration SHALL NOT be introduced to make a check pass.

#### Scenario: Intentional pattern needs an exclusion
- **WHEN** the codebase relies on a pattern the checks flag (for example, deliberately mutable configuration objects exposed by the Struts 1 API)
- **THEN** the pattern is excluded in the shared exclusion configuration with a rationale, and the build passes without altering the pattern

#### Scenario: Exclusion without rationale is not accepted
- **WHEN** a contributor adds an exclusion entry without a rationale
- **THEN** the exclusion is rejected in review because the configuration requires a documented reason per entry

### Requirement: Analysis reports produced
The build SHALL produce machine-readable result reports for both the style check and the bug-pattern analysis in each module's build output directory, regardless of whether the check passed.

#### Scenario: Reports available after a run
- **WHEN** `./mvnw verify` runs (passing or failing)
- **THEN** a Checkstyle result report and a SpotBugs result report exist in each analyzed module's build output directory

### Requirement: Local reproducibility
A developer SHALL be able to run both checks on a clean checkout using the Maven wrapper alone, with no configuration beyond the repository contents.

#### Scenario: Fresh clone run
- **WHEN** a developer clones the repository and runs `./mvnw verify`
- **THEN** both checks execute with the repository-provided ruleset and exclusion configuration and no additional setup
