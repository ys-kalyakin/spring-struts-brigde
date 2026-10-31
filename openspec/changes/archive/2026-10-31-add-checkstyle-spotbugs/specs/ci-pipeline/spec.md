# Spec Delta

## ADDED Requirements

### Requirement: Saving static analysis reports
CI SHALL save the Checkstyle and SpotBugs result reports as a run artifact regardless of the build result, including the case where one of the checks fails the build. The saved reports SHALL cover every analyzed module and SHALL be available for download after the run completes, so a static-analysis failure can be diagnosed from the run.

#### Scenario: Successful run with reports
- **WHEN** all checks pass and the build succeeds
- **THEN** the Checkstyle and SpotBugs reports of all modules are available as a run artifact

#### Scenario: Static analysis failure
- **WHEN** a style violation or bug-pattern finding fails the build in CI
- **THEN** the reports are still uploaded and available for diagnostics
